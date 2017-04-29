package com.poptech.poptalk.utils;

import android.text.TextUtils;

/**
 * Created by Administrator on 02/03/2017.
 */

public class StringUtils {
    public static boolean isNullOrEmpty(String input){
        return TextUtils.isEmpty(input);
    }
}
