package com.poptech.poptalk;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.util.Base64;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.AppModule;
import com.poptech.poptalk.di.DaggerAppComponent;
import com.poptech.poptalk.utils.SaveData;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sontt on 25/04/2017.
 */

public class PopTalkApplication extends Application {
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private AppComponent mAppComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        applicationContext = this;
        applicationHandler = new Handler(applicationContext.getMainLooper());
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(applicationContext)).build();
        FacebookSdk.sdkInitialize(getApplicationContext());
        NativeLoader.initNativeLibs(applicationContext);
        FFmpeg ffmpeg = FFmpeg.getInstance(applicationContext);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
        }

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.poptech.poptalk", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
