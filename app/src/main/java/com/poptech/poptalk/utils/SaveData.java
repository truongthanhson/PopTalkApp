package com.poptech.poptalk.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveData {
    private static SaveData saveData = null;
    private SharedPreferences sharePreference;

    public SaveData(Context context) {
        sharePreference = context.getSharedPreferences("PopTalk", Context.MODE_PRIVATE);
    }

    public static SaveData getInstance(Context context) {
        if (saveData == null) {
            saveData = new SaveData(context);
        }
        return saveData;
    }

    public void reset() {
        sharePreference.edit().clear().commit();
        saveData = null;
    }

    public void setLoggedIn(boolean isLogin) {
        sharePreference.edit().putBoolean("isLogin", isLogin).commit();
    }

    public boolean isLoggedIn() {
        return sharePreference.getBoolean("isLogin", false);
    }
}
