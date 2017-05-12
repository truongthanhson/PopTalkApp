package com.poptech.poptalk.speakitem;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.collections.CollectionsActivity;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.sound.AudioController;
import com.poptech.poptalk.sound.NotificationCenter;
import com.poptech.poptalk.utils.AndroidUtilities;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.view.AudioTimelineView;
import com.poptech.poptalk.view.SeekBarWaveformView;

import java.util.List;

import javax.inject.Inject;

public class SpeakItemDetailFragment extends Fragment implements NotificationCenter.NotificationCenterDelegate, SpeakItemDetailContract.View, View.OnTouchListener, View.OnClickListener, AudioTimelineView.AudioTimelineDelegate, View.OnLongClickListener {

    public static SpeakItemDetailFragment newInstance(long speakItemId) {
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_SPEAK_ITEM_ID, speakItemId);
        SpeakItemDetailFragment fragment = new SpeakItemDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    SpeakItemDetailPresenter mPresenter;
    private View mView;
    private ViewPager mViewPager;
    private ScrollView mScrollView;

    private ImageView mPhotoView;
    private ImageButton mPhotoEdit;
    private TextView mPhotoLocation;
    private TextView mPhotoDateTime;
    private EditText mPhotoDescription;

    private TextView mTimerText;
    private ImageButton mRecordButton;
    private LinearLayout mSlideCancel;
    private RelativeLayout mRecordMenu;

    private FrameLayout mWaveformMenu;
    private SeekBarWaveformView mRecordWave;
    private AudioTimelineView mRecordTimeline;
    private ProgressBar mProgressBar;

    private LinearLayout mPlayMenu;
    private ImageButton mPlayCurrentButton;
    private ImageButton mPlayNextButton;

    private AudioController mAudioCtrl;
    private SpeakItem mSpeakItem;
    private long mSpeakItemId;
    private float mStartedDraggingX;
    private float mDistCanMove;
    private boolean mRecordPermission;
    private boolean mNextPlaying;
    private boolean mNextPausing;
    private boolean mCurrentPlaying;
    private boolean mCurrentPausing;
    private boolean mRecording;

