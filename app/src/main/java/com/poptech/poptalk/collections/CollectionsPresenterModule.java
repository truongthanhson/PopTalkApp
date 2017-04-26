package com.poptech.poptalk.collections;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Administrator on 27/04/2017.
 */
@Module
public class CollectionsPresenterModule {
    private CollectionsContract.View mView;

    public CollectionsPresenterModule(CollectionsContract.View view) {
        this.mView = view;
    }

    @Provides
    CollectionsContract.View provideCollectionsPresenterView(){
        return mView;
    }
}
