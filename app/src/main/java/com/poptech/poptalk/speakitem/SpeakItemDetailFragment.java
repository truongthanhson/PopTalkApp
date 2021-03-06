package com.poptech.poptalk.speakitem;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
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
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.location.LocationTask;
import com.poptech.poptalk.maps.MapActivity;
import com.poptech.poptalk.sound.AudioController;
import com.poptech.poptalk.sound.NotificationCenter;
import com.poptech.poptalk.utils.AndroidUtilities;
import com.poptech.poptalk.utils.AnimationUtils;
import com.poptech.poptalk.utils.SaveData;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.view.AudioTimelineView;
import com.poptech.poptalk.view.SeekBarWaveformView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class SpeakItemDetailFragment extends Fragment implements NotificationCenter.NotificationCenterDelegate, SpeakItemDetailContract.View, View.OnTouchListener, View.OnClickListener, AudioTimelineView.AudioTimelineDelegate, View.OnLongClickListener, TextWatcher {
    public enum PlayState {
        NONE,
        CURRENT,
        CURRENT_PAUSE,
        NEXT,
        NEXT_PAUSE,
        REPEAT,
        REPEAT_PAUSE
    }

    public interface SpeakItemDetailFragmentCallback {
        void onClickSpeakItemDialog(SpeakItem speakItem);
    }

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
    private Button mLanguage1;
    private Button mLanguage2;
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
    private ImageButton mPlayRepeatButton;
    private ImageButton mAudioCutButton;

    private AudioController mAudioCtrl;
    private SpeakItem mSpeakItem;
    private long mSpeakItemId;
    private float mStartedDraggingX;
    private float mDistCanMove;
    private boolean mRecordPermission;
    private boolean mRecording;
    private boolean mLanguage1Enable;
    private boolean mLanguage2Enable;
    private PlayState mPlayState;

    private SpeakItemDetailFragmentCallback mCallback;

    public SpeakItemDetailFragment() {
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpeakItemDetailFragmentCallback) {
            mCallback = (SpeakItemDetailFragmentCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerSpeakItemDetailComponent.builder().appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent()).
                speakItemDetailPresenterModule(new SpeakItemDetailPresenterModule(this)).build().inject(this);

        mSpeakItemId = getArguments().getLong(Constants.KEY_SPEAK_ITEM_ID);
    }

    /**
     * Adds a listener to the layout {@link ViewTreeObserver} to wait for the view measurements
     * and then calls {@link #consolideHeights(LinearLayout)}.
     */
    private static void scheduleConsolideHeights(final LinearLayout layout) {

        final ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();

        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    consolideHeights(layout);
                }
            });
        }
    }

    /**
     * Note: you might want to use {@link #scheduleConsolideHeights(android.widget.LinearLayout)}.
     * <p>
     * Takes a {@link android.widget.LinearLayout} and, for each of its child views, sets the height
     * of the child's LayoutParams to the current height of the child.
     * <p>
     * Useful when a {@link android.widget.LinearLayout} that has relative heights (with weights)
     * may display the keyboard. With absolute heights, the layout maintains its aspect when the soft
     * keyboard appears.
     * <p>
     * See: http://stackoverflow.com/q/22534107/1121497
     */
    private static void consolideHeights(LinearLayout layout) {

        for (int i = 0; i < layout.getChildCount(); i++) {
            final View child = layout.getChildAt(i);
            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
            params.height = child.getHeight();
            params.weight = 0;
            child.setLayoutParams(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_speak_items_detail_layout, container, false);
        initView();
        initData();
        scheduleConsolideHeights((LinearLayout) mView.findViewById(R.id.root));
        return mView;
    }

    private void initView() {
        mViewPager = (ViewPager) getActivity().findViewById(R.id.speak_item_pager_id);
        mScrollView = (ScrollView) mView.findViewById(R.id.speak_item_scroll_id);

        // Photo
        mPhotoView = (ImageView) mView.findViewById(R.id.photo_img_id);
        mPhotoView.setOnLongClickListener(this);
        mPhotoView.setOnClickListener(this);

        mPhotoEdit = (ImageButton) mView.findViewById(R.id.photo_edit_btn_id);
        mPhotoLocation = (TextView) mView.findViewById(R.id.photo_location_id);
        mPhotoDateTime = (TextView) mView.findViewById(R.id.photo_datetime_id);
        mLanguage1 = (Button) mView.findViewById(R.id.language1_button_id);
        mLanguage1.setOnClickListener(this);
        mLanguage1.setOnLongClickListener(this);
        mLanguage2 = (Button) mView.findViewById(R.id.language2_button_id);
        mLanguage2.setOnClickListener(this);
        mLanguage2.setOnLongClickListener(this);
        mPhotoDescription = (EditText) mView.findViewById(R.id.description_et_id);
        mPhotoDescription.addTextChangedListener(this);

        // Record
        mRecordMenu = (RelativeLayout) mView.findViewById(R.id.record_menu_rl_id);
        mRecordButton = (ImageButton) mView.findViewById(R.id.record_ll_id);
        mRecordButton.setOnTouchListener(this);
        mTimerText = (TextView) mView.findViewById(R.id.timer_tv_id);
        mSlideCancel = (LinearLayout) mView.findViewById(R.id.slide_cancel_ll_id);

        // Waveform
        mWaveformMenu = (FrameLayout) mView.findViewById(R.id.waveform_menu_id);
        mRecordWave = (SeekBarWaveformView) mView.findViewById(R.id.record_waveform_id);
        mRecordTimeline = (AudioTimelineView) mView.findViewById(R.id.record_timeline_id);
        mRecordTimeline.setDelegate(this);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar_id);

        // Play
        mPlayMenu = (LinearLayout) mView.findViewById(R.id.play_menu_ll_id);
        mPlayCurrentButton = (ImageButton) mView.findViewById(R.id.play_current_button_id);
        mPlayCurrentButton.setOnClickListener(this);
        mPlayNextButton = (ImageButton) mView.findViewById(R.id.play_next_button_id);
        mPlayNextButton.setOnClickListener(this);
        mPlayRepeatButton = (ImageButton) mView.findViewById(R.id.play_repeat_button_id);
        mPlayRepeatButton.setOnClickListener(this);
        mAudioCutButton = (ImageButton) mView.findViewById(R.id.audio_cut_button_id);
        mAudioCutButton.setOnClickListener(this);


    }

    private void initData() {
        mLanguage1Enable = true;
        mLanguage2Enable = false;
        mAudioCtrl = new AudioController(mSpeakItemId);
        mStartedDraggingX = -1;
        mDistCanMove = AndroidUtilities.dp(100);
        mRecordPermission = false;
        mPlayState = PlayState.NONE;
        mRecording = false;
        setNotification();
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                mPresenter.loadSpeakItem(mSpeakItemId);
                mPresenter.loadCollection(mSpeakItem.getCollectionId());
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
        mPresenter.updateSpeakItem(mSpeakItem);
        super.onPause();
    }

    @Override
    public void onStop() {
        cleanPlay();
        clearNotification();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (mView != null && mAudioCtrl != null && mSpeakItem != null) {
                cleanPlay();
            }
        }
    }

    @Override
    public void setPresenter(SpeakItemDetailContract.Presenter presenter) {
        this.mPresenter = (SpeakItemDetailPresenter) presenter;
    }

    @Override
    public void onSpeakItemLoaded(SpeakItem speakItem) {
        mPresenter.loadCollection(speakItem.getCollectionId());

        mSpeakItem = speakItem;
        onReloadPhotoView();
        onReloadPhotoAttribute();
        onReloadAudioWave();
    }

    @Override
    public void onCollectionLoaded(Collection collection) {
        if (!StringUtils.isNullOrEmpty(collection.getDescription())) {
            ((SpeakItemDetailActivity) getActivity()).getSupportActionBar().setTitle(collection.getDescription());
        }
    }

    private void onReloadPhotoView() {
        Glide.with(getActivity())
                .load(mSpeakItem.getPhotoPath())
                .centerCrop()
                .thumbnail(0.5f)
                .placeholder(R.color.colorAccent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mPhotoView);
    }

    private void onReloadPhotoAttribute() {
        if (!StringUtils.isNullOrEmpty(mSpeakItem.getLanguage())) {
            mLanguage1.setText(mSpeakItem.getLanguage());
        }
        if (!StringUtils.isNullOrEmpty(SaveData.getInstance(getActivity()).getLanguage())) {
            mLanguage2.setText(SaveData.getInstance(getActivity()).getLanguage());
        }
        mPhotoDateTime.setText(mSpeakItem.getDateTime());
        mPhotoLocation.setText(mSpeakItem.getLocation());
        if (mSpeakItem.getLatitude() != 0 || mSpeakItem.getLongitude() != 0) {
            Location location = new Location("");
            location.setLatitude(mSpeakItem.getLatitude());
            location.setLongitude(mSpeakItem.getLongitude());
            LocationTask task = new LocationTask(getActivity());
            task.setListener(new LocationTask.onGetAddressTaskListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(Address address) {
                    if (address != null) {
                        String location = getLocation(address);
                        if (!StringUtils.isNullOrEmpty(location)) {
                            mSpeakItem.setLocation(location);
                            mPhotoLocation.setText(location);
                        }
                        String language = getLanguage(address);
                        if (!StringUtils.isNullOrEmpty(language)) {
                            mSpeakItem.setLanguage(language);
                            mLanguage1.setText(language);
                        }
                    }
                }
            });
            task.execute(location);
        }
        mPhotoDescription.setText(mSpeakItem.getDescription1());
    }

    private String getLocation(Address address) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> stringList = new ArrayList<>();

        if (!StringUtils.isNullOrEmpty(address.getLocality())) {
            stringList.add(address.getLocality());
        } else if (!StringUtils.isNullOrEmpty(address.getAdminArea())) {
            stringList.add(address.getAdminArea());
        }
        if (!StringUtils.isNullOrEmpty(address.getCountryName())) {
            stringList.add(address.getCountryName());
        }
        for (int i = 0; i < stringList.size(); i++) {
            stringBuilder.append(stringList.get(i));
            if (i < stringList.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        // Return the text
        return stringBuilder.toString();
    }

    private String getLanguage(Address address) {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (locale.getCountry().equalsIgnoreCase(address.getCountryCode())) {
                String language = locale.getDisplayLanguage();
                if (!StringUtils.isNullOrEmpty(language)) {
                    String[] regexChars = {"\\s+", "\\s*-\\s*", "\\s*'\\s*"};
                    String space = " ";
                    for (String regex : regexChars) {
                        language = language.replaceAll(regex, space);
                    }
                    language = language.replaceAll("^\\s+", "");
                    language = language.replaceAll("\\s+$", "");
                    return language;
                }
            }
        }
        return "";
    }


    private void onReloadAudioWave() {
        // Reload marks
        mRecordTimeline.setProgressLeft(mSpeakItem.getAudioLeftMark());
        mRecordTimeline.setProgressMiddle(mSpeakItem.getAudioMiddleMark());
        mRecordTimeline.setProgressRight(mSpeakItem.getAudioRightMark());

        // Reload waveform
        if (mAudioCtrl.generateWaveform(mSpeakItem)) {
            mPlayMenu.setVisibility(View.VISIBLE);
            mWaveformMenu.setVisibility(View.VISIBLE);
            mTimerText.setVisibility(View.VISIBLE);
            Long time = mSpeakItem.getAudioDuration() / 1000;
            mTimerText.setText(String.format("%02d:%02d", time / 60, time % 60));
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
            case R.id.play_repeat_button_id:
                playRepeat();
                break;
            case R.id.audio_cut_button_id:
                showAudioCuttingDialog();
                break;
            case R.id.photo_img_id:
                showMap();
                break;
            case R.id.language1_button_id:
                if (StringUtils.isNullOrEmpty(mSpeakItem.getLanguage())) {
                    mPhotoDescription.setError(getString(R.string.description_error_language));
                    AnimationUtils.shake(getActivity().getApplicationContext(), mPhotoDescription);
                } else {
                    mLanguage1Enable = true;
                    mLanguage2Enable = false;
                    mPhotoDescription.setText(mSpeakItem.getDescription1());
                }
                break;
            case R.id.language2_button_id:
                if (StringUtils.isNullOrEmpty(SaveData.getInstance(getActivity()).getLanguage())) {
                    mPhotoDescription.setError(getString(R.string.description_error_language));
                    AnimationUtils.shake(getActivity().getApplicationContext(), mPhotoDescription);
                } else {
                    mLanguage1Enable = false;
                    mLanguage2Enable = true;
                    mPhotoDescription.setText(mSpeakItem.getDescription2());
                }
                break;
            default:
                break;
        }
    }

    private void showMap() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("lat", mSpeakItem.getLatitude());
        intent.putExtra("long", mSpeakItem.getLongitude());
        startActivity(intent);
    }


    private void playCurrent() {
        if(mAudioCtrl != null) {
            if (mPlayState != PlayState.CURRENT) {
                if (mPlayState != PlayState.CURRENT_PAUSE) {
                    stopPlay();
                    mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
                    mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
                }
                mAudioCtrl.playAudio(mSpeakItem);
                mPlayState = PlayState.CURRENT;
                setPlayView();
            } else {
                mAudioCtrl.pauseAudio(mSpeakItem);
                mPlayState = PlayState.CURRENT_PAUSE;
                setPlayView();
            }
        }
    }

    private void playNext() {
        if(mAudioCtrl != null) {
            if (mPlayState != PlayState.NEXT) {
                if (mPlayState != PlayState.NEXT_PAUSE) {
                    stopPlay();
                    mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
                    mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
                }
                mAudioCtrl.playAudio(mSpeakItem);
                mPlayState = PlayState.NEXT;
                setPlayView();
            } else {
                mAudioCtrl.pauseAudio(mSpeakItem);
                mPlayState = PlayState.NEXT_PAUSE;
                setPlayView();
            }
        }
    }


    private void playRepeat() {
        if(mAudioCtrl != null) {
            if (mPlayState != PlayState.REPEAT) {
                if (mPlayState != PlayState.REPEAT_PAUSE) {
                    stopPlay();
                    mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
                    mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
                }
                mAudioCtrl.playAudio(mSpeakItem);
                mPlayState = PlayState.REPEAT;
                setPlayView();
            } else {
                mAudioCtrl.pauseAudio(mSpeakItem);
                mPlayState = PlayState.REPEAT_PAUSE;
                setPlayView();
            }
        }
    }

    private void cleanPlay() {
        mPlayState = PlayState.NONE;
        mRecording = false;
        setPlayView();
        setRecordView();
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        if(mAudioCtrl != null) {
            mAudioCtrl.cleanupPlayer();
        }
    }

    private void stopPlay() {
        if (mAudioCtrl != null) {
            mAudioCtrl.stopAudio();
        }
        mPlayState = PlayState.NONE;
        setPlayView();
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
                        // Pause Audio
                        switch (mPlayState) {
                            case CURRENT:
                                mPlayState = PlayState.CURRENT_PAUSE;
                                break;
                            case NEXT:
                                mPlayState = PlayState.NEXT_PAUSE;
                                break;
                            case REPEAT:
                                mPlayState = PlayState.REPEAT_PAUSE;
                                break;
                        }
                        setPlayView();
                        mStartedDraggingX = -1;
                        mRecording = true;
                        setRecordView();
                        String audioPath = Environment.getExternalStorageDirectory() +
                                Constants.PATH_APP + "/" +
                                Constants.PATH_AUDIO + "/" +
                                mSpeakItemId + "_" + System.currentTimeMillis() + "_record.ogg";
                        mAudioCtrl.startRecording(audioPath);
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
            mRecordButton.setTranslationX(0);
            Utils.scaleView(mRecordButton, 1.0f, 1.0f);
        }
    }

    private void setPlayView() {
        if (mPlayState == PlayState.CURRENT) {
            mPlayCurrentButton.setSelected(true);
        } else {
            mPlayCurrentButton.setSelected(false);
        }
        if (mPlayState == PlayState.NEXT) {
            mPlayNextButton.setSelected(true);
        } else {
            mPlayNextButton.setSelected(false);
        }
        if (mPlayState == PlayState.REPEAT) {
            mPlayRepeatButton.setSelected(true);
        } else {
            mPlayRepeatButton.setSelected(false);
        }
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
            if (mPlayState == PlayState.CURRENT) {
                mRecordWave.setLeftProgress(mSpeakItem.getAudioMiddleMark());
                mPlayState = PlayState.NONE;
                setPlayView();
            } else if (mPlayState == PlayState.NEXT) {
                mRecordWave.setLeftProgress(mSpeakItem.getAudioRightMark());
                mPlayState = PlayState.NONE;
                setPlayView();
            } else if (mPlayState == PlayState.REPEAT) {
                mRecordWave.setLeftProgress(mSpeakItem.getAudioRightMark());
                mPlayState = PlayState.NONE;
                setPlayView();
                playRepeat();
            }
        }
    }

    private void onAudioProgressDidChanged(float progress, int progressSec) {
        if (mSpeakItem != null && mAudioCtrl.isPlayingAudio(mSpeakItem)) {
            if ((mPlayState == PlayState.CURRENT) && (progress >= mSpeakItem.getAudioMiddleMark())) {
                mAudioCtrl.stopAudio();
                return;
            } else if ((mPlayState == PlayState.NEXT) && (progress >= mSpeakItem.getAudioRightMark())) {
                mAudioCtrl.stopAudio();
                return;
            } else if ((mPlayState == PlayState.REPEAT) && (progress >= mSpeakItem.getAudioRightMark())) {
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
            mPlayState = PlayState.NONE;
            setPlayView();
            mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
            mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        }
    }

    private void onAudioDidSent(SpeakItem speakItem) {
        cleanPlay();
        mSpeakItem.setAudioPath(speakItem.getAudioPath());
        mSpeakItem.setAudioDuration(speakItem.getAudioDuration());
        mSpeakItem.setAudioWaveform(speakItem.getAudioWaveform());
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        mRecordWave.setWaveform(mSpeakItem.getAudioWaveform());
        mRecordWave.setVisibility(View.VISIBLE);
        mRecordTimeline.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        Long time = mSpeakItem.getAudioDuration() / 1000;
        mTimerText.setText(String.format("%02d:%02d", time / 60, time % 60));
    }

    private void onRecordStarted() {
        //mRecording = true;
        //setRecordView();
    }

    private void onRecordProgressChanged(long t) {
        Long time = t / 1000;
        int ms = (int) (t % 1000L) / 10;
        mTimerText.setText(String.format("%02d:%02d.%02d", time / 60, time % 60, ms));
    }

    private void onRecordStopped() {
        mTimerText.setText("00:00.00");
        if (mSpeakItem.getAudioWaveform() != null && mSpeakItem.getAudioWaveform().length > 0) {
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
        stopPlay();
        mSpeakItem.setAudioLeftMark(progress);
        if ((mPlayState == PlayState.CURRENT) || (mPlayState == PlayState.NEXT) || (mPlayState == PlayState.REPEAT)) {
            mSpeakItem.setAudioProgress(mSpeakItem.getAudioLeftMark());
            //mAudioCtrl.seekToProgress(mSpeakItem, mSpeakItem.getAudioLeftMark());
        }
        mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
        mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
    }

    @Override
    public void onMiddleProgressChanged(float progress) {
        stopPlay();
        mSpeakItem.setAudioMiddleMark(progress);
        if ((mPlayState != PlayState.CURRENT) && (mPlayState != PlayState.NEXT) && (mPlayState != PlayState.REPEAT)) {
            mRecordWave.setLeftProgress(mSpeakItem.getAudioLeftMark());
            mRecordWave.setRightProgress(mSpeakItem.getAudioRightMark());
        }

    }

    @Override
    public void onRightProgressChanged(float progress) {
        stopPlay();
        mSpeakItem.setAudioRightMark(progress);
        if ((mPlayState != PlayState.CURRENT) && (mPlayState != PlayState.NEXT) && (mPlayState != PlayState.REPEAT)) {
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
            case R.id.language1_button_id:
            case R.id.language2_button_id:
                showChooseLanguageDialog(v);
                break;
            default:
                break;
        }
        return false;
    }

    private void showChoosePhotoDialog() {
        CharSequence options[] = new CharSequence[]{"Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.PopTalk_AlertDialog);
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
                                intent.putExtra(Constants.KEY_PHOTO_GALLERY, Constants.GALLERY_RESULT_PICK_PHOTO);
                                startActivityForResult(intent, Constants.REQUEST_GALLERY_CAPTURE);
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

    private void showChooseLanguageDialog(View v) {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> languages = new ArrayList<>();
        for (Locale l : locales) {
            String language = l.getDisplayLanguage();
            if (!StringUtils.isNullOrEmpty(language)) {
                String[] regexChars = {"\\s+", "\\s*-\\s*", "\\s*'\\s*"};
                String space = " ";
                for (String regex : regexChars) {
                    language = language.replaceAll(regex, space);
                }
                language = language.replaceAll("^\\s+", "");
                language = language.replaceAll("\\s+$", "");
                languages.add(language);
            }

        }
        List<String> sortedLanguages = new ArrayList<>(new HashSet<>(languages));
        Collections.sort(sortedLanguages, String.CASE_INSENSITIVE_ORDER);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.PopTalk_AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_listview_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Select Language");
        alertDialog.setIcon(R.drawable.ic_language);
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        final AlertDialog dialog = alertDialog.create();

        ListView listView = (ListView) convertView.findViewById(R.id.list_view_id);
        EditText searchText = (EditText) convertView.findViewById(R.id.search_id);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_listview_dialog_layout, R.id.row_id, sortedLanguages);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String language = (String) parent.getItemAtPosition(position);
                ((Button) v).setText(language);
                if (v.getId() == R.id.language1_button_id) {
                    mSpeakItem.setLanguage(language);
                } else if (v.getId() == R.id.language2_button_id) {
                    SaveData.getInstance(getActivity()).setLanguage(language);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void showAudioCuttingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.PopTalk_AlertDialog);
        builder.setTitle("Audio Cutting");
        builder.setMessage("Do you want to cut audio from A to C position?");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dexter.withActivity(getActivity())
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new BaseMultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                super.onPermissionsChecked(report);
                                if (report.areAllPermissionsGranted()) {
                                    FFmpeg ffmpeg = FFmpeg.getInstance(getActivity());
                                    try {
                                        long leftTime = (long) (mSpeakItem.getAudioDuration() * mSpeakItem.getAudioLeftMark());
                                        long rightTime = (long) (mSpeakItem.getAudioDuration() * mSpeakItem.getAudioRightMark());
                                        float durationTime = ((float) rightTime - (float) leftTime) / 1000;
                                        String startTime = String.format("%02d:%02d:%02d.%02d", (leftTime / 1000) / 3600, (leftTime / 1000) / 60, (leftTime / 1000) % 60, (int) (leftTime % 1000L) / 10);

                                        String audioIn = mSpeakItem.getAudioPath();
                                        String audioOut = Environment.getExternalStorageDirectory() +
                                                Constants.PATH_APP + "/" +
                                                Constants.PATH_AUDIO + "/" +
                                                mSpeakItemId + "_" + System.currentTimeMillis() + "_record.ogg";
                                        String run = "-i " + audioIn + " -ss " + startTime + " -t " + durationTime + " -acodec copy " + audioOut;
                                        String cmd[] = run.split(" ");
                                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                                            @Override
                                            public void onStart() {
                                                mProgressBar.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onProgress(String message) {
                                                mProgressBar.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onFailure(String message) {
                                                mProgressBar.setVisibility(View.GONE);
                                                Toast.makeText(getActivity(), "Failure: " + message, Toast.LENGTH_LONG);
                                            }

                                            @Override
                                            public void onSuccess(String message) {
                                                mProgressBar.setVisibility(View.GONE);
                                                mSpeakItem.setAudioPath(audioOut);
                                                mSpeakItem.setAudioDuration(rightTime - leftTime);
                                                onReloadAudioWave();
                                            }

                                            @Override
                                            public void onFinish() {
                                                mProgressBar.setVisibility(View.GONE);
                                            }
                                        });
                                    } catch (FFmpegCommandAlreadyRunningException e) {
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), "Exception: " + e.toString(), Toast.LENGTH_LONG);
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                super.onPermissionRationaleShouldBeShown(permissions, token);
                            }
                        }).check();
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Constants.REQUEST_GALLERY_CAPTURE) {
            if (resultCode == Constants.GALLERY_RESULT_PICK_PHOTO) {
                String photoPath = intent.getExtras().getString(Constants.KEY_GALLERY_PATH);
                String dateTime = intent.getExtras().getString(Constants.KEY_GALLERY_DATETIME);
                Location location = intent.getExtras().getParcelable(Constants.KEY_GALLERY_LOCATION);
                mSpeakItem.setPhotoPath(photoPath);
                mSpeakItem.setDateTime(dateTime);
                mSpeakItem.setLatitude(location.getLatitude());
                mSpeakItem.setLongitude(location.getLongitude());
                mSpeakItem.setLocation("");
                mPresenter.updateSpeakItem(mSpeakItem);
                onReloadPhotoView();
                onReloadPhotoAttribute();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_more) {
            mCallback.onClickSpeakItemDialog(mSpeakItem);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSpeakItemFromDialog(SpeakItem speakItem) {
        mSpeakItem = speakItem;
        onReloadPhotoAttribute();
        mPresenter.updateSpeakItem(speakItem);
        mPresenter.loadCollection(speakItem.getCollectionId());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mLanguage1Enable) {
            mSpeakItem.setDescription1(s.toString());
        }
        if (mLanguage2Enable) {
            mSpeakItem.setDescription2(s.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mLanguage1Enable) {
            mSpeakItem.setDescription1(s.toString());
        }
        if (mLanguage2Enable) {
            mSpeakItem.setDescription2(s.toString());
        }
    }
}
