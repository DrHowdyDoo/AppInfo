package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.ViewModel;

import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ApkListViewModel extends ViewModel {

    private List<ApkInfo> apkInfoList = new ArrayList<>();

    private List<ApkInfo> savedApkInfoList = new ArrayList<>();

    private int sortState = Constants.SORT_BY_SIZE;

    private int filterState = Constants.NO_FILTER;

    public List<ApkInfo> getApkInfoList() {
        return apkInfoList;
    }

    public void setApkInfoList(List<ApkInfo> apkInfoList) {
        this.apkInfoList = apkInfoList;
    }

    public List<ApkInfo> getSavedApkInfoList() {
        return savedApkInfoList;
    }

    public void setSavedApkInfoList(List<ApkInfo> savedApkInfoList) {
        this.savedApkInfoList = savedApkInfoList;
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
