package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private String appSearchText;
    private String apkSearchText;
    private boolean isSearchVisible = false;

    private final MutableLiveData<Boolean> showSplitApks = new MutableLiveData<>(false);

    public LiveData<Boolean> getShowSplitApks() {
        return showSplitApks;
    }

    public void setShowSplitApks(boolean show) {
        showSplitApks.setValue(show);
    }


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
