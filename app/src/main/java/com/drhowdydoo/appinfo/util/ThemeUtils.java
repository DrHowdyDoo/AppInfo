package com.drhowdydoo.appinfo.util;

import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    public static void applyTheme(Context context, int mode) {

        int api = Build.VERSION.SDK_INT;
        if (api >= Build.VERSION_CODES.S) {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                mode = uiModeManager.getNightMode();
            }
            uiModeManager.setApplicationNightMode(mode);
        } else {
            AppCompatDelegate.setDefaultNightMode(mode);
        }

    }
}


