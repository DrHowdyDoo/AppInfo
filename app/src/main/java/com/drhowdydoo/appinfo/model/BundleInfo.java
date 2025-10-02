package com.drhowdydoo.appinfo.model;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class BundleInfo {

    private PackageInfo packageInfo;
    private Drawable icon;

    public BundleInfo(PackageInfo packageInfo, Drawable icon) {
        this.packageInfo = packageInfo;
        this.icon = icon;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
}
