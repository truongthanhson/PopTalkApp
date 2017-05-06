package com.poptech.poptalk.collections;

import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by truongthanhson on 5/6/17.
 */

public class SpeakItemPresenter implements SpeakItemsContract.Presenter {

    private SpeakItemModel mModel;
    private SpeakItemsContract.View mView;

    @Inject
    public SpeakItemPresenter(SpeakItemModel mModel, SpeakItemsContract.View mView) {
        this.mModel = mModel;
        this.mView = mView;
    }

    @Inject
    public void setListeners(){
        mView.setPresenter(this);
    }

    @Override
    public void loadSpeakItems() {
//        mModel.generateTestData();
        List<SpeakItem> speakItems = mModel.getSpeakItems();
        mView.onSpeakItemsLoaded(speakItems);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
    }
}
