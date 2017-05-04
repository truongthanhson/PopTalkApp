package com.poptech.poptalk.collections;

import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.provider.CollectionsModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class CollectionsPresenter implements CollectionsContract.Presenter {
    private CollectionsContract.View mView;
    private CollectionsModel mModel;
    private List<Collection> allCollections;

    @Inject
    public CollectionsPresenter(CollectionsModel model, CollectionsContract.View view) {
        this.mModel = model;
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
        List<Collection> collections = mModel.getCollections();
        mView.onCollectionsLoaded(collections);

    }
}
