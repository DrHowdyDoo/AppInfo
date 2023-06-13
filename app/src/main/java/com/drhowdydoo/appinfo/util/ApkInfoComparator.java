package com.drhowdydoo.appinfo.util;

import com.drhowdydoo.appinfo.model.ApkInfo;

import java.util.Comparator;

public class ApkInfoComparator implements Comparator<ApkInfo> {

    private int sort_by;

    public ApkInfoComparator(int sort_by) {
        this.sort_by = sort_by;
    }


    @Override
    public int compare(ApkInfo o1, ApkInfo o2) {
        switch (sort_by) {
            case Constants.SORT_BY_NAME:
                return o1.getApkName().compareToIgnoreCase(o2.getApkName());

            case Constants.SORT_BY_SIZE:
                return Long.compare(o1.getApkSize(), o2.getApkSize());

        }
        return 0;
    }
}