package com.drhowdydoo.appinfo.util;

import android.content.pm.PackageManager;

import com.drhowdydoo.appinfo.model.AppInfo;

import java.util.Comparator;

public class AppInfoComparator implements Comparator<AppInfo> {

    private int sort_by;

    public AppInfoComparator(int sort_by) {
        this.sort_by = sort_by;
    }

    @Override
    public int compare(AppInfo o1, AppInfo o2) {

        switch (sort_by) {
            case Constants.SORT_BY_NAME:
                return o1.getAppName().compareToIgnoreCase(o2.getAppName());

            case Constants.SORT_BY_SIZE:
                return Long.compare(o1.getSize(), o2.getSize());

            case Constants.SORT_BY_LAST_UPDATED:
                return Long.compare(o1.getAppLastUpdateTime(), o2.getAppLastUpdateTime());

            case Constants.SORT_BY_LAST_USED:
                return Long.compare(o1.getLastTimeUsed(), o2.getLastTimeUsed());

            case Constants.SORT_BY_MOST_USED:
                return Long.compare(o2.getTotalForegroundTime(), o1.getTotalForegroundTime());

        }

        return 0;
    }

}
