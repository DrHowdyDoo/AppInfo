package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    @SuppressLint("WrongConstant")
    public static void applyTheme(Context context, int mode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                mode = uiModeManager.getNightMode();
            }
            uiModeManager.setApplicationNightMode(mode);
        } else {
            AppCompatDelegate.setDefaultNightMode(mode);
        }

    }

    public static int getColorAttr(Context context, @AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }
}


