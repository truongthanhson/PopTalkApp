package com.poptech.poptalk.speakitem;

import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;

import javax.inject.Inject;

public class SpeakItemDetailPresenter implements SpeakItemDetailContract.Presenter {

    private SpeakItemModel mModel;
    private SpeakItemDetailContract.View mView;

    @Inject
    public SpeakItemDetailPresenter(SpeakItemModel mModel, SpeakItemDetailContract.View mView) {
        this.mModel = mModel;
        this.mView = mView;
    }

    @Inject
    public void setListeners() {
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
    }

    @Override
    public void loadSpeakItem(int itemId) {
        List<SpeakItem> speakItems = mModel.getSpeakItems();
        mView.onSpeakItemLoaded(speakItems.get(itemId));
    }
}
