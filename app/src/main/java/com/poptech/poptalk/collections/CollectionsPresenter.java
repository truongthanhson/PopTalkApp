package com.poptech.poptalk.collections;

import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class CollectionsPresenter implements CollectionsContract.Presenter {
    private CollectionsContract.View mView;
    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;
    private List<Collection> allCollections;

    @Inject
    public CollectionsPresenter(SpeakItemModel speakItemModel, CollectionsModel collectionsModel, CollectionsContract.View view) {
        this.mSpeakItemModel = speakItemModel;
        this.mCollectionModel = collectionsModel;
        this.mView = view;
    }

    @Inject
    public void setupListeners(){
        mView.setPresenter(this);
    }


    @Override
    public void start() {
        mView.setPresenter(this);
    }

    @Override
    public void loadCollections() {
        //mModel.generateTestData();
        List<Collection> collections = mCollectionModel.getCollections();
        for (int i = 0; i < collections.size(); i++) {
            long collectionId = collections.get(i).getId();
            collections.get(i).addAllSpeakItems(mSpeakItemModel.getSpeakItems(collectionId));
        }
        mView.onCollectionsLoaded(collections);

    }
}
