package com.poptech.poptalk.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Truong Thanh Son on 11/6/2015.
 */
public class AppPrefrence {
    /**
     * The save data.
     */
    private static AppPrefrence appPrefrence = null;

    /**
     * The share preference.
     */
    private SharedPreferences sharePreference;

    /**
     * Instantiates a new save data.
     *
     * @param context the context
     */
    public AppPrefrence(Context context) {
        sharePreference = context.getSharedPreferences("PhotoSound", Context.MODE_PRIVATE);
    }

    /**
     * Gets the single instance of AppPrefrence.
     *
     * @param context the context
     * @return single instance of AppPrefrence
     */
    public static AppPrefrence getInstance(Context context) {
        if (appPrefrence == null) {
            appPrefrence = new AppPrefrence(context);
        }
        return appPrefrence;
    }

    public void reset() {
        sharePreference.edit().clear().commit();
        appPrefrence = null;
    }

    /**
     * Sets the internet connecting.
     *
     * @param isConnected the new internet connecting
     */
    public void setInternetConnecting(boolean isConnected) {
        sharePreference.edit().putBoolean("network", isConnected).commit();
    }

    /**
     * Checks if is internet connecting.
     *
     * @return true, if is internet connecting
     */
    public boolean isInternetConnecting() {
        return sharePreference.getBoolean("network", false);
    }

    public void setLoggedIn(boolean isLogin) {
        sharePreference.edit().putBoolean("isLogin", isLogin).commit();
    }

    public boolean isLoggedIn() {
        return sharePreference.getBoolean("isLogin", false);
    }
}
