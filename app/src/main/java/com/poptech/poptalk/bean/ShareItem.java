package com.poptech.poptalk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sontt on 30/04/2017.
 */

public class ShareItem implements Parcelable {
    private Constants.ShareType shareType;
    SpeakItem speakItem;
    StoryBoard storyBoard;

    public ShareItem() {
        shareType = Constants.ShareType.NONE;
    }

    public Constants.ShareType getShareType() {
        return shareType;
    }

    public void setShareType(Constants.ShareType shareType) {
        this.shareType = shareType;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.speakItem, flags);
        dest.writeParcelable(this.storyBoard, flags);
    }

    protected ShareItem(Parcel in) {
        this.speakItem = in.readParcelable(SpeakItem.class.getClassLoader());
        this.storyBoard = in.readParcelable(StoryBoard.class.getClassLoader());
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
