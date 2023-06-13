package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private String appSearchText;
    private String apkSearchText;
    private boolean isSearchVisible = false;

    public String getAppSearchText() {
        return appSearchText;
    }

    public void setAppSearchText(String appSearchText) {
        this.appSearchText = appSearchText;
    }

    public String getApkSearchText() {
        return apkSearchText;
    }

    public void setApkSearchText(String apkSearchText) {
        this.apkSearchText = apkSearchText;
    }

    public boolean isSearchVisible() {
        return isSearchVisible;
    }

    public void setSearchVisible(boolean searchVisible) {
        isSearchVisible = searchVisible;
    }
}
