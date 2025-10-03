package com.drhowdydoo.appinfo.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Utilities {

    public static final Map<String, String> sourcePackageMap = Map.ofEntries(
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

    public static final Map<Integer, String> androidVersions = Map.ofEntries(
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
            Map.entry(34, "Android 14 UpsideDownCake"),
            Map.entry(35, "Android 15 VanillaIceCream"),
            Map.entry(36, "Android 16 Baklava")
    );


    public final static Set<String> skipDirectoriesSet = new HashSet<>(Arrays.asList("data",
            "Recordings",
            "Audiobooks",
            "Notifications",
            "Alarms",
            "Ringtones",
            "Podcasts",
            "Music",
            "DCIM",
            "Movies",
            "Pictures",
            "Subtitles",
            "DigiLocker",
            "LMC",
            "Wallpapers"));


    public static boolean shouldSearchApks = true;

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

    public static void extractFile(ZipInputStream zipIn, String destinationPath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationPath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static File extractToTempFile(ZipFile zipFile, ZipEntry entry, Context context) throws IOException {
        File tempFile = File.createTempFile(getNameWithoutExtension(entry.getName()), getExtension(entry.getName()), context.getCacheDir());

        try (InputStream inputStream = zipFile.getInputStream(entry);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return tempFile;
    }

    public static String getNameWithoutExtension(String fileName) {
        if (fileName == null) return "";
        int indexOfDot = fileName.lastIndexOf(".");
        if (indexOfDot == -1) return fileName;
        return fileName.substring(0, indexOfDot);
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int indexOfDot = fileName.lastIndexOf(".");
        if (indexOfDot == -1) return "";
        return fileName.substring(indexOfDot).toLowerCase();
    }

    public static PackageInfo getBundleApkPackageInfo(Context context, PackageManager packageManager, String apkFilePath) {
        if (apkFilePath == null) return null;
        try (ZipFile zipFile = new ZipFile(apkFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Look for base.apk or other APK files
                if (!isSplitApk(entry.getName()) && entry.getName().endsWith(".apk")) {
                    // Extract to temporary file and analyze
                    File tempApk = extractToTempFile(zipFile, entry, context);
                    PackageInfo info = packageManager.getPackageArchiveInfo(
                            tempApk.getAbsolutePath(),
                            Constants.packageManagerFlags);
                    boolean deleteSuccess = tempApk.delete();
                    if (!deleteSuccess) tempApk.deleteOnExit();
                    return info;
                }
            }
        } catch (IOException e) {
            Log.e("ApksAnalyzer", "Error analyzing APKS file", e);
        }
        return null;
    }

    public static boolean isSplitApk(String fileName){
        if (fileName == null) return false;
        return fileName.startsWith("split_") || fileName.startsWith("config.");
    }

    public static boolean isInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) return null;

        try {
            // If already a BitmapDrawable, just return its bitmap
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null) return bitmap;
            }

            // Otherwise, render drawable into a bitmap
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            // fallback size if drawable has no intrinsic size (some vectors/adaptive icons)
            if (width <= 0) width = 128;
            if (height <= 0) height = 128;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
