package com.drhowdydoo.appinfo.model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

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

    public AppInfo(String appName,
                   Drawable appIcon,
                   long appLastUpdateTime,
                   ApplicationInfo appInfo,
                   boolean isSplitApp,
                   String appVersion, boolean isSystemApp) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.appLastUpdateTime = appLastUpdateTime;
        this.appInfo = appInfo;
        this.isSplitApp = isSplitApp;
        this.appVersion = appVersion;
        this.isSystemApp = isSystemApp;
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
}
