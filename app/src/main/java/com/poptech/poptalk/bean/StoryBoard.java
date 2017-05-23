package com.poptech.poptalk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by sontt on 23/05/2017.
 */

public class StoryBoard implements Parcelable {
    private long id;
    private List<SpeakItem> speakItems;
    private long createdTime;

    public StoryBoard(long id, List<SpeakItem> speakItems, long createdTime) {
        this.id = id;
        this.speakItems = speakItems;
        this.createdTime = createdTime;
    }

    public StoryBoard() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<SpeakItem> getSpeakItems() {
        return speakItems;
    }

    public void setSpeakItems(List<SpeakItem> speakItems) {
        this.speakItems = speakItems;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeTypedList(this.speakItems);
        dest.writeLong(this.createdTime);
    }

    protected StoryBoard(Parcel in) {
        this.id = in.readLong();
        this.speakItems = in.createTypedArrayList(SpeakItem.CREATOR);
        this.createdTime = in.readLong();
    }

    public static final Creator<StoryBoard> CREATOR = new Creator<StoryBoard>() {
        @Override
        public StoryBoard createFromParcel(Parcel source) {
            return new StoryBoard(source);
        }

        @Override
        public StoryBoard[] newArray(int size) {
            return new StoryBoard[size];
        }
    };
}
