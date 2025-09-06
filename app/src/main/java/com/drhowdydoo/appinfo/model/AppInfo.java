package com.drhowdydoo.appinfo.model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import java.util.Objects;

public class AppInfo {

    private String appName;
    private Drawable appIcon;
    private long appLastUpdateTime;
    private long size;
    private ApplicationInfo appInfo;
    private long lastTimeUsed;
    private boolean isSplitApp;
    private String appVersion;
    private boolean isSystemApp;
    private long totalForegroundTime;
    private String packageName;

    private long installTmStamp;

    private String installSource;

    public AppInfo(String appName,
                   Drawable appIcon,
                   long appLastUpdateTime,
                   ApplicationInfo appInfo,
                   boolean isSplitApp,
                   String appVersion,
                   boolean isSystemApp,
                   String packageName, long installTmStamp, String installSource) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.appLastUpdateTime = appLastUpdateTime;
        this.appInfo = appInfo;
        this.isSplitApp = isSplitApp;
        this.appVersion = appVersion;
        this.isSystemApp = isSystemApp;
        this.packageName = packageName;
        this.installTmStamp = installTmStamp;
        this.installSource = installSource;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public long getAppLastUpdateTime() {
        return appLastUpdateTime;
    }

    public void setAppLastUpdateTime(long appLastUpdateTime) {
        this.appLastUpdateTime = appLastUpdateTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed(long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }

    public boolean isSplitApp() {
        return isSplitApp;
    }

    public void setSplitApp(boolean splitApp) {
        isSplitApp = splitApp;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public long getTotalForegroundTime() {
        return totalForegroundTime;
    }

    public void setTotalForegroundTime(long totalForegroundTime) {
        this.totalForegroundTime = totalForegroundTime;
    }

    public long getInstallTmStamp() {
        return installTmStamp;
    }

    public void setInstallTmStamp(long installTmStamp) {
        this.installTmStamp = installTmStamp;
    }
    public String getInstallSource() {
        return installSource;
    }

    public void setInstallSource(String installSource) {
        this.installSource = installSource;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInfo appInfo1 = (AppInfo) o;
        return appLastUpdateTime == appInfo1.appLastUpdateTime && size == appInfo1.size && lastTimeUsed == appInfo1.lastTimeUsed && isSplitApp == appInfo1.isSplitApp && isSystemApp == appInfo1.isSystemApp && Objects.equals(appName, appInfo1.appName) && Objects.equals(appIcon, appInfo1.appIcon) && Objects.equals(appInfo, appInfo1.appInfo) && Objects.equals(appVersion, appInfo1.appVersion) && totalForegroundTime == appInfo1.totalForegroundTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appIcon, appLastUpdateTime, size, appInfo, lastTimeUsed, isSplitApp, appVersion, isSystemApp, totalForegroundTime);
    }
}
