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
    public SpeakItemPresenter(SpeakItemModel speakItemModel, CollectionsModel collectionModel, SpeakItemsContract.View mView) {
        this.mSpeakItemModel = speakItemModel;
        this.mCollectionModel = collectionModel;
        this.mView = mView;
    }

    @Inject
    public void setListeners() {
        mView.setPresenter(this);
    }

    @Override
    public void loadCollections() {
        List<Collection> collections = mCollectionModel.getCollections();
        for (int i = 0; i < collections.size(); i++) {
            long collectionId = collections.get(i).getId();
            collections.get(i).addAllSpeakItems(mSpeakItemModel.getSpeakItems(collectionId));
        }
        mView.onCollectionsLoaded(collections);
    }

    @Override
    public void loadSpeakItems(long collectionId) {
        //mModel.generateTestData();
        List<SpeakItem> speakItems = mSpeakItemModel.getSpeakItems(collectionId);
        mView.onSpeakItemsLoaded(speakItems);
    }

    @Override
    public void loadCollection(long collectionId) {
        Collection collection = mCollectionModel.getCollection(collectionId);
        collection.addAllSpeakItems(mSpeakItemModel.getSpeakItems(collectionId));
        mView.onCollectionLoaded(collection);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
    }
}
