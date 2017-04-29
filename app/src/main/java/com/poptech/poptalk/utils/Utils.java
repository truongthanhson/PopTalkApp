
package com.poptech.poptalk.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ParcelFileDescriptor;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.poptech.poptalk.preference.AppPrefrence;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static String getDeviceUDID(Context context) {
        String IMEI = "";
        if (context != null) {
            if (IMEI == null || IMEI.compareTo("") == 0
                    || IMEI.compareTo("null") == 0) {
                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String udid = "";
                udid = telephonyManager.getDeviceId();
                if (udid == null || udid.toString().compareTo("") == 0) {
                    udid = Secure.getString(context.getContentResolver(),
                            Secure.ANDROID_ID);
                }
                IMEI = udid;
            }
        }
        return IMEI;
    }

    public static void setTypeface(Context context, String fontName, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    /**
     * Sets the typeface roboto.
     *
     * @param context the context
     * @param views   the views
     */
    public static void setTypefaceRoboto(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    public static void setTypefaceRobotoItalic(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    /**
     * Sets the typeface roboto regular.
     *
     * @param context the context
     * @param views   the views
     */
    public static void setTypefaceRobotoRegular(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    /**
     * Sets the typeface roboto regular italic.
     *
     * @param context the context
     * @param views   the views
     */
    public static void setTypefaceRobotoRegularItalic(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font, Typeface.ITALIC);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font, Typeface.ITALIC);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font, Typeface.ITALIC);
                }
            }
        }
    }


    /**
     * Sets the typeface roboto medium.
     *
     * @param context the context
     * @param views   the views
     */
    public static void setTypefaceRobotoMedium(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    /**
     * Sets the typeface roboto bold.
     *
     * @param context the context
     * @param views   the views
     */
    public static void setTypefaceRobotoBold(Context context, View... views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(font);
                } else if (view instanceof EditText) {
                    ((EditText) view).setTypeface(font);
                } else if (view instanceof Button) {
                    ((Button) view).setTypeface(font);
                }
            }
        }
    }

    public static int getDisplayWidth(Activity pActivity) {
        if (pActivity == null) {
            throw new NullPointerException("Utils::getDisplayWidth pActivity is null");
        }
        DisplayMetrics metrics = new DisplayMetrics();
        pActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * Gets the display height.
     *
     * @param pActivity the activity
     * @return the display height
     */
    public static int getDisplayHeight(Activity pActivity) {
        if (pActivity == null) {
            throw new NullPointerException("Utils::getDisplayHeight pActivity is null");
        }
        DisplayMetrics metrics = new DisplayMetrics();
        pActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * Merge arr to string.
     *
     * @param list         the list
     * @param splitPattern the split pattern
     * @return the string
     */
    public static String mergeArrToString(List<String> list, String splitPattern) {
        String text = "";
        if (list != null) {
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    text += list.get(i);
                    if (list.size() > 1 && i < list.size() - 1) {
                        text += splitPattern;
                    }
                }
            }
        }
        return text;
    }

    /**
     * Split to array list.
     *
     * @param input   the input
     * @param pattern the pattern
     * @return the array list
     */
    public static ArrayList<String> splitToArrayList(String input, String pattern) {
        ArrayList<String> list = new ArrayList<String>();
        if (!TextUtils.isEmpty(input)) {
            String[] arr = input.split(pattern);
            if (arr.length > 0) {
                Collections.addAll(list, arr);
            } else if (arr.length == 0) {
                list.add(input);
            }
        }
        return list;
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                String message =
                        "File "
                                + directory
                                + " exists and is "
                                + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                String message =
                        "Unable to create directory " + directory;
                throw new IOException(message);
            }
        }
    }

    public static boolean writeBitmapToFile(Bitmap bitmap, String filename,
                                            String format) {
        boolean rv = false;
        if (bitmap == null) {
            return rv;
        }
        // Currently Android only support two formats: PNG and JPEG
        Bitmap.CompressFormat imgFormat;
        if (format.equalsIgnoreCase("png")) {
            imgFormat = Bitmap.CompressFormat.PNG;
        } else if (format.equalsIgnoreCase("jpeg")
                || format.equalsIgnoreCase("jpg")) {
            imgFormat = Bitmap.CompressFormat.JPEG;
        } else {
            return rv;
        }
        try {
            FileOutputStream out = new FileOutputStream(filename);
            rv = bitmap.compress(imgFormat, 70, out);
            out.close();
        } catch (IOException e) {
            return false;
        }
        return rv;
    }

    public static float convertDPToPixel(Context context, float dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return ((int) (dps * scale + 0.5f));
    }

    public static void checkInternetConnection(Context context) {
        boolean isConnected = false;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        AppPrefrence.getInstance(context).setInternetConnecting(isConnected);
    }

    public static Bitmap loadBitmap(String path, float maxWidth, float maxHeight, boolean useMaxScale) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        FileDescriptor fileDescriptor = null;
        ParcelFileDescriptor parcelFD = null;


        if (path != null) {
            BitmapFactory.decodeFile(path, bmOptions);
        }
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        float scaleFactor = useMaxScale ? Math.max(photoW / maxWidth, photoH / maxHeight) : Math.min(photoW / maxWidth, photoH / maxHeight);
        if (scaleFactor < 1) {
            scaleFactor = 1;
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;

        String exifPath = null;
        if (path != null) {
            exifPath = path;
        }

        Matrix matrix = null;

        if (exifPath != null) {
            ExifInterface exif;
            try {
                exif = new ExifInterface(exifPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        Bitmap b = null;
        if (path != null) {
            try {
                b = BitmapFactory.decodeFile(path, bmOptions);
                if (b != null) {
                    Bitmap newBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    if (newBitmap != b) {
                        b.recycle();
                        b = newBitmap;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return b;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        Bitmap iBitmap;
        int iWidth = reqWidth;
        int iHeight = reqHeight;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(path, options);
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == 1) {
                iWidth = reqWidth;
                iHeight = reqHeight;
            } else if (orientation == 6) {
                iWidth = reqHeight;
                iHeight = reqWidth;
            } else if (orientation == 3) {
                iWidth = reqWidth;
                iHeight = reqHeight;
            } else if (orientation == 8) {
                iWidth = reqHeight;
                iHeight = reqWidth;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, iWidth, iHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        iBitmap = BitmapFactory.decodeFile(path, options);

        return iBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            } else {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            }
        }

        return inSampleSize;
    }

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static List<String> getLanguages() {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> languages = new ArrayList<>();
        for (Locale l : locales) {
            String language = l.getDisplayLanguage();
            if (!StringUtils.isNullOrEmpty(language)) {
                String[] regexChars = {"\\s+", "\\s*-\\s*", "\\s*'\\s*"};
                String space = " ";
                for (String regex : regexChars) {
                    language = language.replaceAll(regex, space);
                }
                language = language.replaceAll("^\\s+", "");
                language = language.replaceAll("\\s+$", "");
                languages.add(language);
            }

        }
        List<String> sortedLanguages = new ArrayList<>(new HashSet<>(languages));
        Collections.sort(sortedLanguages, String.CASE_INSENSITIVE_ORDER);
        return sortedLanguages;
    }

    public static void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(1000);
        v.startAnimation(anim);
    }

    public static String formatTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }
}
