package com.drhowdydoo.appinfo;

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

import androidx.fragment.app.Fragment;

import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.PermissionManager;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldMayBeFinal")
public class AppInfoManager {

    private Context context;
    private static final long LAST_USED_TIME_NOT_AVAILABLE = 63072000000L;
    private UsageStatsManager usageStatsManager;
    private StorageStatsManager storageStatsManager;
    private long start;
    private PackageManager packageManager;

    public AppInfoManager(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        this.start = calendar.getTimeInMillis();
        packageManager = context.getPackageManager();
        storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
    }



    @SuppressLint("CheckResult")
    public void getAllApps(Fragment fragment) throws PackageManager.NameNotFoundException, IOException {

        long begin = System.currentTimeMillis();
        Observable.fromCallable(() -> {
                    PackageManager packageManager = context.getPackageManager();
                    return packageManager.getInstalledApplications(0);
                })
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .flatMap(appInfo -> Observable.fromCallable(() -> getAppInfo(appInfo)).subscribeOn(Schedulers.io()))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfoList -> {
                    long end = System.currentTimeMillis();
                    System.out.println("Time : " + (end - begin));
                    ((AppFragment) fragment).setData(appInfoList, true);
                }, Throwable::printStackTrace);

    }

    private AppInfo getAppInfo(ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName,PackageManager.GET_META_DATA);

        String appName = packageManager.getApplicationLabel(applicationInfo).toString();
        Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
        long lastUpdated = now() - packageManager.getPackageInfo(applicationInfo.packageName,0).lastUpdateTime;

        boolean isSplit = packageInfo.applicationInfo.metaData != null && packageInfo.applicationInfo.metaData.getBoolean("com.android.vending.splits.required", false);
        String appVersion = packageInfo.versionName;
        boolean isSystemApp = ((applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);

        AppInfo appInfo = new AppInfo(appName,
                appIcon,
                lastUpdated,
                applicationInfo,
                isSplit,
                appVersion,
                isSystemApp);

        if (PermissionManager.hasUsageStatsPermission(context)) {
            long totalAppSize = 0;
            try {
                StorageStats storageStats = storageStatsManager.queryStatsForUid(applicationInfo.storageUuid, applicationInfo.uid);
                totalAppSize = storageStats.getAppBytes() + storageStats.getCacheBytes() + storageStats.getDataBytes();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            appInfo.setSize(totalAppSize);

        } else {
            String[] splitSourceDirs = applicationInfo.splitSourceDirs;
            long appSize = 0;

            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                appInfo.setSplitApp(true);
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
    public void addLastUsedTimeToAppInfo(List<AppInfo> appInfoList, AppFragment appFragment){

        Observable.fromCallable(() -> {
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
                    appFragment.setData(processedList,true);
                }, Throwable::printStackTrace);

    }

    private long getLastUsedTime(ApplicationInfo applicationInfo, Map<String, UsageStats> usageStatsMap){
        try {
            UsageStats usageStats = usageStatsMap.get(applicationInfo.packageName);
            long lastUsed = usageStats != null ? (now() - usageStats.getLastTimeUsed()) : LAST_USED_TIME_NOT_AVAILABLE;
            return lastUsed;
        } catch (Exception e) {
            return LAST_USED_TIME_NOT_AVAILABLE;
        }

    }

    private long now(){
        return new Date().getTime();
    }

}
