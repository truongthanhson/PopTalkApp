package com.poptech.poptalk;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.AppModule;
import com.poptech.poptalk.di.DaggerAppComponent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        FacebookSdk.sdkInitialize(getApplicationContext());
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
