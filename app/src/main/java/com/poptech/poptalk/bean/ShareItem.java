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

public class ShareItem {
    private Constants.ShareType shareType;
    List<SpeakItem> speakItems;

    public ShareItem() {
        shareType = Constants.ShareType.NONE;
        speakItems = new ArrayList<>();
    }

    public Constants.ShareType getShareType() {
        return shareType;
    }

    public void setShareType(Constants.ShareType shareType) {
        this.shareType = shareType;
    }

    public List<SpeakItem> getSpeakItems() {
        return speakItems;
    }

    public void setSpeakItems(List<SpeakItem> speakItems) {
        this.speakItems = speakItems;
    }


    public void addSpeakItem(SpeakItem speakItem) {
        this.speakItems.add(speakItem);
    }
}
