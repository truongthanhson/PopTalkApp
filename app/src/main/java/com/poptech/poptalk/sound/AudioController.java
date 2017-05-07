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
import com.poptech.poptalk.utils.Utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

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

    public static int[] readArgs = new int[3];

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

    private Runnable recordStartRunnable;
    private DispatchQueue recordQueue;
    private AudioRecord audioRecorder = null;
    private File recordingAudioFile = null;
    private SpeakItem recordingAudio;
    private long recordStartTime;
    private long recordTimeCount;
    private int sendAfterDone;
    private int playerBufferSize = 0;
    private ArrayList<ByteBuffer> recordBuffers = new ArrayList<>();
    private DispatchQueue fileEncodingQueue;
    private DispatchQueue fileDecodingQueue;
    private DispatchQueue playerQueue;
    private ArrayList<AudioBuffer> usedPlayerBuffers = new ArrayList<>();
    private ArrayList<AudioBuffer> freePlayerBuffers = new ArrayList<>();
    private Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (audioRecorder != null) {
                ByteBuffer buffer;
                if (!recordBuffers.isEmpty()) {
                    buffer = recordBuffers.get(0);
                    recordBuffers.remove(0);
                } else {
                    buffer = ByteBuffer.allocateDirect(recordBufferSize);
                    buffer.order(ByteOrder.nativeOrder());
                }
                buffer.rewind();
                int len = audioRecorder.read(buffer, buffer.capacity());
                if (len > 0) {
                    buffer.limit(len);
                    double sum = 0;
                    try {
                        long newSamplesCount = samplesCount + len / 2;
                        int currentPart = (int) (((double) samplesCount / (double) newSamplesCount) * recordSamples.length);
                        int newPart = recordSamples.length - currentPart;
                        float sampleStep;
                        if (currentPart != 0) {
                            sampleStep = (float) recordSamples.length / (float) currentPart;
                            float currentNum = 0;
                            for (int a = 0; a < currentPart; a++) {
                                recordSamples[a] = recordSamples[(int) currentNum];
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
                            if (i == (int) nextNum && currentNum < recordSamples.length) {
                                recordSamples[currentNum] = peak;
                                nextNum += sampleStep;
                                currentNum++;
                            }
                        }
                        samplesCount = newSamplesCount;
                    } catch (Exception e) {
                    }
                    buffer.position(0);
                    final double amplitude = Math.sqrt(sum / len / 2);
                    final ByteBuffer finalBuffer = buffer;
                    final boolean flush = len != buffer.capacity();
                    if (len != 0) {
                        fileEncodingQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                while (finalBuffer.hasRemaining()) {
                                    int oldLimit = -1;
                                    if (finalBuffer.remaining() > fileBuffer.remaining()) {
                                        oldLimit = finalBuffer.limit();
                                        finalBuffer.limit(fileBuffer.remaining() + finalBuffer.position());
                                    }
                                    fileBuffer.put(finalBuffer);
                                    if (fileBuffer.position() == fileBuffer.limit() || flush) {
                                        if (writeFrame(fileBuffer, !flush ? fileBuffer.limit() : finalBuffer.position()) != 0) {
                                            fileBuffer.rewind();
                                            recordTimeCount += fileBuffer.limit() / 2 / 16;
                                        }
                                    }
                                    if (oldLimit != -1) {
                                        finalBuffer.limit(oldLimit);
                                    }
                                }
                                recordQueue.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        recordBuffers.add(finalBuffer);
                                    }
                                });
                            }
                        });
                    }
                    recordQueue.postRunnable(recordRunnable);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordProgressChanged, System.currentTimeMillis() - recordStartTime, amplitude);
                        }
                    });
                } else {
                    recordBuffers.add(buffer);
                    stopRecordingInternal(sendAfterDone);
                }
            }
        }
    };
    private short[] recordSamples = new short[1024];
    private long samplesCount;
    private ByteBuffer fileBuffer;
    private int recordBufferSize;
    private boolean isPaused = false;
    private MediaPlayer audioPlayer = null;
    private AudioTrack audioTrackPlayer = null;
    private int lastProgress = 0;
    private int ignoreFirstProgress = 0;
    private SpeakItem playingAudioItem;
    private final Object sync = new Object();
    private final Object playerSync = new Object();
    private final Object playerObjectSync = new Object();
    private final Object progressTimerSync = new Object();
    private Timer progressTimer = null;
    private long lastPlayPcm;
    private long currentTotalPcmDuration;
    private boolean decodingFinished = false;
    private int buffersWrited;

    private static volatile AudioController Instance = null;

    public static AudioController getInstance() {
        AudioController localInstance = Instance;
        if (localInstance == null) {
            synchronized (AudioController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new AudioController();
                }
            }
        }
        return localInstance;
    }

    public AudioController() {
        try {
            recordBufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (recordBufferSize <= 0) {
                recordBufferSize = 1280;
            }
            playerBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (playerBufferSize <= 0) {
                playerBufferSize = 3840;
            }
            for (int a = 0; a < 5; a++) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                buffer.order(ByteOrder.nativeOrder());
                recordBuffers.add(buffer);
            }
            for (int a = 0; a < 3; a++) {
                freePlayerBuffers.add(new AudioBuffer(playerBufferSize));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        fileBuffer = ByteBuffer.allocateDirect(1920);
        recordQueue = new DispatchQueue("recordQueue");
        recordQueue.setPriority(Thread.MAX_PRIORITY);
        fileEncodingQueue = new DispatchQueue("fileEncodingQueue");
        fileEncodingQueue.setPriority(Thread.MAX_PRIORITY);
        playerQueue = new DispatchQueue("playerQueue");
        fileDecodingQueue = new DispatchQueue("fileDecodingQueue");
    }

    public void startRecording(final String path) {
        boolean paused = false;
        if (playingAudioItem != null && isPlayingAudio(playingAudioItem) && !isAudioPaused()) {
            paused = true;
            pauseAudio(playingAudioItem);
        }

        try {
            Vibrator v = (Vibrator) PopTalkApplication.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        recordQueue.postRunnable(recordStartRunnable = new Runnable() {
            @Override
            public void run() {
                if (audioRecorder != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            recordStartRunnable = null;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError);
                        }
                    });
                    return;
                }
                recordingAudio = new SpeakItem();
                recordingAudioFile = new File(path);

                try {
                    if (startRecord(recordingAudioFile.getAbsolutePath()) == 0) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                recordStartRunnable = null;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartError);
                            }
                        });
                        return;
                    }

                    audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordBufferSize * 10);
                    recordStartTime = System.currentTimeMillis();
                    recordTimeCount = 0;
                    samplesCount = 0;
                    fileBuffer.rewind();

                    audioRecorder.startRecording();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return;
                }

                recordQueue.postRunnable(recordRunnable);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        recordStartRunnable = null;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStarted);
                    }
                });
            }
        }, paused ? 500 : 50);
    }

    private void stopRecordingInternal(final int send) {
        if (send != 0) {
            final File recordingAudioFileToSend = recordingAudioFile;
            final SpeakItem audioToSend = recordingAudio;
            fileEncodingQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            /*getWaveform2(recordSamples, recordSamples.length); */
                            audioToSend.setAudioWaveform(getWaveform(recordingAudioFileToSend.getAbsolutePath()));
                            audioToSend.setAudioPath(recordingAudioFileToSend.getAbsolutePath());
                            long duration = recordTimeCount;
                            audioToSend.setAudioDuration((int) (recordTimeCount / 1000));
                            if (duration > 700) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidSent, audioToSend);
                            } else {
                                recordingAudioFileToSend.delete();
                            }
                        }
                    });
                }
            });
        }
        try {
            if (audioRecorder != null) {
                audioRecorder.release();
                audioRecorder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        recordingAudio = null;
        recordingAudioFile = null;
    }

    public void stopRecording(final int send) {
        if (recordStartRunnable != null) {
            recordQueue.cancelRunnable(recordStartRunnable);
            recordStartRunnable = null;
        }
        recordQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (audioRecorder == null) {
                    return;
                }
                try {
                    sendAfterDone = send;
                    audioRecorder.stop();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    if (recordingAudioFile != null) {
                        recordingAudioFile.delete();
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
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStopped);
                    }
                });
            }
        });
    }

    private void seekOpusPlayer(final float progress) {
        if (progress == 1.0f) {
            return;
        }
        if (!isPaused) {
            audioTrackPlayer.pause();
        }
        audioTrackPlayer.flush();
        fileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                seekOpusFile(progress);
                synchronized (playerSync) {
                    freePlayerBuffers.addAll(usedPlayerBuffers);
                    usedPlayerBuffers.clear();
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isPaused) {
                            ignoreFirstProgress = 3;
                            lastPlayPcm = (long) (currentTotalPcmDuration * progress);
                            if (audioTrackPlayer != null) {
                                audioTrackPlayer.play();
                            }
                            lastProgress = (int) (currentTotalPcmDuration / 48.0f * progress);
                            checkPlayerQueue();
                        }
                    }
                });
            }
        });
    }

    public boolean seekToProgress(SpeakItem audioItem, float progress) {
        if (audioTrackPlayer == null && audioPlayer == null || audioItem == null || playingAudioItem == null) {
            return false;
        }
        try {
            if (audioPlayer != null) {
                int seekTo = (int) (audioPlayer.getDuration() * progress);
                audioPlayer.seekTo(seekTo);
                lastProgress = seekTo;
            } else if (audioTrackPlayer != null) {
                seekOpusPlayer(progress);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    private void checkDecoderQueue() {
        fileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (decodingFinished) {
                    checkPlayerQueue();
                    return;
                }
                boolean was = false;
                while (true) {
                    AudioBuffer buffer = null;
                    synchronized (playerSync) {
                        if (!freePlayerBuffers.isEmpty()) {
                            buffer = freePlayerBuffers.get(0);
                            freePlayerBuffers.remove(0);
                        }
                        if (!usedPlayerBuffers.isEmpty()) {
                            was = true;
                        }
                    }
                    if (buffer != null) {
                        readOpusFile(buffer.buffer, playerBufferSize, readArgs);
                        buffer.size = readArgs[0];
                        buffer.pcmOffset = readArgs[1];
                        buffer.finished = readArgs[2];
                        if (buffer.finished == 1) {
                            decodingFinished = true;
                        }
                        if (buffer.size != 0) {
                            buffer.buffer.rewind();
                            buffer.buffer.get(buffer.bufferBytes);
                            synchronized (playerSync) {
                                usedPlayerBuffers.add(buffer);
                            }
                        } else {
                            synchronized (playerSync) {
                                freePlayerBuffers.add(buffer);
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
        playerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (playerObjectSync) {
                    if (audioTrackPlayer == null || audioTrackPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        return;
                    }
                }
                AudioBuffer buffer = null;
                synchronized (playerSync) {
                    if (!usedPlayerBuffers.isEmpty()) {
                        buffer = usedPlayerBuffers.get(0);
                        usedPlayerBuffers.remove(0);
                    }
                }

                if (buffer != null) {
                    int count = 0;
                    try {
                        count = audioTrackPlayer.write(buffer.bufferBytes, 0, buffer.size);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    buffersWrited++;

                    if (count > 0) {
                        final long pcm = buffer.pcmOffset;
                        final int marker = buffer.finished == 1 ? count : -1;
                        final int finalBuffersWrited = buffersWrited;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                lastPlayPcm = pcm;
                                if (marker != -1) {
                                    if (audioTrackPlayer != null) {
                                        audioTrackPlayer.setNotificationMarkerPosition(1);
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
                    synchronized (playerSync) {
                        freePlayerBuffers.add(buffer);
                    }
                }
            }
        });
    }

    private void startProgressTimer(final SpeakItem currentPlayingSpeakItem) {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            progressTimer = new Timer();
            progressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (sync) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (currentPlayingSpeakItem != null && (audioPlayer != null || audioTrackPlayer != null) && !isPaused) {
                                    try {
                                        if (ignoreFirstProgress != 0) {
                                            ignoreFirstProgress--;
                                            return;
                                        }
                                        int progress;
                                        float value;
                                        if (audioPlayer != null) {
                                            progress = audioPlayer.getCurrentPosition();
                                            value = (float) lastProgress / (float) audioPlayer.getDuration();
                                            if (progress <= lastProgress) {
                                                return;
                                            }
                                        } else {
                                            progress = (int) (lastPlayPcm / 48.0f);
                                            value = (float) lastPlayPcm / (float) currentTotalPcmDuration;
                                            if (progress == lastProgress) {
                                                return;
                                            }
                                        }
                                        lastProgress = progress;
                                        currentPlayingSpeakItem.setAudioProgress(value);
                                        currentPlayingSpeakItem.setAudioProgressSec(lastProgress / 1000);
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, value);
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
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void cleanupPlayer() {
        if (audioPlayer != null) {
            try {
                audioPlayer.reset();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                audioPlayer.stop();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                audioPlayer.release();
                audioPlayer = null;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else if (audioTrackPlayer != null) {
            synchronized (playerObjectSync) {
                try {
                    audioTrackPlayer.pause();
                    audioTrackPlayer.flush();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                try {
                    audioTrackPlayer.release();
                    audioTrackPlayer = null;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        stopProgressTimer();
        lastProgress = 0;
        isPaused = false;
        if (playingAudioItem != null) {
            playingAudioItem.setAudioProgress(0.0f);
            playingAudioItem.setAudioProgressSec(0);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, 0);
            playingAudioItem = null;
        }
    }

    public boolean pauseAudio(SpeakItem audioItem) {
        if (audioTrackPlayer == null && audioPlayer == null || audioItem == null || playingAudioItem == null) {
            return false;
        }
        stopProgressTimer();
        try {
            if (audioPlayer != null) {
                audioPlayer.pause();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
            }
            isPaused = true;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            isPaused = false;
            return false;
        }
        return true;
    }

    public boolean resumeAudio(SpeakItem audioItem) {
        if (audioTrackPlayer == null && audioPlayer == null || audioItem == null || playingAudioItem == null) {
            return false;
        }

        try {
            startProgressTimer(audioItem);
            if (audioPlayer != null) {
                audioPlayer.start();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.play();
            }
            isPaused = false;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    public boolean isPlayingAudio(SpeakItem audioItem) {
        return !(audioTrackPlayer == null && audioPlayer == null || audioItem == null || playingAudioItem == null);
    }

    public boolean isAudioPaused() {
        return isPaused;
    }


    public boolean playAudio(final SpeakItem audioItem) {
        if (audioItem == null) {
            return false;
        }
        if ((audioTrackPlayer != null || audioPlayer != null)) {
            if (isPaused) {
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
            synchronized (playerObjectSync) {
                try {
                    ignoreFirstProgress = 3;
                    final Semaphore semaphore = new Semaphore(0);
                    final Boolean[] result = new Boolean[1];
                    fileDecodingQueue.postRunnable(new Runnable() {
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
                    currentTotalPcmDuration = getTotalPcmDuration();
                    audioTrackPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, playerBufferSize, AudioTrack.MODE_STREAM);
                    audioTrackPlayer.setStereoVolume(1.0f, 1.0f);
                    audioTrackPlayer.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                        @Override
                        public void onMarkerReached(AudioTrack audioTrack) {
                            cleanupPlayer();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartCompleted);
                        }

                        @Override
                        public void onPeriodicNotification(AudioTrack audioTrack) {
                        }
                    });
                    audioTrackPlayer.play();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    if (audioTrackPlayer != null) {
                        audioTrackPlayer.release();
                        audioTrackPlayer = null;
                        isPaused = false;
                        playingAudioItem = null;
                    }
                    return false;
                }
            }
        } else {
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                audioPlayer.setDataSource(audioFile.getAbsolutePath());
                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        cleanupPlayer();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.recordStartCompleted);
                    }
                });
                audioPlayer.prepare();
                audioPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioPlayStateChanged);
                if (audioPlayer != null) {
                    audioPlayer.release();
                    audioPlayer = null;
                    isPaused = false;
                    playingAudioItem = null;
                }
                return false;
            }
        }

        isPaused = false;
        lastProgress = 0;
        lastPlayPcm = 0;
        playingAudioItem = audioItem;
        startProgressTimer(playingAudioItem);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidStarted, audioItem);

        if (audioPlayer != null) {
            try {
                if (playingAudioItem.getAudioProgress() != 0) {
                    int seekTo = (int) (audioPlayer.getDuration() * playingAudioItem.getAudioProgress());
                    audioPlayer.seekTo(seekTo);
                }
            } catch (Exception e2) {
                playingAudioItem.setAudioProgress(0);
                playingAudioItem.setAudioProgressSec(0);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, 0);
                Log.e(TAG, e2.toString());
            }
        } else if (audioTrackPlayer != null) {
            if (playingAudioItem.getAudioProgress() == 1) {
                playingAudioItem.setAudioProgress(0);
            }
            fileDecodingQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (playingAudioItem != null && playingAudioItem.getAudioProgress() != 0) {
                            lastPlayPcm = (long) (currentTotalPcmDuration * playingAudioItem.getAudioProgress());
                            seekOpusFile(playingAudioItem.getAudioProgress());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    synchronized (playerSync) {
                        freePlayerBuffers.addAll(usedPlayerBuffers);
                        usedPlayerBuffers.clear();
                    }
                    decodingFinished = false;
                    checkPlayerQueue();
                }
            });
        }
        return true;
    }

    public void stopAudio() {
        if (audioTrackPlayer == null && audioPlayer == null || playingAudioItem == null) {
            return;
        }
        try {
            if (audioPlayer != null) {
                try {
                    audioPlayer.reset();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                audioPlayer.stop();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
                audioTrackPlayer.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            } else if (audioTrackPlayer != null) {
                synchronized (playerObjectSync) {
                    audioTrackPlayer.release();
                    audioTrackPlayer = null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        stopProgressTimer();
        playingAudioItem = null;
        isPaused = false;
    }


    public SpeakItem getPlayingAudioItem() {
        return playingAudioItem;
    }
}
