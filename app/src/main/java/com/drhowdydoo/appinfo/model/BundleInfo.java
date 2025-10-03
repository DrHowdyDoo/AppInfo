package com.drhowdydoo.appinfo.model;

import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class BundleInfo {

    private PackageInfo packageInfo;
    private Bitmap icon;

    public BundleInfo(PackageInfo packageInfo, Bitmap icon) {
        this.packageInfo = packageInfo;
        this.icon = icon;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
}
