package com.poptech.poptalk.collections;

import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by truongthanhson on 5/6/17.
 */

public class SpeakItemPresenter implements SpeakItemsContract.Presenter {

    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;
    private SpeakItemsContract.View mView;

    @Inject
    public SpeakItemPresenter(SpeakItemModel mSpeakItemModel, CollectionsModel mCollectionModel, SpeakItemsContract.View mView) {
        this.mSpeakItemModel = mSpeakItemModel;
        this.mCollectionModel = mCollectionModel;
        this.mView = mView;
    }

    @Inject
    public void setListeners() {
        mView.setPresenter(this);
    }

    @Override
    public void loadAllSpeakItems() {
        List<SpeakItem> speakItems = mSpeakItemModel.getSpeakItems();
        List<Collection> collections = mCollectionModel.getCollections();
        mView.onAllSpeakItemsLoaded(speakItems, collections);
    }

    @Override
    public void loadSpeakItems(long collectionId) {
        //mModel.generateTestData();
        List<SpeakItem> speakItems = mSpeakItemModel.getSpeakItems(collectionId);
        mView.onSpeakItemsLoaded(speakItems);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
    }
}