    public SpeakItemDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerSpeakItemDetailComponent.builder().appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent()).
                speakItemDetailPresenterModule(new SpeakItemDetailPresenterModule(this)).build().inject(this);

        mSpeakItemId = getArguments().getLong(Constants.KEY_SPEAK_ITEM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_speak_items_detail_layout, container, false);
        initView();
        initData();
        return mView;
    }

    private void initView() {
        mViewPager = (ViewPager) getActivity().findViewById(R.id.speak_item_pager_id);
        mScrollView = (ScrollView) mView.findViewById(R.id.speak_item_scroll_id);

        // Photo
        mPhotoView = (ImageView) mView.findViewById(R.id.photo_img_id);
        mPhotoView.setOnLongClickListener(this);
        mPhotoEdit = (ImageButton) mView.findViewById(R.id.photo_edit_btn_id);
        mPhotoLocation = (TextView) mView.findViewById(R.id.photo_location_id);
        mPhotoDateTime = (TextView) mView.findViewById(R.id.photo_datetime_id);
        mPhotoDescription = (EditText) mView.findViewById(R.id.description_et_id);

        // Record
        mRecordMenu = (RelativeLayout) mView.findViewById(R.id.record_menu_rl_id);
        mRecordButton = (ImageButton) mView.findViewById(R.id.record_ll_id);
        mTimerText = (TextView) mView.findViewById(R.id.timer_tv_id);
        mSlideCancel = (LinearLayout) mView.findViewById(R.id.slide_cancel_ll_id);

        // Waveform
        mWaveformMenu = (FrameLayout) mView.findViewById(R.id.waveform_menu_id);
        mRecordWave = (SeekBarWaveformView) mView.findViewById(R.id.record_waveform_id);
        mRecordTimeline = (AudioTimelineView) mView.findViewById(R.id.record_timeline_id);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar_id);

        // Play
        mPlayMenu = (LinearLayout) mView.findViewById(R.id.play_menu_ll_id);
        mPlayCurrentButton = (ImageButton) mView.findViewById(R.id.play_current_button_id);
        mPlayNextButton = (ImageButton) mView.findViewById(R.id.play_next_button_id);

        mRecordButton.setOnTouchListener(this);
        mPlayCurrentButton.setOnClickListener(this);
        mPlayNextButton.setOnClickListener(this);
        mRecordTimeline.setDelegate(this);
    }

    private void initData() {
        mAudioCtrl = new AudioController(mSpeakItemId);
        mStartedDraggingX = -1;
        mDistCanMove = AndroidUtilities.dp(100);
        mRecordPermission = false;
        mNextPlaying = false;
        mNextPausing = false;
        mCurrentPlaying = false;
        mCurrentPausing = false;
        mRecording = false;
        setNotification();
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                mPresenter.loadSpeakItem(mSpeakItemId);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                super.onPermissionDenied(response);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permission, token);
            }
        }).check();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopPlay();
        clearNotification();
        mPresenter.updateSpeakItem(mSpeakItem);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (mView != null && mAudioCtrl != null && mSpeakItem != null) {
                stopPlay();
            }
        }
    }

    @Override
    public void setPresenter(SpeakItemDetailContract.Presenter presenter) {
        this.mPresenter = (SpeakItemDetailPresenter) presenter;
    }

    @Override
    public void onSpeakItemLoaded(SpeakItem speakItem) {
        mSpeakItem = speakItem;
        Glide.with(getActivity())
                .load(speakItem.getPhotoPath())
                .centerCrop()
                .thumbnail(0.5f)
                .placeholder(R.color.colorAccent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mPhotoView);

        mPhotoLocation.setText(speakItem.getLocation());
        mPhotoDateTime.setText(speakItem.getDateTime());

        // Reload marks
        mRecordTimeline.setProgressLeft(speakItem.getAudioLeftMark());
        mRecordTimeline.setProgressMiddle(speakItem.getAudioMiddleMark());
        mRecordTimeline.setProgressRight(speakItem.getAudioRightMark());

        // Reload waveform
        if (mAudioCtrl.generateWaveform(speakItem)) {
            mPlayMenu.setVisibility(View.VISIBLE);
            mWaveformMenu.setVisibility(View.VISIBLE);
        }
    }

    private void setNotification() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordCompleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioStartCompleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
    }

    private void clearNotification() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordCompleted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioStartCompleted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_current_button_id:
                playCurrent();
                break;
            case R.id.play_next_button_id:
                playNext();
                break;
            default:
                break;
        }
    }


    private void playCurrent() {
        if (!mCurrentPlaying) {
            mCurrentPlaying = true;
            mNextPlaying = false;
            setPlayView();
            if (!mCurrentPausing) {
                mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
                mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
            }
            mAudioCtrl.playAudio(mSpeakItem);
            mCurrentPausing = false;
        } else {
            mCurrentPlaying = false;
            mNextPlaying = false;
            setPlayView();
            mAudioCtrl.pauseAudio(mSpeakItem);
            mCurrentPausing = true;
        }
    }

    private void playNext() {
        if (!mNextPlaying) {
            mCurrentPlaying = false;
            mNextPlaying = true;
            setPlayView();
            if (!mNextPausing) {
                mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
                mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
            }
            mAudioCtrl.playAudio(mSpeakItem);
            mNextPausing = false;
        } else {
            mCurrentPlaying = false;
            mNextPlaying = false;
            setPlayView();
            mAudioCtrl.pauseAudio(mSpeakItem);
            mNextPausing = true;
        }
    }

    private void stopPlay() {
        mCurrentPlaying = false;
        mCurrentPausing = false;
        mNextPlaying = false;
        mNextPausing = false;
        mRecording = false;
        setPlayView();
        setRecordView();
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        mAudioCtrl.cleanupPlayer();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.record_ll_id) {
                    mViewPager.requestDisallowInterceptTouchEvent(true);
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    Dexter.withActivity(getActivity())
                            .withPermissions(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.VIBRATE,
                                    Manifest.permission.RECORD_AUDIO
                            ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                mRecordPermission = true;
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            mRecordPermission = false;
                        }
                    }).check();

                    if (mRecordPermission) {
                        mStartedDraggingX = -1;
                        mRecording = true;
                        setRecordView();
                        mSpeakItem.generateAudioPath();
                        mAudioCtrl.startRecording(mSpeakItem.getAudioPath());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.record_ll_id) {
                    mViewPager.requestDisallowInterceptTouchEvent(false);
                    mScrollView.requestDisallowInterceptTouchEvent(false);
                    if (mRecordPermission) {
                        mStartedDraggingX = -1;
                        mRecording = false;
                        setRecordView();
                        mAudioCtrl.stopRecording(1);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (v.getId() == R.id.record_ll_id) {
                    mViewPager.requestDisallowInterceptTouchEvent(true);
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    float x = event.getX();
                    x = x + mRecordButton.getX();
                    if ((x - mStartedDraggingX) < -mDistCanMove) {
                        mRecording = false;
                        setRecordView();
                        mAudioCtrl.stopRecording(0);
                        return true;
                    }
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSlideCancel.getLayoutParams();
                    if (mStartedDraggingX != -1) {
                        float dist = (x - mStartedDraggingX);
                        params.leftMargin = AndroidUtilities.dp(100) + (int) dist;
                        mSlideCancel.setLayoutParams(params);
                        mRecordButton.setTranslationX(dist);
                        float alpha = 1.0f + dist / mDistCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        mSlideCancel.setAlpha(alpha);
                    }
                    if (x <= mSlideCancel.getX() + mSlideCancel.getWidth() + (int) ((mRecordMenu.getMeasuredWidth() - mSlideCancel.getMeasuredWidth()) / 2.0f)) {
                        if (mStartedDraggingX == -1) {
                            mStartedDraggingX = x;
                            mDistCanMove = mRecordMenu.getMeasuredWidth() / 2.0f;
                            if (mDistCanMove <= 0) {
                                mDistCanMove = AndroidUtilities.dp(100);
                            } else if (mDistCanMove > AndroidUtilities.dp(100)) {
                                mDistCanMove = AndroidUtilities.dp(100);
                            }
                        }
                    }
                    if (params.leftMargin > AndroidUtilities.dp(100)) {
                        params.leftMargin = AndroidUtilities.dp(100);
                        mRecordButton.setTranslationX(0);
                        mSlideCancel.setLayoutParams(params);
                        mSlideCancel.setAlpha(1);
                        mStartedDraggingX = -1;
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private void setRecordView() {
        if (mRecording) {
            mRecordButton.setPressed(true);
            mPlayMenu.setVisibility(View.GONE);
            mTimerText.setText("00:00.00");
            mSlideCancel.setVisibility(View.VISIBLE);
            mTimerText.setVisibility(View.VISIBLE);
            mRecordButton.setTranslationX(0);
            Utils.scaleView(mRecordButton, 1.3f, 1.3f);
        } else {
            mRecordButton.setPressed(false);
            mSlideCancel.setAlpha(1.0f);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSlideCancel.getLayoutParams();
            params.leftMargin = AndroidUtilities.dp(100);
            mSlideCancel.setLayoutParams(params);
            mSlideCancel.setVisibility(View.GONE);
            mTimerText.setVisibility(View.GONE);
            mRecordButton.setTranslationX(0);
            Utils.scaleView(mRecordButton, 1.0f, 1.0f);
        }
    }

    private void setPlayView() {
        if (mCurrentPlaying) {
            mPlayCurrentButton.setSelected(true);
        } else {
            mPlayCurrentButton.setSelected(false);
        }
        if (mNextPlaying) {
            mPlayNextButton.setSelected(true);
        } else {
            mPlayNextButton.setSelected(false);
        }
        mTimerText.setText("00:00");
        mTimerText.setVisibility(View.VISIBLE);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        long audioId = (Long) args[0];
        if (audioId == mSpeakItemId) {
            if (id == NotificationCenter.recordProgressChanged) {
                onRecordProgressChanged((Long) args[1]);
            } else if (id == NotificationCenter.recordStartError || id == NotificationCenter.recordStopped) {
                onRecordStopped();
            } else if (id == NotificationCenter.recordStarted) {
                onRecordStarted();
            } else if (id == NotificationCenter.recordCompleted) {
                onRecordCompleted();
            } else if (id == NotificationCenter.audioDidSent) {
                onAudioDidSent((SpeakItem) args[1]);
            } else if (id == NotificationCenter.audioDidReset) {
                onAudioDidReset();
            } else if (id == NotificationCenter.audioProgressDidChanged) {
                onAudioProgressDidChanged((float) args[1], (int) args[2]);
            } else if (id == NotificationCenter.audioStartCompleted) {
                onAudioStartCompleted();
            }
        }
    }

    private void onAudioStartCompleted() {
        if (mSpeakItem != null && !mAudioCtrl.isPlayingAudio(mSpeakItem)) {
            if (mCurrentPlaying) {
                mRecordWave.setLeftProgress(mSpeakItem.getAudioMiddleMark());
            } else if (mNextPlaying) {
                mRecordWave.setLeftProgress(mSpeakItem.getAudioRightMark());
            }
            mNextPlaying = false;
            mCurrentPlaying = false;
            setPlayView();
        }
    }

    private void onAudioProgressDidChanged(float progress, int progressSec) {
        if (mSpeakItem != null && mAudioCtrl.isPlayingAudio(mSpeakItem)) {
            if (mCurrentPlaying && (progress >= mSpeakItem.getAudioMiddleMark())) {
                mAudioCtrl.stopAudio();
                return;
            } else if (mNextPlaying && (progress >= mSpeakItem.getAudioRightMark())) {
                mAudioCtrl.stopAudio();
                return;
            }
            mSpeakItem.setAudioProgress(progress);
            mSpeakItem.setAudioProgressSec(progressSec);
            mRecordWave.setLeftProgress(mSpeakItem.getAudioProgress());
            String timeString = String.format("%02d:%02d", mSpeakItem.getAudioProgressSec() / 60, mSpeakItem.getAudioProgressSec() % 60);
            mTimerText.setText(timeString);
        }
    }

    private void onAudioDidReset() {
        if (mSpeakItem != null && !mAudioCtrl.isPlayingAudio(mSpeakItem)) {
            mCurrentPlaying = false;
            mNextPlaying = false;
            setPlayView();
            mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
            mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        }
    }

    private void onAudioDidSent(SpeakItem speakItem) {
        mSpeakItem.setAudioPath(speakItem.getAudioPath());
        mSpeakItem.setAudioDuration(speakItem.getAudioDuration());
        mSpeakItem.setAudioWaveform(speakItem.getAudioWaveform());
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        mRecordWave.setWaveform(mSpeakItem.getAudioWaveform());
        mRecordWave.setVisibility(View.VISIBLE);
        mRecordTimeline.setVisibility(View.VISIBLE);
    }

    private void onRecordStarted() {
        mRecording = true;
        setRecordView();
    }

    private void onRecordProgressChanged(long t) {
        Long time = t / 1000;
        int ms = (int) (t % 1000L) / 10;
        mTimerText.setText(String.format("%02d:%02d.%02d", time / 60, time % 60, ms));
    }

    private void onRecordStopped() {
        mTimerText.setText("00:00.00");
        if (mSpeakItem.getAudioWaveform().length > 0) {
            mPlayMenu.setVisibility(View.VISIBLE);
        }
    }

    private void onRecordCompleted() {
        mTimerText.setText("00:00.00");
        mWaveformMenu.setVisibility(View.VISIBLE);
        mPlayMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLeftProgressChanged(float progress) {
        mNextPausing = false;
        mCurrentPausing = false;
        mSpeakItem.setAudioLeftMark(progress);
        if (mCurrentPlaying || mNextPlaying) {
            mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
            mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
        }
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
    }

    @Override
    public void onMiddleProgressChanged(float progress) {
        mSpeakItem.setAudioMiddleMark(progress);
        if (!mCurrentPlaying && !mNextPlaying) {
            mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
            mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        }

    }

    @Override
    public void onRightProgressChanged(float progress) {
        mSpeakItem.setAudioRightMark(progress);
        if (!mCurrentPlaying && !mNextPlaying) {
            mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        }
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.photo_img_id:
                showChoosePhotoDialog();
                break;
            default:
                break;
        }
        return false;
    }

    private void showChoosePhotoDialog() {
        CharSequence options[] = new CharSequence[]{"Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pickup Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Dexter.withActivity(getActivity())
                            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new BaseMultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            super.onPermissionsChecked(report);
                            if (report.areAllPermissionsGranted()) {
                                Intent intent = new Intent(getActivity(), GalleryActivity.class);
                                intent.putExtra(Constants.KEY_PHOTO_GALLERY, GalleryActivity.GalleryType.PICK_GALLERY_PHOTO);
                                startActivityForResult(intent, GalleryActivity.SELECT_PHOTO_REQUEST_CODE);
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            super.onPermissionRationaleShouldBeShown(permissions, token);
                        }
                    }).check();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
