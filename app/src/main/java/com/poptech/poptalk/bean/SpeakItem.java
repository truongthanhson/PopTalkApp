package com.poptech.poptalk.bean;

import android.os.Environment;

import com.poptech.poptalk.Constants;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem {
    private long id;
    private String description;
    private String mark;
    private String photoPath;
    private long collectionId;
    private String audioPath;
    private int audioDuration;
    private float audioProgress;
    private int audioProgressSec;
    private byte[] audioWaveform;
    private String datetime;
    private String location;


    public SpeakItem() {
    }

    public SpeakItem(long id, String location, String datetime, String description, String mark, String photoPath, long collectionId) {
        this.id = id;
        this.location = location;
        this.datetime = datetime;
        this.description = description;
        this.mark = mark;
        this.photoPath = photoPath;
        this.collectionId = collectionId;
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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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
        this.audioPath = Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_SOUND + "/" + id + "_record.ogg";
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
}
