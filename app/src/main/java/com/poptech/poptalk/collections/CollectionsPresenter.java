package com.poptech.poptalk.collections;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class CollectionsPresenter implements CollectionsContract.Presenter {
    private CollectionsContract.View mView;
    private CollectionsModel mModel;

    @Inject
    public CollectionsPresenter(CollectionsModel model, CollectionsContract.View view) {
        this.mModel = model;
        this.mView = view;
    }

    @Override
    public void start() {

    }
}
