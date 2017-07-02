package com.poptech.poptalk.speakitem;

import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.SpeakItemModel;

import javax.inject.Inject;

public class SpeakItemDetailPresenter implements SpeakItemDetailContract.Presenter {

    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;
    private SpeakItemDetailContract.View mView;

    @Inject
    public SpeakItemDetailPresenter(SpeakItemModel mSpeakItemModel, CollectionsModel mCollectionModel, SpeakItemDetailContract.View mView) {
        this.mSpeakItemModel = mSpeakItemModel;
        this.mCollectionModel = mCollectionModel;
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
    public void loadSpeakItem(long itemId) {
        mView.onSpeakItemLoaded(mSpeakItemModel.getSpeakItem(itemId));
    }

    @Override
    public void loadCollection(long collectionId) {
        Collection collection = mCollectionModel.getCollection(collectionId);
        collection.addAllSpeakItems(mSpeakItemModel.getSpeakItems(collectionId));
        mView.onCollectionLoaded(collection);
    }

    @Override
    public void updateSpeakItem(SpeakItem speakItem) {
        mSpeakItemModel.updateSpeakItem(speakItem);
    }

    @Override
    public void updateCollection(Collection collection) {
        mCollectionModel.updateCollection(collection);
    }
}
