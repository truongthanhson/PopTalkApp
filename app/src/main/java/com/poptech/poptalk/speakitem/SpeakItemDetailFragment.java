package com.poptech.poptalk.speakitem;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.sound.AudioController;
import com.poptech.poptalk.sound.NotificationCenter;
import com.poptech.poptalk.utils.AndroidUtilities;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.view.SeekBarWaveformView;

import java.util.List;

import javax.inject.Inject;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class SpeakItemDetailFragment extends Fragment implements NotificationCenter.NotificationCenterDelegate, SpeakItemDetailContract.View, View.OnTouchListener {
    public static SpeakItemDetailFragment newInstance(int speakItemId) {
        Bundle args = new Bundle();
        args.putInt("speak_item_id", speakItemId);
        SpeakItemDetailFragment fragment = new SpeakItemDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    SpeakItemDetailPresenter mPresenter;
    private View mView;
    private ImageView mPhotoView;
    private ImageButton mPhotoEdit;
    private TextView mPhotoLocation;
    private TextView mPhotoDateTime;
    private EditText mPhotoDescription;
    private TextView mLanguageButton;
    private TextView mTimerText;
    private FrameLayout mRecordButton;
    private LinearLayout mSlideCancel;
    private RelativeLayout mRecordMenu;
    private SeekBarWaveformView mRecordWave;

    private SpeakItem mSpeakItem;
    private int mSpeakItemId = -1;
    private float mStartedDraggingX = -1;
    private float mDistCanMove = AndroidUtilities.dp(100);

    public SpeakItemDetailFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the presenter
        DaggerSpeakItemDetailComponent.builder().appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent()).
                speakItemDetailPresenterModule(new SpeakItemDetailPresenterModule(this)).build().inject(this);

        mSpeakItemId = getArguments().getInt("speak_item_id");
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
        mPhotoView = (ImageView) mView.findViewById(R.id.photo_img_id);
        mPhotoEdit = (ImageButton) mView.findViewById(R.id.photo_edit_btn_id);
        mPhotoLocation = (TextView) mView.findViewById(R.id.photo_location_id);
        mPhotoDateTime = (TextView) mView.findViewById(R.id.photo_datetime_id);
        mPhotoDescription = (EditText) mView.findViewById(R.id.description_et_id);
        mLanguageButton = (TextView) mView.findViewById(R.id.language_button_id);
        mRecordMenu = (RelativeLayout) mView.findViewById(R.id.record_menu_rl_id);
        mRecordButton = (FrameLayout) mView.findViewById(R.id.record_fl_id);
        mRecordButton.setOnTouchListener(this);
        mTimerText = (TextView) mView.findViewById(R.id.timer_tv_id);
        mSlideCancel = (LinearLayout) mView.findViewById(R.id.slide_cancel_ll_id);
        mRecordWave = (SeekBarWaveformView) mView.findViewById(R.id.record_waveform_id);
    }

    private void initData() {
        setNotification();
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
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    mPresenter.loadSpeakItem(mSpeakItemId);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            }
        }).check();
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
        clearNotification();
        super.onDestroy();
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
                .placeholder(R.color.colorAccent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mPhotoView);

        mPhotoLocation.setText(speakItem.getLocation());
        mPhotoDateTime.setText(speakItem.getDateTime());
    }

    private void setNotification() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recordStartCompleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
    }

    private void clearNotification() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStarted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStartError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStopped);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordProgressChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recordStartCompleted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidSent);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioDidReset);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "ID: " + v.getId() + "Event: " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.record_fl_id) {
                    mStartedDraggingX = -1;
                    setRecordUIStatus(true);
                    mSpeakItem.generateAudioPath();
                    AudioController.getInstance().startRecording(mSpeakItem.getAudioPath());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.record_fl_id) {
                    mStartedDraggingX = -1;
                    setRecordUIStatus(false);
                    AudioController.getInstance().stopRecording(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (v.getId() == R.id.record_fl_id) {
                    float x = event.getX();
                    x = x + mRecordButton.getX();
                    if ((x - mStartedDraggingX) < -mDistCanMove) {
                        setRecordUIStatus(false);
                        AudioController.getInstance().stopRecording(0);
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

    private void setRecordUIStatus(boolean isRecording) {
        if (isRecording) {
            mView.findViewById(R.id.play_ll_id).setVisibility(View.GONE);
            mView.findViewById(R.id.record_normal_fl_id).setVisibility(View.GONE);
            mView.findViewById(R.id.record_press_fl_id).setVisibility(View.VISIBLE);
            mSlideCancel.setVisibility(View.VISIBLE);
            mTimerText.setVisibility(View.VISIBLE);
            mTimerText.setText("00:00.00");
            mRecordButton.setTranslationX(0);
            Utils.scaleView(mRecordButton, 1.3f, 1.3f);
        } else {
            mView.findViewById(R.id.play_ll_id).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.record_normal_fl_id).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.record_press_fl_id).setVisibility(View.GONE);
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

    private void setPlayFirstUIStatus(boolean isPlaying) {
        if (isPlaying) {
            mView.findViewById(R.id.play_first_press_fl_id).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.play_first_normal_fl_id).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.play_first_press_fl_id).setVisibility(View.GONE);
            mView.findViewById(R.id.play_first_normal_fl_id).setVisibility(View.VISIBLE);
        }
    }

    private void setPlaySecondUIStatus(boolean isPlaying) {
        if (isPlaying) {
            mView.findViewById(R.id.play_second_press_fl_id).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.play_second_normal_fl_id).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.play_second_press_fl_id).setVisibility(View.GONE);
            mView.findViewById(R.id.play_second_normal_fl_id).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.recordProgressChanged) {
            long t = (Long) args[0];
            Long time = t / 1000;
            int ms = (int) (t % 1000L) / 10;
            mTimerText.setText(String.format("%02d:%02d.%02d", time / 60, time % 60, ms));
        } else if (id == NotificationCenter.recordStartError || id == NotificationCenter.recordStopped) {
            mTimerText.setText("00:00.00");
        } else if (id == NotificationCenter.recordStarted) {
            setRecordUIStatus(true);
        } else if (id == NotificationCenter.audioDidSent) {
            mSpeakItem = (SpeakItem) args[0];
            mRecordWave.setVisibility(View.VISIBLE);
            mView.findViewById(R.id.waveform_menu_id).setVisibility(View.VISIBLE);
            mRecordWave.setWaveform(mSpeakItem.getAudioWaveform());
        } else if (id == NotificationCenter.audioDidReset) {
            if (mSpeakItem != null && !AudioController.getInstance().isPlayingAudio(mSpeakItem)) {
                mView.findViewById(R.id.play_first_press_fl_id).setVisibility(View.GONE);
                mView.findViewById(R.id.play_first_normal_fl_id).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.play_second_press_fl_id).setVisibility(View.GONE);
                mView.findViewById(R.id.play_second_normal_fl_id).setVisibility(View.VISIBLE);
                mRecordWave.setProgress(0);
            }
        } else if (id == NotificationCenter.audioProgressDidChanged) {
            if (mSpeakItem != null && AudioController.getInstance().isPlayingAudio(mSpeakItem)) {
                SpeakItem audioPlaying = AudioController.getInstance().getPlayingAudioItem();
                mSpeakItem.setAudioProgress(audioPlaying.getAudioProgress());
                mSpeakItem.setAudioProgressSec(audioPlaying.getAudioProgressSec());
                mRecordWave.setProgress(mSpeakItem.getAudioProgress());
                String timeString = String.format("%d:%02d", mSpeakItem.getAudioProgressSec() / 60, mSpeakItem.getAudioProgressSec() % 60);
                mTimerText.setText(timeString);
            }
        } else if (id == NotificationCenter.recordStartCompleted) {
            if (mSpeakItem != null && !AudioController.getInstance().isPlayingAudio(mSpeakItem)) {
                mRecordWave.setProgress(1.0f);
                setPlayFirstUIStatus(false);
                setPlaySecondUIStatus(false);
            }

        }
    }
}
