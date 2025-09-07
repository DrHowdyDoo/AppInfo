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
                String name1 = o1.getApkName();
                String name2 = o2.getApkName();
                if (name1 == null && name2 == null) return 0;
                if (name1 == null) return -1;
                if (name2 == null) return 1;
                return name1.compareToIgnoreCase(name2);

            case Constants.SORT_BY_SIZE:
                return Long.compare(o1.getApkSize(), o2.getApkSize());
        }
        return 0;
    }
}