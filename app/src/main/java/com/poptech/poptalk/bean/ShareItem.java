package com.poptech.poptalk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.poptech.poptalk.Constants;

/**
 * Created by sontt on 30/04/2017.
 */

public class ShareItem implements Parcelable {
    private Constants.ShareType type;
    SpeakItem speakItem;
    StoryBoard storyBoard;
    Collection collection;

    public ShareItem() {
        type = Constants.ShareType.NONE;
    }

    public Constants.ShareType getType() {
        return type;
    }

    public void setType(Constants.ShareType type) {
        this.type = type;
    }

    public SpeakItem getSpeakItem() {
        return speakItem;
    }

    public void setSpeakItem(SpeakItem speakItem) {
        this.speakItem = speakItem;
    }

    public StoryBoard getStoryboard() {
        return storyBoard;
    }

    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
    }


    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.speakItem, flags);
        dest.writeParcelable(this.storyBoard, flags);
        dest.writeParcelable(this.collection, flags);
    }

    protected ShareItem(Parcel in) {
        this.speakItem = in.readParcelable(SpeakItem.class.getClassLoader());
        this.storyBoard = in.readParcelable(StoryBoard.class.getClassLoader());
        this.collection = in.readParcelable(Collection.class.getClassLoader());
    }

    public static final Parcelable.Creator<ShareItem> CREATOR = new Parcelable.Creator<ShareItem>() {
        @Override
        public ShareItem createFromParcel(Parcel source) {
            return new ShareItem(source);
        }

        @Override
        public ShareItem[] newArray(int size) {
            return new ShareItem[size];
        }
    };
}
