package com.drhowdydoo.appinfo.model;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class ApkInfo {

    private String apkName;
    private Drawable apkIcon;
    private long apkSize;
    private String apkPath;
    private String apkAbsolutePath;
    private String apkVersion;
    private PackageInfo apkInfo;

    private boolean isInstalled;

    public ApkInfo(String apkName,
                   Drawable apkIcon,
                   long apkSize,
                   String apkPath,
                   String apkAbsolutePath, String apkVersion,
                   PackageInfo apkInfo, boolean isInstalled) {

        this.apkName = apkName;
        this.apkIcon = apkIcon;
        this.apkSize = apkSize;
        this.apkPath = apkPath;
        this.apkAbsolutePath = apkAbsolutePath;
        this.apkVersion = apkVersion;
        this.apkInfo = apkInfo;
        this.isInstalled = isInstalled;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public Drawable getApkIcon() {
        return apkIcon;
    }

    public void setApkIcon(Drawable apkIcon) {
        this.apkIcon = apkIcon;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getApkAbsolutePath() {
        return apkAbsolutePath;
    }

    public void setApkAbsolutePath(String apkAbsolutePath) {
        this.apkAbsolutePath = apkAbsolutePath;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }

    public PackageInfo getApkInfo() {
        return apkInfo;
    }

    public void setApkInfo(PackageInfo apkInfo) {
        this.apkInfo = apkInfo;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }
}
