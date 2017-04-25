package com.poptech.poptalk.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Administrator on 04/03/2017.
 */

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
    Context getApplicationContext();
}
