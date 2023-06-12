package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.ViewModel;

import com.drhowdydoo.appinfo.model.ApkInfo;

import java.util.ArrayList;
import java.util.List;

public class ApkListViewModel extends ViewModel {

    private List<ApkInfo> apkInfoList = new ArrayList<>();

    public List<ApkInfo> getApkInfoList() {
        return apkInfoList;
    }

    public void setApkInfoList(List<ApkInfo> apkInfoList) {
        this.apkInfoList = apkInfoList;
    }
}
