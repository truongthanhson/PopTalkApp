package com.poptech.poptalk.utils;

import android.content.Context;
import android.view.View;

import com.poptech.poptalk.R;


public class AnimationUtils {
	public static void shake(Context context, View view) {
		android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake);
		view.startAnimation(shake);
		view.requestFocus();
	}
}
