package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.model.AppInfo;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldMayBeFinal")
public class AppInfoManager {

    private Context context;
    private UsageStatsManager usageStatsManager;
    private StorageStatsManager storageStatsManager;
    private PackageManager packageManager;

    public AppInfoManager(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = context.getPackageManager();
        storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
    }


    @SuppressLint("CheckResult")
    public void getAllApps(AppFragment appFragment) {

        Observable.fromCallable(() -> {
                    PackageManager packageManager = context.getPackageManager();
                    return packageManager.getInstalledApplications(0);
                })
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .flatMap(appInfo -> Observable.fromCallable(() -> getAppInfo(appInfo)).subscribeOn(Schedulers.io()))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(appFragment::hideProgressIndicators)
                .subscribe(appInfoList -> {
                    if (appFragment.sortedState == Constants.SORT_BY_LAST_USED) {
                        addLastUsedTimeToAppInfo(appInfoList, appFragment);
                    } else if (appFragment.sortedState == Constants.SORT_BY_MOST_USED) {
                        addForegroundTimeToAppInfo(appInfoList, appFragment);
                    } else {
                        appFragment.setData(appInfoList, true);
                    }
                }, Throwable::printStackTrace);

    }

    private AppInfo getAppInfo(ApplicationInfo applicationInfo) {

        long lastUpdateTime = Constants.LAST_UPDATED_TIME_NOT_AVAILABLE;
        boolean isSplit;
        long installTmStamp = Constants.INSTALL_TIMESTAMP_NOT_AVAILABLE;
        String appVersion = "";
        String installSource = getInstallSource(applicationInfo.packageName);

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_META_DATA);
            lastUpdateTime = now() - packageInfo.lastUpdateTime;
            installTmStamp = packageInfo.firstInstallTime;
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException notFoundException) {
            notFoundException.printStackTrace();
        }

        String appName = packageManager.getApplicationLabel(applicationInfo).toString();
        Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
        boolean isSystemApp = ((applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        String[] splitSourceDirs = applicationInfo.splitSourceDirs;
        isSplit = (splitSourceDirs != null && splitSourceDirs.length > 0);

        AppInfo appInfo = new AppInfo(appName,
                appIcon,
                lastUpdateTime,
                applicationInfo,
                isSplit,
                appVersion,
                isSystemApp,
                applicationInfo.packageName,
                installTmStamp,
                installSource);

        if (PermissionManager.hasUsageStatsPermission(context)) {
            long totalAppSize = 0;
            try {
                StorageStats storageStats = storageStatsManager.queryStatsForUid(applicationInfo.storageUuid, applicationInfo.uid);
                totalAppSize = storageStats.getAppBytes() + storageStats.getDataBytes();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            appInfo.setSize(totalAppSize);

        } else {
            long appSize = 0;
            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                for (String splitSourceDir : splitSourceDirs) {
                    File splitFile = new File(splitSourceDir);
                    appSize += splitFile.length();
                }
            }
            String appSourceDir = applicationInfo.sourceDir;
            File appFile = new File(appSourceDir);
            appSize += appFile.length();

            appInfo.setSize(appSize);
        }

        return appInfo;
    }

    @SuppressLint("CheckResult")
    public void addLastUsedTimeToAppInfo(List<AppInfo> appInfoList, AppFragment appFragment) {

        Observable.fromCallable(() -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.YEAR, -1);
                    long start = calendar.getTimeInMillis();
                    long end = System.currentTimeMillis();
                    return usageStatsManager.queryAndAggregateUsageStats(start, end);
                })
                .flatMap(usageStatsMap -> Observable.fromIterable(appInfoList)
                        .flatMap(appInfo -> Observable.fromCallable(() -> {
                            appInfo.setLastTimeUsed(getLastUsedTime(appInfo.getAppInfo(), usageStatsMap));
                            return appInfo;
                        }).subscribeOn(Schedulers.computation()))
                )
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(processedList -> {
                    appFragment.setData(processedList, true);
                }, Throwable::printStackTrace);

    }

    @SuppressLint("CheckResult")
    public void addForegroundTimeToAppInfo(List<AppInfo> appInfoList, AppFragment appFragment) {

        Observable.fromCallable(() -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_WEEK, -7);
                    long start = calendar.getTimeInMillis();
                    long end = System.currentTimeMillis();
                    return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end);
                })
                .flatMap(usageStatsList -> Observable.fromIterable(appInfoList)
                        .flatMap(appInfo -> Observable.fromCallable(() -> {
                            HashMap<String, UsageStats> usageStatsMap = new HashMap<>();
                            for (UsageStats usageStats : usageStatsList) {
                                String packageName = usageStats.getPackageName();
                                usageStatsMap.put(packageName, usageStats);
                            }
                            appInfo.setTotalForegroundTime(getForegroundTime(appInfo.getAppInfo(), usageStatsMap));
                            return appInfo;
                        }).subscribeOn(Schedulers.computation()))
                )
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(processedList -> {
                    appFragment.setData(processedList, true);
                }, Throwable::printStackTrace);

    }

    private long getLastUsedTime(ApplicationInfo applicationInfo, Map<String, UsageStats> usageStatsMap) {
        try {
            UsageStats usageStats = usageStatsMap.get(applicationInfo.packageName);
            return usageStats != null ? (now() - usageStats.getLastTimeUsed()) : Constants.LAST_USED_TIME_NOT_AVAILABLE;
        } catch (Exception e) {
            return Constants.LAST_USED_TIME_NOT_AVAILABLE;
        }

    }

    private long getForegroundTime(ApplicationInfo applicationInfo, Map<String, UsageStats> usageStatsMap) {
        try {
            UsageStats usageStats = usageStatsMap.get(applicationInfo.packageName);
            return usageStats != null ? usageStats.getTotalTimeInForeground() : Constants.FOREGROUND_TIME_NOT_AVAILABLE;
        } catch (Exception e) {
            return Constants.FOREGROUND_TIME_NOT_AVAILABLE;
        }

    }

    public String getInstallSource(String packageName) {
        String installSource = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                installSource = context.getPackageManager().getInstallSourceInfo(packageName).getInstallingPackageName();
            } else {
                installSource = context.getPackageManager().getInstallerPackageName(packageName);
            }
            return installSource != null ? installSource : "Unknown";
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException exception) {
            installSource = "Unknown";
        }

        return installSource;
    }

    private long now() {
        return new Date().getTime();
    }

}
