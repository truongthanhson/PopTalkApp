package com.poptech.poptalk.speakitem;

import dagger.Module;
import dagger.Provides;

@Module
public class SpeakItemDetailPresenterModule {
    private SpeakItemDetailContract.View mView;

    public SpeakItemDetailPresenterModule(SpeakItemDetailContract.View view) {
        this.mView = view;
    }

    @Provides
    SpeakItemDetailContract.View providesSpeakItemsPresenterView() {
        return mView;
    }
}
