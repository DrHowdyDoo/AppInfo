package com.drhowdydoo.appinfo.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class ApkExtractor {

    public static void extractApk(String appName, PackageInfo packageInfo) {

        try {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            String sourceDir = appInfo.publicSourceDir;
            File destinationRootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AppInfo");
            File destinationAppFolder = new File(destinationRootFolder, appName);
            boolean created = destinationAppFolder.mkdirs();

            // Check if the app has split APKs
            String[] splitSourceDirs = appInfo.splitPublicSourceDirs;
            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                for (String splitSourceDir : splitSourceDirs) {
                    extractApkFileAtPath(splitSourceDir, destinationAppFolder);
                }
            }

            extractApkFileAtPath(sourceDir, destinationAppFolder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extractApkFileAtPath(String apkFilePath, File destinationAppFolder) {
        try {
            File sourceFile = new File(apkFilePath);
            String appName = sourceFile.getName().toLowerCase().startsWith("base") ? destinationAppFolder.getName() + ".apk" : sourceFile.getName();
            File destinationFile = new File(destinationAppFolder, appName);
            Utilities.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
