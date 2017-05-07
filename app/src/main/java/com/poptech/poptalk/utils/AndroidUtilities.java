package com.poptech.poptalk.utils;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

import com.poptech.poptalk.PopTalkApplication;

/**
 * Created by sontt on 26/02/2017.
 */

public class AndroidUtilities {
    public static float density = 1;

    static {
        density = PopTalkApplication.applicationContext.getResources().getDisplayMetrics().density;
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            PopTalkApplication.applicationHandler.post(runnable);
        } else {
            PopTalkApplication.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


    public static float getPixelsInCM(Activity pActivity, float cm, boolean isX) {
        if (pActivity == null) {
            throw new NullPointerException("Utils::getDisplayWidth pActivity is null");
        }
        DisplayMetrics metrics = new DisplayMetrics();
        pActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (cm / 2.54f) * (isX ? metrics.xdpi : metrics.ydpi);
    }
}
