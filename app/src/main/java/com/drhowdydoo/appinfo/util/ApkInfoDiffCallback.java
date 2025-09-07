package com.drhowdydoo.appinfo.util;

import androidx.recyclerview.widget.DiffUtil;

import com.drhowdydoo.appinfo.model.ApkInfo;

import java.util.List;

public class ApkInfoDiffCallback extends DiffUtil.Callback {

    private List<ApkInfo> mOldApkInfoList;
    private List<ApkInfo> mNewApkInfoList;

    public ApkInfoDiffCallback(List<ApkInfo> mOldApkInfoList, List<ApkInfo> mNewApkInfoList) {
        this.mOldApkInfoList = mOldApkInfoList;
        this.mNewApkInfoList = mNewApkInfoList;
    }

    @Override
    public int getOldListSize() {
        return mOldApkInfoList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewApkInfoList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldApkInfoList.get(oldItemPosition).getApkInfo() != null &&
                mNewApkInfoList.get(newItemPosition).getApkInfo() != null &&
                mOldApkInfoList.get(oldItemPosition).getApkInfo().packageName
                        .equalsIgnoreCase(mNewApkInfoList.get(newItemPosition).getApkInfo().packageName);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldApkInfoList.get(oldItemPosition).equals(mNewApkInfoList.get(newItemPosition));
    }
}
