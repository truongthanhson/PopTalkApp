package com.poptech.poptalk.bean;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.utils.StringUtils;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem implements Parcelable {
    private long id;
    private String description;
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


    public SpeakItem() {
        id = -1;
        description = "";
        photoPath = "";
        datetime = "";
        location = "";
        latitude = 0;
        longitude = 0;
        collectionId = -1;
        audioPath = "";
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.description);
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
    }

    protected SpeakItem(Parcel in) {
        this.id = in.readLong();
        this.description = in.readString();
        this.photoPath = in.readString();
        this.datetime = in.readString();
        this.location = in.readString();
        this.latitude = in.readFloat();
        this.longitude = in.readFloat();
        this.collectionId = in.readLong();
        this.audioPath = in.readString();
        this.audioDuration = in.readInt();
        this.audioProgress = in.readFloat();
        this.audioProgressSec = in.readInt();
        this.audioWaveform = in.createByteArray();
        this.audioLeftMark = in.readFloat();
        this.audioRightMark = in.readFloat();
        this.audioMiddleMark = in.readFloat();
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
