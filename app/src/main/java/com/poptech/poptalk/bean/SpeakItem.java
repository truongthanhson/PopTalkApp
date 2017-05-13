package com.poptech.poptalk.bean;

import android.os.Environment;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.utils.StringUtils;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem {
    private long id;
    private String description;
    private String photoPath;
    private String datetime;
    private String location;
    private float latitude;
    private float longitude;
    private long collectionId;
    private String audioPath;
    private int audioDuration;
    private float audioProgress;
    private int audioProgressSec;
    private byte[] audioWaveform;
    private float audioLeftMark;
    private float audioRightMark;
    private float audioMiddleMark;


    public SpeakItem() {
        setDefaultMark();
    }

    public SpeakItem(long id, String location, String datetime, String description, String mark, String photoPath, long collectionId) {
        this.id = id;
        this.location = location;
        this.datetime = datetime;
        this.description = description;
        this.photoPath = photoPath;
        this.collectionId = collectionId;
        setDefaultMark();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setDateTime(String datetime) {
        this.datetime = datetime;
    }

    public String getDateTime() {
        return datetime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String latitude) {
        try {
            this.latitude = Float.valueOf(latitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String longitude) {
        try {
            this.longitude = Float.valueOf(longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getLongitude() {
        return longitude;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public void generateAudioPath() {
        this.audioPath = Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_AUDIO + "/" + id + "_record.ogg";
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioDuration(int audioDuration) {
        this.audioDuration = audioDuration;
    }

    public int getAudioDuration() {
        return audioDuration;
    }

    public void setAudioProgress(float audioProgress) {
        this.audioProgress = audioProgress;
    }

    public float getAudioProgress() {
        return audioProgress;
    }

    public void setAudioProgressSec(int audioProgressSec) {
        this.audioProgressSec = audioProgressSec;
    }

    public int getAudioProgressSec() {
        return audioProgressSec;
    }

    public void setAudioWaveform(byte[] audioWaveform) {
        this.audioWaveform = audioWaveform;
    }

    public byte[] getAudioWaveform() {
        return audioWaveform;
    }

    private void setDefaultMark() {
        this.audioLeftMark = 0.0f;
        this.audioMiddleMark = 0.5f;
        this.audioRightMark = 1.0f;
    }

    public void setAudioMark(String audioMark) {
        if (!StringUtils.isNullOrEmpty(audioMark)) {
            String[] marks = audioMark.split(";");
            try {
                this.audioLeftMark = Float.valueOf(marks[0]);
                this.audioMiddleMark = Float.valueOf(marks[1]);
                this.audioRightMark = Float.valueOf(marks[2]);
            } catch (Exception e) {
                setDefaultMark();
            }
        } else {
            setDefaultMark();
        }
    }

    public String getAudioMark() {
        return audioLeftMark + ";" + audioMiddleMark + ";" + audioRightMark;
    }

    public void setAudioLeftMark(float audioLeftMark) {
        this.audioLeftMark = audioLeftMark;
    }

    public float getAudioLeftMark() {
        return audioLeftMark;
    }

    public void setAudioMiddleMark(float audioMiddleMark) {
        this.audioMiddleMark = audioMiddleMark;
    }

    public float getAudioMiddleMark() {
        return audioMiddleMark;
    }

    public void setAudioRightMark(float audioRightMark) {
        this.audioRightMark = audioRightMark;
    }

    public float getAudioRightMark() {
        return audioRightMark;
    }
}
