package com.drhowdydoo.appinfo.viewmodel;

import androidx.lifecycle.ViewModel;

import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppListViewModel extends ViewModel {

    private List<AppInfo> appInfoList = new ArrayList<>();
    private List<AppInfo> savedAppInfoList = new ArrayList<>();

    private int sortedState = Constants.DEFAULT_SORT;
    private int filterState = Constants.NO_FILTER;

    private boolean reverseSort = false;

    public List<AppInfo> getAppInfoList() {
        return appInfoList;
    }

    public void setAppInfoList(List<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    public List<AppInfo> getSavedAppInfoList() {
        return savedAppInfoList;
    }

    public void setSavedAppInfoList(List<AppInfo> savedAppInfoList) {
        this.savedAppInfoList = savedAppInfoList;
    }

    public int getSortedState() {
        return sortedState;
    }

    public void setSortedState(int sortedState) {
        this.sortedState = sortedState;
    }

    public int getFilterState() {
        return filterState;
    }

    public void setFilterState(int filterState) {
        this.filterState = filterState;
    }

    public boolean isReverseSort() {
        return reverseSort;
    }

    public void setReverseSort(boolean reverseSort) {
        this.reverseSort = reverseSort;
    }
}
