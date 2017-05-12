package com.poptech.poptalk.sound;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Vibrator;
import android.util.Log;

import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.utils.AndroidUtilities;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import static android.R.attr.path;

public class AudioController {
    private static final String TAG = "AudioController";

    private native int startRecord(String path);

    private native int writeFrame(ByteBuffer frame, int len);

    private native void stopRecord();

    private native int openOpusFile(String path);

    private native int seekOpusFile(float position);

    private native int isOpusFile(String path);

    private native void closeOpusFile();

    private native void readOpusFile(ByteBuffer buffer, int capacity, int[] args);

    private native long getTotalPcmDuration();

    public native byte[] getWaveform(String path);

    public native byte[] getWaveform2(short[] array, int length);

    public static int[] mReadArgs = new int[3];

    private long mAudioId;

    private class AudioBuffer {
        public AudioBuffer(int capacity) {
            buffer = ByteBuffer.allocateDirect(capacity);
            bufferBytes = new byte[capacity];
        }

        ByteBuffer buffer;
        byte[] bufferBytes;
        int size;
        int finished;
        long pcmOffset;
    }

    private Runnable mRecordStartRunnable;
    private DispatchQueue mGlobalQueue;
    private DispatchQueue mRecordQueue;
    private DispatchQueue mFileEncodingQueue;
    private DispatchQueue mFileDecodingQueue;
    private DispatchQueue mPlayerQueue;
    private AudioRecord mAudioRecorder = null;
    private File mRecordingAudioFile = null;
    private SpeakItem mRecordingAudio;
    private long mRecordStartTime;
    private long mRecordTimeCount;
    private int mSendAfterDone;
    private int mPlayerBufferSize = 0;
    private ArrayList<ByteBuffer> mRecordBuffers = new ArrayList<>();
    private ArrayList<AudioBuffer> mUsedPlayerBuffers = new ArrayList<>();
    private ArrayList<AudioBuffer> mFreePlayerBuffers = new ArrayList<>();
    private Runnable mRecordRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAudioRecorder != null) {
                ByteBuffer buffer;
                if (!mRecordBuffers.isEmpty()) {
                    buffer = mRecordBuffers.get(0);
                    mRecordBuffers.remove(0);
                } else {
                    buffer = ByteBuffer.allocateDirect(mRecordBufferSize);
                    buffer.order(ByteOrder.nativeOrder());
                }
                buffer.rewind();
                int len = mAudioRecorder.read(buffer, buffer.capacity());
                if (len > 0) {
                    buffer.limit(len);
                    double sum = 0;
                    try {
                        long newSamplesCount = mSamplesCount + len / 2;
                        int currentPart = (int) (((double) mSamplesCount / (double) newSamplesCount) * mRecordSamples.length);
                        int newPart = mRecordSamples.length - currentPart;
                        float sampleStep;
                        if (currentPart != 0) {
                            sampleStep = (float) mRecordSamples.length / (float) currentPart;
                            float currentNum = 0;
                            for (int a = 0; a < currentPart; a++) {
                                mRecordSamples[a] = mRecordSamples[(int) currentNum];
                                currentNum += sampleStep;
                            }
                        }
                        int currentNum = currentPart;
                        float nextNum = 0;
                        sampleStep = (float) len / 2 / (float) newPart;
                        for (int i = 0; i < len / 2; i++) {
                            short peak = buffer.getShort();
                            if (peak > 2500) {
                                sum += peak * peak;
                            }
                            if (i == (int) nextNum && currentNum < mRecordSamples.length) {
                                mRecordSamples[currentNum] = peak;
                                nextNum += sampleStep;
                                currentNum++;
                            }
                        }
                        mSamplesCount = newSamplesCount;
                    } catch (Exception e) {
                    }
                    buffer.position(0);
                    final double amplitude = Math.sqrt(sum / len / 2);
                    final ByteBuffer finalBuffer = buffer;
                    final boolean flush = len != buffer.capacity();
                    if (len != 0) {
                        mFileEncodingQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                while (finalBuffer.hasRemaining()) {
                                    int oldLimit = -1;
                                    if (finalBuffer.remaining() > mFileBuffer.remaining()) {
                                        oldLimit = finalBuffer.limit();
                                        finalBuffer.limit(mFileBuffer.remaining() + finalBuffer.position());
                                    }
                                    mFileBuffer.put(finalBuffer);
                                    if (mFileBuffer.position() == mFileBuffer.limit() || flush) {
                                        if (writeFrame(mFileBuffer, !flush ? mFileBuffer.limit() : finalBuffer.position()) != 0) {
                                            mFileBuffer.rewind();
                                            mRecordTimeCount += mFileBuffer.limit() / 2 / 16;
                                        }
                                    }
                                    if (oldLimit != -1) {
                                        finalBuffer.limit(oldLimit);
                                    }
                                }
                                mRecordQueue.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordBuffers.add(finalBuffer);
                                    }
                                });
                            }
                        });
                    }
                    mRecordQueue.postRunnable(mRecordRunnable);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordProgressChanged, mAudioId, System.currentTimeMillis() - mRecordStartTime, amplitude);
                        }
                    });
                } else {
                    mRecordBuffers.add(buffer);
                    stopRecordingInternal(mSendAfterDone);
                }
            }
        }
    };
    private short[] mRecordSamples = new short[1024];
    private long mSamplesCount;
    private ByteBuffer mFileBuffer;
    private int mRecordBufferSize;
    private boolean mIsPaused = false;
    private MediaPlayer mAudioPlayer = null;
    private AudioTrack mAudioTrackPlayer = null;
    private int mLastProgress = 0;
    private int mIgnoreFirstProgress = 0;
    private SpeakItem mPlayingAudioItem;
    private final Object mSync = new Object();
    private final Object mPlayerSync = new Object();
    private final Object mPlayerObjectSync = new Object();
    private final Object mProgressTimerSync = new Object();
    private Timer mProgressTimer = null;
    private long mLastPlayPcm;
    private long mCurrentTotalPcmDuration;
    private boolean mDecodingFinished = false;
    private int mBuffersWrited;

    private static volatile AudioController mInstance = null;

    public static AudioController getInstance(long id) {
        AudioController localInstance = mInstance;
        if (localInstance == null) {
            synchronized (AudioController.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new AudioController(id);
                }
            }
        }
        localInstance.mAudioId = id;
        return localInstance;
    }

    public AudioController(long id) {
        this.mAudioId = id;
        try {
            mRecordBufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (mRecordBufferSize <= 0) {
                mRecordBufferSize = 1280;
            }
            mPlayerBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (mPlayerBufferSize <= 0) {
                mPlayerBufferSize = 3840;
            }
            for (int a = 0; a < 5; a++) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                buffer.order(ByteOrder.nativeOrder());
                mRecordBuffers.add(buffer);
            }
            for (int a = 0; a < 3; a++) {
                mFreePlayerBuffers.add(new AudioBuffer(mPlayerBufferSize));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        mFileBuffer = ByteBuffer.allocateDirect(1920);
        mGlobalQueue = new DispatchQueue("mGlobalQueue");
        mRecordQueue = new DispatchQueue("mRecordQueue");
        mRecordQueue.setPriority(Thread.MAX_PRIORITY);
        mFileEncodingQueue = new DispatchQueue("mFileEncodingQueue");
        mFileEncodingQueue.setPriority(Thread.MAX_PRIORITY);
        mPlayerQueue = new DispatchQueue("mPlayerQueue");
        mFileDecodingQueue = new DispatchQueue("mFileDecodingQueue");
    }

    public void startRecording(final String path) {
        boolean paused = false;
        if (mPlayingAudioItem != null && isPlayingAudio(mPlayingAudioItem) && !isAudioPaused()) {
            paused = true;
            pauseAudio(mPlayingAudioItem);
        }

        try {
            Vibrator v = (Vibrator) PopTalkApplication.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        mRecordQueue.postRunnable(mRecordStartRunnable = new Runnable() {
            @Override
            public void run() {
                if (mAudioRecorder != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecordStartRunnable = null;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError, mAudioId);
                        }
                    });
                    return;
                }
                mRecordingAudio = new SpeakItem();
                mRecordingAudioFile = new File(path);
                if (!mRecordingAudioFile.getParentFile().exists()) {
                    try {
                        Utils.forceMkdir(mRecordingAudioFile.getParentFile());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if (startRecord(mRecordingAudioFile.getAbsolutePath()) == 0) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordStartRunnable = null;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError, mAudioId);
                            }
                        });
                        return;
                    }

                    mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mRecordBufferSize * 10);
                    mRecordStartTime = System.currentTimeMillis();
                    mRecordTimeCount = 0;
                    mSamplesCount = 0;
                    mFileBuffer.rewind();

                    mAudioRecorder.startRecording();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return;
                }

                mRecordQueue.postRunnable(mRecordRunnable);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordStartRunnable = null;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStarted, mAudioId);
                    }
                });
            }
        }, paused ? 500 : 50);
    }

    private void stopRecordingInternal(final int send) {
        if (send != 0) {
            final File recordingAudioFileToSend = mRecordingAudioFile;
            final SpeakItem audioToSend = mRecordingAudio;
            mFileEncodingQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            /*getWaveform2(mRecordSamples, mRecordSamples.length); */
                            audioToSend.setAudioWaveform(getWaveform(recordingAudioFileToSend.getAbsolutePath()));
                            audioToSend.setAudioPath(recordingAudioFileToSend.getAbsolutePath());
                            long duration = mRecordTimeCount;
                            audioToSend.setAudioDuration((int) (mRecordTimeCount / 1000));
                            if (duration > 700) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidSent, mAudioId, audioToSend);
                            } else {
                                recordingAudioFileToSend.delete();
                            }
                        }
                    });
                }
            });
        }
        try {
            if (mAudioRecorder != null) {
                mAudioRecorder.release();
                mAudioRecorder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        mRecordingAudio = null;
        mRecordingAudioFile = null;
    }

    public void stopRecording(final int send) {
        if (mRecordStartRunnable != null) {
            mRecordQueue.cancelRunnable(mRecordStartRunnable);
            mRecordStartRunnable = null;
        }
        mRecordQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mAudioRecorder == null) {
                    return;
                }
                try {
                    mSendAfterDone = send;
                    mAudioRecorder.stop();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    if (mRecordingAudioFile != null) {
                        mRecordingAudioFile.delete();
                    }
                }
                if (send == 0) {
                    stopRecordingInternal(0);
                }
                try {
                    Vibrator v = (Vibrator) PopTalkApplication.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(50);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (send == 0) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStopped, mAudioId);
                        } else {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordCompleted, mAudioId);
                        }
                    }
                });
            }
        });
    }

    private void seekOpusPlayer(final float progress) {
        if (progress == 1.0f) {
            return;
        }
        if (!mIsPaused) {
            mAudioTrackPlayer.pause();
        }
        mAudioTrackPlayer.flush();
        mFileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                seekOpusFile(progress);
                synchronized (mPlayerSync) {
                    mFreePlayerBuffers.addAll(mUsedPlayerBuffers);
                    mUsedPlayerBuffers.clear();
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mIsPaused) {
                            mIgnoreFirstProgress = 3;
                            mLastPlayPcm = (long) (mCurrentTotalPcmDuration * progress);
                            if (mAudioTrackPlayer != null) {
                                mAudioTrackPlayer.play();
                            }
                            mLastProgress = (int) (mCurrentTotalPcmDuration / 48.0f * progress);
                            checkPlayerQueue();
                        }
                    }
                });
            }
        });
    }

    public boolean seekToProgress(SpeakItem audioItem, float progress) {
        if (mAudioTrackPlayer == null && mAudioPlayer == null || audioItem == null || mPlayingAudioItem == null) {
            return false;
        }
        try {
            if (mAudioPlayer != null) {
                int seekTo = (int) (mAudioPlayer.getDuration() * progress);
                mAudioPlayer.seekTo(seekTo);
                mLastProgress = seekTo;
            } else if (mAudioTrackPlayer != null) {
                seekOpusPlayer(progress);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    private void checkDecoderQueue() {
        mFileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mDecodingFinished) {
                    checkPlayerQueue();
                    return;
                }
                boolean was = false;
                while (true) {
                    AudioBuffer buffer = null;
                    synchronized (mPlayerSync) {
                        if (!mFreePlayerBuffers.isEmpty()) {
                            buffer = mFreePlayerBuffers.get(0);
                            mFreePlayerBuffers.remove(0);
                        }
                        if (!mUsedPlayerBuffers.isEmpty()) {
                            was = true;
                        }
                    }
                    if (buffer != null) {
                        readOpusFile(buffer.buffer, mPlayerBufferSize, mReadArgs);
                        buffer.size = mReadArgs[0];
                        buffer.pcmOffset = mReadArgs[1];
                        buffer.finished = mReadArgs[2];
                        if (buffer.finished == 1) {
                            mDecodingFinished = true;
                        }
                        if (buffer.size != 0) {
                            buffer.buffer.rewind();
                            buffer.buffer.get(buffer.bufferBytes);
                            synchronized (mPlayerSync) {
                                mUsedPlayerBuffers.add(buffer);
                            }
                        } else {
                            synchronized (mPlayerSync) {
                                mFreePlayerBuffers.add(buffer);
                                break;
                            }
                        }
                        was = true;
                    } else {
                        break;
                    }
                }
                if (was) {
                    checkPlayerQueue();
                }
            }
        });
    }

    private void checkPlayerQueue() {
        mPlayerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (mPlayerObjectSync) {
                    if (mAudioTrackPlayer == null || mAudioTrackPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        return;
                    }
                }
                AudioBuffer buffer = null;
                synchronized (mPlayerSync) {
                    if (!mUsedPlayerBuffers.isEmpty()) {
                        buffer = mUsedPlayerBuffers.get(0);
                        mUsedPlayerBuffers.remove(0);
                    }
                }

                if (buffer != null) {
                    int count = 0;
                    try {
                        count = mAudioTrackPlayer.write(buffer.bufferBytes, 0, buffer.size);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    mBuffersWrited++;

                    if (count > 0) {
                        final long pcm = buffer.pcmOffset;
                        final int marker = buffer.finished == 1 ? count : -1;
                        final int finalBuffersWrited = mBuffersWrited;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mLastPlayPcm = pcm;
                                if (marker != -1) {
                                    if (mAudioTrackPlayer != null) {
                                        mAudioTrackPlayer.setNotificationMarkerPosition(1);
                                    }
                                    if (finalBuffersWrited == 1) {
                                        cleanupPlayer();
                                    }
                                }
                            }
                        });
                    }

                    if (buffer.finished != 1) {
                        checkPlayerQueue();
                    }
                }
                if (buffer == null || buffer != null && buffer.finished != 1) {
                    checkDecoderQueue();
                }

                if (buffer != null) {
                    synchronized (mPlayerSync) {
                        mFreePlayerBuffers.add(buffer);
                    }
                }
            }
        });
    }

    private void startProgressTimer(final SpeakItem currentPlayingSpeakItem) {
        synchronized (mProgressTimerSync) {
            if (mProgressTimer != null) {
                try {
                    mProgressTimer.cancel();
                    mProgressTimer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            mProgressTimer = new Timer();
            mProgressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (currentPlayingSpeakItem != null && (mAudioPlayer != null || mAudioTrackPlayer != null) && !mIsPaused) {
                                    try {
                                        if (mIgnoreFirstProgress != 0) {
                                            mIgnoreFirstProgress--;
                                            return;
                                        }
                                        int progress;
                                        float value;
                                        if (mAudioPlayer != null) {
                                            progress = mAudioPlayer.getCurrentPosition();
                                            value = (float) mLastProgress / (float) mAudioPlayer.getDuration();
                                            if (progress <= mLastProgress) {
                                                return;
                                            }
                                        } else {
                                            progress = (int) (mLastPlayPcm / 48.0f);
                                            value = (float) mLastPlayPcm / (float) mCurrentTotalPcmDuration;
                                            if (progress == mLastProgress) {
                                                return;
                                            }
                                        }
                                        mLastProgress = progress;
                                        currentPlayingSpeakItem.setAudioProgress(value);
                                        currentPlayingSpeakItem.setAudioProgressSec(mLastProgress / 1000);
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, mAudioId, value, mLastProgress / 1000);
                                    } catch (Exception e) {
                                        Log.e(TAG, e.toString());
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, 17);
        }
    }

    private void stopProgressTimer() {
        synchronized (mProgressTimerSync) {
            if (mProgressTimer != null) {
                try {
                    mProgressTimer.cancel();
                    mProgressTimer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void cleanupPlayer() {
        if (mAudioPlayer != null) {
            try {
                mAudioPlayer.reset();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                mAudioPlayer.stop();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                mAudioPlayer.release();
                mAudioPlayer = null;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else if (mAudioTrackPlayer != null) {
            synchronized (mPlayerObjectSync) {
                try {
                    mAudioTrackPlayer.pause();
                    mAudioTrackPlayer.flush();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                try {
                    mAudioTrackPlayer.release();
                    mAudioTrackPlayer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        stopProgressTimer();
        mLastProgress = 0;
        mIsPaused = false;
        if (mPlayingAudioItem != null) {
            mPlayingAudioItem.setAudioProgress(0.0f);
            mPlayingAudioItem.setAudioProgressSec(0);
            mPlayingAudioItem = null;
        }
    }

    public boolean pauseAudio(SpeakItem audioItem) {
        if (mAudioTrackPlayer == null && mAudioPlayer == null || audioItem == null || mPlayingAudioItem == null) {
            return false;
        }
        stopProgressTimer();
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.pause();
            } else if (mAudioTrackPlayer != null) {
                mAudioTrackPlayer.pause();
            }
            mIsPaused = true;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, mAudioId);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            mIsPaused = false;
            return false;
        }
        return true;
    }

    public boolean resumeAudio(SpeakItem audioItem) {
        if (mAudioTrackPlayer == null && mAudioPlayer == null || audioItem == null || mPlayingAudioItem == null) {
            return false;
        }

        try {
            startProgressTimer(audioItem);
            if (mAudioPlayer != null) {
                mAudioPlayer.start();
            } else if (mAudioTrackPlayer != null) {
                mAudioTrackPlayer.play();
                checkPlayerQueue();
            }
            mIsPaused = false;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, mAudioId);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    public boolean isPlayingAudio(SpeakItem audioItem) {
        return !(mAudioTrackPlayer == null && mAudioPlayer == null || audioItem == null || mPlayingAudioItem == null);
    }

    public boolean isAudioPaused() {
        return mIsPaused;
    }


    public boolean playAudio(final SpeakItem audioItem) {
        if (audioItem == null) {
            return false;
        }
        if ((mAudioTrackPlayer != null || mAudioPlayer != null)) {
            if (mIsPaused) {
                resumeAudio(audioItem);
            }
            return true;
        }

        cleanupPlayer();
        final File audioFile = new File(audioItem.getAudioPath());
        if (!audioFile.exists()) {
            return false;
        }

        if (isOpusFile(audioFile.getAbsolutePath()) == 1) {
            synchronized (mPlayerObjectSync) {
                try {
                    mIgnoreFirstProgress = 3;
                    final Semaphore semaphore = new Semaphore(0);
                    final Boolean[] result = new Boolean[1];
                    mFileDecodingQueue.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            result[0] = openOpusFile(audioFile.getAbsolutePath()) != 0;
                            semaphore.release();
                        }
                    });
                    semaphore.acquire();

                    if (!result[0]) {
                        return false;
                    }
                    mCurrentTotalPcmDuration = getTotalPcmDuration();
                    mAudioTrackPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mPlayerBufferSize, AudioTrack.MODE_STREAM);
                    mAudioTrackPlayer.setStereoVolume(1.0f, 1.0f);
                    mAudioTrackPlayer.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                        @Override
                        public void onMarkerReached(AudioTrack audioTrack) {
                            cleanupPlayer();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioStartCompleted, mAudioId);
                        }

                        @Override
                        public void onPeriodicNotification(AudioTrack audioTrack) {
                        }
                    });
                    mAudioTrackPlayer.play();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    if (mAudioTrackPlayer != null) {
                        mAudioTrackPlayer.release();
                        mAudioTrackPlayer = null;
                        mIsPaused = false;
                        mPlayingAudioItem = null;
                    }
                    return false;
                }
            }
        } else {
            try {
                mAudioPlayer = new MediaPlayer();
                mAudioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mAudioPlayer.setDataSource(audioFile.getAbsolutePath());
                mAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        cleanupPlayer();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioStartCompleted, mAudioId);
                    }
                });
                mAudioPlayer.prepare();
                mAudioPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged, mAudioId);
                if (mAudioPlayer != null) {
                    mAudioPlayer.release();
                    mAudioPlayer = null;
                    mIsPaused = false;
                    mPlayingAudioItem = null;
                }
                return false;
            }
        }

        mIsPaused = false;
        mLastProgress = 0;
        mLastPlayPcm = 0;
        mPlayingAudioItem = audioItem;
        startProgressTimer(mPlayingAudioItem);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidStarted, mAudioId, audioItem);

        if (mAudioPlayer != null) {
            try {
                if (mPlayingAudioItem.getAudioProgress() != 0) {
                    int seekTo = (int) (mAudioPlayer.getDuration() * mPlayingAudioItem.getAudioProgress());
                    mAudioPlayer.seekTo(seekTo);
                }
            } catch (Exception e2) {
                mPlayingAudioItem.setAudioProgress(0);
                mPlayingAudioItem.setAudioProgressSec(0);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, mAudioId, 0.0f, 0);
                Log.e(TAG, e2.toString());
            }

        } else if (mAudioTrackPlayer != null) {
            if (mPlayingAudioItem.getAudioProgress() == 1) {
                mPlayingAudioItem.setAudioProgress(0);
            }
            mFileDecodingQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mPlayingAudioItem != null && mPlayingAudioItem.getAudioProgress() != 0) {
                            mLastPlayPcm = (long) (mCurrentTotalPcmDuration * mPlayingAudioItem.getAudioProgress());
                            seekOpusFile(mPlayingAudioItem.getAudioProgress());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    synchronized (mPlayerSync) {
                        mFreePlayerBuffers.addAll(mUsedPlayerBuffers);
                        mUsedPlayerBuffers.clear();
                    }
                    mDecodingFinished = false;
                    checkPlayerQueue();
                }
            });
            mAudioTrackPlayer.play();
        }
        return true;
    }

    public void stopAudio() {
        if (mAudioTrackPlayer == null && mAudioPlayer == null || mPlayingAudioItem == null) {
            return;
        }
        try {
            if (mAudioPlayer != null) {
                try {
                    mAudioPlayer.reset();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                mAudioPlayer.stop();
            } else if (mAudioTrackPlayer != null) {
                mAudioTrackPlayer.pause();
                mAudioTrackPlayer.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.release();
                mAudioPlayer = null;
            } else if (mAudioTrackPlayer != null) {
                synchronized (mPlayerObjectSync) {
                    mAudioTrackPlayer.release();
                    mAudioTrackPlayer = null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        stopProgressTimer();
        mPlayingAudioItem = null;
        mIsPaused = false;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioStartCompleted, mAudioId);
    }

    public boolean generateWaveform(final SpeakItem audioItem) {
        if (StringUtils.isNullOrEmpty(audioItem.getAudioPath())) {
            return false;
        }
        File audioFile = new File(audioItem.getAudioPath());
        if (!audioFile.exists()) {
            return false;
        }
        mGlobalQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                final byte[] waveform = getWaveform(audioItem.getAudioPath());
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (waveform != null) {
                            audioItem.setAudioWaveform(waveform);
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidSent, mAudioId, audioItem);
                        }
                    }
                });
            }
        });
        return true;
    }
}
