package com.drhowdydoo.appinfo.util;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Utilities {

    public static Map<String, String> sourcePackageMap = Map.ofEntries(
            Map.entry("com.android.vending", "Google PlayStore"),
            Map.entry("com.amazon.venezia", "Amazon App Store"),
            Map.entry("com.android.chrome", "Google Chrome"),
            Map.entry("com.google.android.packageinstaller", "Package installer"),
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

    public static Map<Integer, String> androidVersions = Map.ofEntries(
            Map.entry(Build.VERSION_CODES.BASE, "Android 1.0/1.1"),
            Map.entry(Build.VERSION_CODES.CUPCAKE, "Android 1.5 Cupcake"),
            Map.entry(Build.VERSION_CODES.DONUT, "Android 1.6 Donut"),
            Map.entry(Build.VERSION_CODES.ECLAIR, "Android 2.0 Eclair"),
            Map.entry(Build.VERSION_CODES.ECLAIR_MR1, "Android 2.1 Eclair"),
            Map.entry(Build.VERSION_CODES.FROYO, "Android 2.2 Froyo"),
            Map.entry(Build.VERSION_CODES.GINGERBREAD, "Android 2.3 Gingerbread"),
            Map.entry(Build.VERSION_CODES.HONEYCOMB, "Android 3.0 Honeycomb"),
            Map.entry(Build.VERSION_CODES.HONEYCOMB_MR1, "Android 3.1 Honeycomb"),
            Map.entry(Build.VERSION_CODES.HONEYCOMB_MR2, "Android 3.2 Honeycomb"),
            Map.entry(Build.VERSION_CODES.ICE_CREAM_SANDWICH, "Android 4.0 Ice Cream Sandwich"),
            Map.entry(Build.VERSION_CODES.JELLY_BEAN, "Android 4.1 Jelly Bean"),
            Map.entry(Build.VERSION_CODES.JELLY_BEAN_MR1, "Android 4.2 Jelly Bean"),
            Map.entry(Build.VERSION_CODES.JELLY_BEAN_MR2, "Android 4.3 Jelly Bean"),
            Map.entry(Build.VERSION_CODES.KITKAT, "Android 4.4 KitKat"),
            Map.entry(Build.VERSION_CODES.LOLLIPOP, "Android 5.0 Lollipop"),
            Map.entry(Build.VERSION_CODES.LOLLIPOP_MR1, "Android 5.1 Lollipop"),
            Map.entry(Build.VERSION_CODES.M, "Android 6.0 Marshmallow"),
            Map.entry(Build.VERSION_CODES.N, "Android 7.0 Nougat"),
            Map.entry(Build.VERSION_CODES.N_MR1, "Android 7.1 Nougat"),
            Map.entry(Build.VERSION_CODES.O, "Android 8.0 Oreo"),
            Map.entry(Build.VERSION_CODES.O_MR1, "Android 8.1 Oreo"),
            Map.entry(Build.VERSION_CODES.P, "Android 9 Pie"),
            Map.entry(Build.VERSION_CODES.Q, "Android 10"),
            Map.entry(Build.VERSION_CODES.R, "Android 11"),
            Map.entry(Build.VERSION_CODES.S, "Android 12"),
            Map.entry(Build.VERSION_CODES.S_V2, "Android 12L"),
            Map.entry(Build.VERSION_CODES.TIRAMISU, "Android 13 TIRAMISU"),
            Map.entry(34, "Android 14 UpsideDownCake")
    );

    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(sourceFile);
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

}
