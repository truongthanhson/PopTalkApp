package com.poptech.poptalk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sontt on 28/04/2017.
 */

public class Collection implements Parcelable {
    private long id;
    private String description;
    private long addedTime;
    private int numAccess;
    private List<SpeakItem> speakItems;

    public Collection() {
        description = "";
        addedTime = 0;
        numAccess = 0;
        speakItems = new ArrayList<>();
    }

    public Collection(int id, String description, String language, String thumbPath) {
        this.id = id;
        this.description = description;
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

    public List<SpeakItem> getSpeakItems() {
        return speakItems;
    }

    public void addAllSpeakItems(List<SpeakItem> speakItems) {
        this.speakItems.addAll(speakItems);
    }

    public void addSpeakItems(SpeakItem speakItem) {
        this.speakItems.add(speakItem);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.description);
        dest.writeLong(this.addedTime);
        dest.writeInt(this.numAccess);
        dest.writeTypedList(this.speakItems);
    }

    protected Collection(Parcel in) {
        this.id = in.readLong();
        this.description = in.readString();
        this.addedTime = in.readLong();
        this.numAccess = in.readInt();
        this.speakItems = in.createTypedArrayList(SpeakItem.CREATOR);
    }

    public static final Parcelable.Creator<Collection> CREATOR = new Parcelable.Creator<Collection>() {
        @Override
        public Collection createFromParcel(Parcel source) {
            return new Collection(source);
        }

        @Override
        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };
}
