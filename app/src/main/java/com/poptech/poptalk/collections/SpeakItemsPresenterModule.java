package com.poptech.poptalk.collections;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 27/04/2017.
 */
@Module
public class SpeakItemsPresenterModule {
    private SpeakItemsContract.View mView;

    public SpeakItemsPresenterModule(SpeakItemsContract.View view) {
        this.mView = view;
    }

    @Provides
    SpeakItemsContract.View providesSpeakItemsPresenterView(){
        return mView;
    }
}
