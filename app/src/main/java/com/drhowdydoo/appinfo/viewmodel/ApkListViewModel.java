package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.ViewModel;

import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ApkListViewModel extends ViewModel {

    private List<ApkInfo> apkInfoList = new ArrayList<>();

    private List<ApkInfo> savedApkInfo = new ArrayList<>();

    private int sortState = Constants.SORT_BY_SIZE;

    private int filterState = Constants.NO_FILTER;

    public List<ApkInfo> getApkInfoList() {
        return apkInfoList;
    }

    public void setApkInfoList(List<ApkInfo> apkInfoList) {
        this.apkInfoList = apkInfoList;
    }

    public List<ApkInfo> getSavedApkInfo() {
        return savedApkInfo;
    }

    public void setSavedApkInfo(List<ApkInfo> savedApkInfo) {
        this.savedApkInfo = savedApkInfo;
    }

    public int getSortState() {
        return sortState;
    }

    public void setSortState(int sortState) {
        this.sortState = sortState;
    }

    public int getFilterState() {
        return filterState;
    }

    public void setFilterState(int filterState) {
        this.filterState = filterState;
    }
}
