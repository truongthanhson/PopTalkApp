package com.poptech.poptalk;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by sontt on 28/04/2017.
 */

public class Constants {
    public static final String TAG = "Constants";

    public static final String KEY_PHOTO_GALLERY = "key_photo_gallery";

    public static final String KEY_SPEAK_ITEM_ID = "key_speak_item_id";

    public static final String KEY_COLLECTION_ID = "key_collection_id";

    public static final String KEY_SPEAK_ITEM_VIEW_TYPE = "key_speak_item_view_type";

    public static final String KEY_SPEAK_ITEM_SORT_TYPE = "key_speak_item_sort_type";

    public static final int REQUEST_AVATAR_CAPTURE = 0;

    public static final String PATH_APP = "/PopTalk";

    public static final String PATH_PHOTO = "/photo";
    public static final String PATH_AUDIO = "/audio";

    public static final int MAX_AUDIO_RECORD_TIME_MS = 600000;

    public static final int GRID_COLUMN_COUNT = 3;

    public static boolean DEBUG_VERSION = false;

    public static String FILES_PATH = null;

    public static String APP_VERSION = null;

    public static String APP_VERSION_NAME = null;

    public static String APP_PACKAGE = null;

    public static String ANDROID_VERSION = null;

    public static String ANDROID_BUILD = null;

    public static String PHONE_MODEL = null;

    public static String PHONE_MANUFACTURER = null;

    public static String CRASH_IDENTIFIER = null;

    public static String DEVICE_IDENTIFIER = null;

    public static void loadFromContext(Context context) {
        ANDROID_VERSION = Build.VERSION.RELEASE;
        ANDROID_BUILD = Build.DISPLAY;
        PHONE_MODEL = Build.MODEL;
        PHONE_MANUFACTURER = Build.MANUFACTURER;
        loadFilesPath(context);
        loadPackageData(context);
        loadCrashIdentifier(context);
        loadDeviceIdentifier(context);
    }

    private static void loadFilesPath(Context context) {
        if (context != null) {
            try {
                File e = context.getFilesDir();
                if (e != null) {
                    FILES_PATH = e.getAbsolutePath();
                }
            } catch (Exception var2) {
                Log.e(TAG, var2.toString());
                var2.printStackTrace();
            }
        }

    }

    private static void loadPackageData(Context context) {
        if (context != null) {
            try {
                PackageManager e = context.getPackageManager();
                PackageInfo packageInfo = e.getPackageInfo(context.getPackageName(), 0);
                APP_PACKAGE = packageInfo.packageName;
                APP_VERSION = "" + packageInfo.versionCode;
                APP_VERSION_NAME = packageInfo.versionName;
                int buildNumber = loadBuildNumber(context, e);
                if (buildNumber != 0 && buildNumber > packageInfo.versionCode) {
                    APP_VERSION = "" + buildNumber;
                }
            } catch (PackageManager.NameNotFoundException var4) {
                Log.e(TAG, var4.toString());
                var4.printStackTrace();
            }
        }

    }

    private static int loadBuildNumber(Context context, PackageManager packageManager) {
        try {
            ApplicationInfo e = packageManager.getApplicationInfo(context.getPackageName(), 128);
            Bundle metaData = e.metaData;
            if (metaData != null) {
                return metaData.getInt("buildNumber", 0);
            }
        } catch (PackageManager.NameNotFoundException var4) {
            Log.e(TAG, var4.toString());
            var4.printStackTrace();
        }

        return 0;
    }

    private static void loadCrashIdentifier(Context context) {
        String deviceIdentifier = Settings.Secure.getString(context.getContentResolver(), "android_id");
        if (!TextUtils.isEmpty(APP_PACKAGE) && !TextUtils.isEmpty(deviceIdentifier)) {
            String combined = APP_PACKAGE + ":" + deviceIdentifier + ":" + createSalt(context);

            try {
                MessageDigest e = MessageDigest.getInstance("SHA-1");
                byte[] bytes = combined.getBytes("UTF-8");
                e.update(bytes, 0, bytes.length);
                bytes = e.digest();
                CRASH_IDENTIFIER = bytesToHex(bytes);
            } catch (Throwable var5) {
                Log.e(TAG, var5.toString());
            }
        }

    }

    private static void loadDeviceIdentifier(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, "android_id");
        if (deviceIdentifier != null) {
            String deviceIdentifierAnonymized = tryHashStringSha256(context, deviceIdentifier);
            DEVICE_IDENTIFIER = deviceIdentifierAnonymized != null ? deviceIdentifierAnonymized : UUID.randomUUID().toString();
        }

    }

    private static String tryHashStringSha256(Context context, String input) {
        String salt = createSalt(context);

        try {
            MessageDigest e = MessageDigest.getInstance("SHA-256");
            e.reset();
            e.update(input.getBytes());
            e.update(salt.getBytes());
            byte[] hashedBytes = e.digest();
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException var5) {
            return null;
        }
    }

    @SuppressLint({"InlinedApi"})
    private static String createSalt(Context context) {
        String abiString;
        if (Build.VERSION.SDK_INT >= 21) {
            abiString = Build.SUPPORTED_ABIS[0];
        } else {
            abiString = Build.CPU_ABI;
        }

        String fingerprint = "HA" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + abiString.length() % 10 + Build.PRODUCT.length() % 10;
        String serial = "";

        try {
            serial = Build.class.getField("SERIAL").get((Object) null).toString();
        } catch (Throwable var5) {
            ;
        }

        return fingerprint + ":" + serial;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hex = new char[bytes.length * 2];

        for (int result = 0; result < bytes.length; ++result) {
            int value = bytes[result] & 255;
            hex[result * 2] = HEX_ARRAY[value >>> 4];
            hex[result * 2 + 1] = HEX_ARRAY[value & 15];
        }

        String var5 = new String(hex);
        return var5.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }
}
