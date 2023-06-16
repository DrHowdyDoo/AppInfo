package com.drhowdydoo.appinfo.util;

import android.content.Context;

import java.util.Map;

public class Utilities {

    public static Map<String, String> sourcePackageMap = Map.ofEntries(
            Map.entry("com.android.vending", "Google PlayStore"),
            Map.entry("com.amazon.venezia", "Amazon App Store"),
            Map.entry("com.android.chrome", "Google Chrome"),
            Map.entry("com.google.android.packageinstaller","Package installer"),
            Map.entry("com.whatsapp", "Whatsapp"),
            Map.entry("org.mozilla.firefox", "Firefox"),
            Map.entry("com.google.android.apps.nbu.files", "Files by Google"),
            Map.entry("com.andatsoft.myapk.fwa", "My APK"),
            Map.entry("org.telegram.messenger", "Telegram"),
            Map.entry("com.google.android.apps.docs", "Google Drive"),
            Map.entry("com.microsoft.windowsintune.companyportal", "Intune"),
            Map.entry("com.brave.browser", "Brave"),
            Map.entry("com.google.android.gm", "Gmail"),
            Map.entry("com.termux", "Termux"),
            Map.entry("com.android.shell", "Shell"),
            Map.entry("debug", "Debug Build")
    );

    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
