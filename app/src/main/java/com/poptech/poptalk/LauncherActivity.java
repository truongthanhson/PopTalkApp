
package com.poptech.poptalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.poptech.poptalk.collections.CollectionsActivity;
import com.poptech.poptalk.login.LoginActivity;
import com.poptech.poptalk.utils.SaveData;

public class LauncherActivity extends FragmentActivity {

    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SaveData.getInstance(PopTalkApplication.applicationContext).isLoggedIn()) {
                    Intent intent = new Intent(LauncherActivity.this, CollectionsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }, Constants.SPLASH_DISPLAY_LENGTH);
    }
}

