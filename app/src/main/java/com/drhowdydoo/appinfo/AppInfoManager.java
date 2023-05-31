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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldMayBeFinal")
public class AppInfoManager {

    private Context context;
    private static final long LAST_USED_TIME_NOT_AVAILABLE = 63072000000L;

    public AppInfoManager(Context context) {
        this.context = context;
    }



    @SuppressLint("CheckResult")
    public void getAllApps(Fragment fragment) throws PackageManager.NameNotFoundException, IOException {

        Observable.fromCallable(() -> {
                    PackageManager packageManager = context.getPackageManager();
                    return packageManager.getInstalledApplications(0);
                })
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .flatMap(appInfo -> Observable.fromCallable(() -> getAppInfo(appInfo)).subscribeOn(Schedulers.io()))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfoList -> ((AppFragment) fragment).setData(appInfoList), Throwable::printStackTrace);

    }

    private AppInfo getAppInfo(ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException, IOException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName,PackageManager.GET_META_DATA);
        StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        StorageStats storageStats = storageStatsManager.queryStatsForUid(applicationInfo.storageUuid, applicationInfo.uid);
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(start, end);
        UsageStats usageStats = usageStatsMap.get(applicationInfo.packageName);


        String appName = packageManager.getApplicationLabel(applicationInfo).toString();
        Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
        long lastUpdated = now() - packageManager.getPackageInfo(applicationInfo.packageName,0).lastUpdateTime;
        long appSize = storageStats.getAppBytes() + storageStats.getCacheBytes() + storageStats.getDataBytes();
        long lastUsed = usageStats != null ? (now() - usageStats.getLastTimeUsed()) : LAST_USED_TIME_NOT_AVAILABLE;
        boolean isSplit = packageInfo.applicationInfo.metaData != null && packageInfo.applicationInfo.metaData.getBoolean("com.android.vending.splits.required", false);
        String appVersion = packageInfo.versionName;
        boolean isSystemApp = ((applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);


        return new AppInfo(appName,
                appIcon,
                lastUpdated,
                appSize,
                applicationInfo,
                lastUsed,
                isSplit,
                appVersion,
                isSystemApp);
    }

    private long now(){
        return new Date().getTime();
    }

}
