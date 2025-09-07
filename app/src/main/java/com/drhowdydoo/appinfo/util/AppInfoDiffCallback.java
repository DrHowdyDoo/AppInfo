package com.drhowdydoo.appinfo.util;

import androidx.recyclerview.widget.DiffUtil;

import com.drhowdydoo.appinfo.model.AppInfo;

import java.util.List;

public class AppInfoDiffCallback extends DiffUtil.Callback {

    private final List<AppInfo> mOldAppInfoList;
    private final List<AppInfo> mNewAppInfoList;

    public AppInfoDiffCallback(List<AppInfo> mOldAppInfoList, List<AppInfo> mNewAppInfoList) {
        this.mOldAppInfoList = mOldAppInfoList;
        this.mNewAppInfoList = mNewAppInfoList;
    }

    @Override
    public int getOldListSize() {
        return mOldAppInfoList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewAppInfoList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldAppInfoList.get(oldItemPosition).getAppName() != null &&
                mNewAppInfoList.get(newItemPosition).getAppName() != null &&
                mOldAppInfoList.get(oldItemPosition).getAppName()
                        .equalsIgnoreCase(mNewAppInfoList.get(newItemPosition).getAppName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldAppInfoList.get(oldItemPosition).equals(mNewAppInfoList.get(newItemPosition));
    }
}
