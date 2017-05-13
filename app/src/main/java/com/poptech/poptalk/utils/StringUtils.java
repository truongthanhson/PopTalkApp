package com.poptech.poptalk.utils;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by Administrator on 02/03/2017.
 */

public class StringUtils {
    public static boolean isNullOrEmpty(String input){
        return TextUtils.isEmpty(input);
    }

    public static boolean isContainIgnoreCase(String input, List<String> array){
        if(array == null || input == null) {
            return false;
        }
        for(String string : array) {
            if(string.trim().equalsIgnoreCase(input.trim())) {
                return true;
            }
        }
        return false;
    }

}
