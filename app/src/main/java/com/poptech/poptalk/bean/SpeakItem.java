package com.poptech.poptalk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.poptech.poptalk.utils.StringUtils;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem implements Parcelable {
    private long id;
    private String description1;
    private String description2;
    private String photoPath;
    private String datetime;
    private String location;
    private double latitude;
    private double longitude;
    private long collectionId;
    private String audioPath;
    private int audioDuration;
    private float audioProgress;
    private int audioProgressSec;
    private byte[] audioWaveform;
    private float audioLeftMark;
    private float audioRightMark;
    private float audioMiddleMark;
    private String language;
    private long addedTime;
    private int numAccess;


    public SpeakItem() {
        id = -1;
        description1 = "";
        description2 = "";
        photoPath = "";
        datetime = "";
        location = "";
        latitude = 0;
        longitude = 0;
        collectionId = -1;
        audioPath = "";
        language = "";
        addedTime = 0;
        numAccess = 0;
        setDefaultMark();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription1() {
        return description1;
    }

    public void setDescription1(String description1) {
        this.description1 = description1;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String latitude) {
        try {
            this.latitude = Double.valueOf(latitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String longitude) {
        try {
            this.longitude = Double.valueOf(longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLongitude() {
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

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    public long getAddedTime() {
        return this.addedTime;
    }

    public void setNumAccess(int numAccess) {
        this.numAccess = numAccess;
    }

    public int getNumAccess() {
        return this.numAccess;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.description1);
        dest.writeString(this.description2);
        dest.writeString(this.photoPath);
        dest.writeString(this.datetime);
        dest.writeString(this.location);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeLong(this.collectionId);
        dest.writeString(this.audioPath);
        dest.writeInt(this.audioDuration);
        dest.writeFloat(this.audioProgress);
        dest.writeInt(this.audioProgressSec);
        dest.writeByteArray(this.audioWaveform);
        dest.writeFloat(this.audioLeftMark);
        dest.writeFloat(this.audioRightMark);
        dest.writeFloat(this.audioMiddleMark);
        dest.writeString(this.language);
        dest.writeLong(this.addedTime);
        dest.writeInt(this.numAccess);
    }

    protected SpeakItem(Parcel in) {
        this.id = in.readLong();
        this.description1 = in.readString();
        this.description2 = in.readString();
        this.photoPath = in.readString();
        this.datetime = in.readString();
        this.location = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.collectionId = in.readLong();
        this.audioPath = in.readString();
        this.audioDuration = in.readInt();
        this.audioProgress = in.readFloat();
        this.audioProgressSec = in.readInt();
        this.audioWaveform = in.createByteArray();
        this.audioLeftMark = in.readFloat();
        this.audioRightMark = in.readFloat();
        this.audioMiddleMark = in.readFloat();
        this.language = in.readString();
        this.addedTime = in.readLong();
        this.numAccess = in.readInt();
    }

    public static final Parcelable.Creator<SpeakItem> CREATOR = new Parcelable.Creator<SpeakItem>() {
        @Override
        public SpeakItem createFromParcel(Parcel source) {
            return new SpeakItem(source);
        }

        @Override
        public SpeakItem[] newArray(int size) {
            return new SpeakItem[size];
        }
    };
}
