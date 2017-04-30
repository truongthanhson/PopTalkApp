package com.poptech.poptalk.sound;

import android.media.MediaRecorder;
import android.os.Build;


import com.poptech.poptalk.Constants;

import java.io.IOException;

// TODO: Auto-generated Javadoc

/**
 * The Class RecordUtils.
 */
public class VoiceRecorder {

    /**
     * The m media recorder.
     */
    private MediaRecorder mMediaRecorder;

    /** The m position. */

    /**
     * The instance.
     */
    private static VoiceRecorder instance = null;


    /**
     * Instantiates a new voice recorder.
     */
    private VoiceRecorder() {

    }

    /**
     * Gets the single instance of VoiceRecorder.
     *
     * @return single instance of VoiceRecorder
     */
    public static VoiceRecorder getInstance() {
        if (instance == null) {
            instance = new VoiceRecorder();
        }
        return instance;
    }

    // Start Record

    /**
     * Start record.
     *
     * @param path the path
     * @return the media recorder
     */
    public void startRecord(String path) {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (Build.VERSION.SDK_INT >= 10) {
            mMediaRecorder.setAudioSamplingRate(44100);
            mMediaRecorder.setAudioEncodingBitRate(96000);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        } else {
            // older version of Android, use crappy sounding voice codec
            mMediaRecorder.setAudioSamplingRate(8000);
            mMediaRecorder.setAudioEncodingBitRate(12200);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }
        mMediaRecorder.setOutputFile(path);
        mMediaRecorder.setMaxDuration(Constants.MAX_AUDIO_RECORD_TIME_MS);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }
    }

    /**
     * Release recorder.
     */
    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    // End Record

    /**
     * End record.
     */
    public void endRecord() {
        try {
            releaseRecorder();
        } catch (IllegalStateException e) {
            // it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }
    }

    // Delete File Record

    /**
     * Delete file record.
     *
     * @param path the path
     */
    /* public void deleteFileRecord(String path) {
        try {
            Utils.forceDelete(new File(path));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } */

}
