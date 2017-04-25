package com.poptech.poptalk;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.AppModule;
import com.poptech.poptalk.di.DaggerAppComponent;

/**
 * Created by sontt on 25/04/2017.
 */

public class PopTalkApplication extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        applicationHandler = new Handler(applicationContext.getMainLooper());
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(applicationContext)).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
