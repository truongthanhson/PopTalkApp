package com.poptech.poptalk.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 04/03/2017.
 */

@Module
public class AppModule {
    Context mApplication;

    public AppModule(Context application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    Context providesApplication(){
        return mApplication;
    }
}
