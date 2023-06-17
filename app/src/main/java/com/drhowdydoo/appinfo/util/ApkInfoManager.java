package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.model.ApkInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ApkInfoManager {

    private Context context;

    private PackageManager packageManager;

    public ApkInfoManager() {

    }

    public ApkInfoManager(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
    }

    private List<File> findAPKFiles(File directory) {
        File[] files = directory.listFiles();
        List<File> apkFiles = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
                    apkFiles.add(file);
                }
            }
        }

        return apkFiles;
    }


    @SuppressLint("CheckResult")
    public void getAllApks(File directory, ApkFragment apkFragment) {

        Observable<File> directoryObservable = Observable.fromArray(directory.listFiles())
                .flatMap(file -> file.isDirectory() ? Observable.just(file) : Observable.empty());

        Observable<List<File>> apkFilesObservable = directoryObservable
                .map(this::findAPKFiles);

        apkFilesObservable
                .flatMap(Observable::fromIterable)
                .subscribeOn(Schedulers.io())
                .map(this::getApkInfo)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apkInfoList -> {
                    System.out.println("Apk List : " + apkInfoList.size());
                    apkFragment.setData(apkInfoList, true);
                }, throwable -> {
                    throwable.printStackTrace();
                });

    }

    private ApkInfo getApkInfo(File apkFile) {

        System.out.println("Apk : " + apkFile.getAbsolutePath());
        PackageInfo apkInfo = packageManager.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_PERMISSIONS | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_META_DATA | PackageManager.GET_SIGNATURES);
        long apkSize = apkFile.length();
        String apkName = apkFile.getName();
        String apkPath = apkFile.getParent();
        String apkAbsolutePath = apkFile.getAbsolutePath();
        String apkVersion = apkInfo.versionName;
        Drawable apkIcon;
        boolean isInstalled = true;
        try {
            apkIcon = packageManager.getApplicationIcon(apkInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            isInstalled = false;
            apkInfo.applicationInfo.sourceDir = apkFile.getAbsolutePath();
            apkInfo.applicationInfo.publicSourceDir = apkFile.getAbsolutePath();
            apkIcon = apkInfo.applicationInfo.loadIcon(packageManager);
        }

        return new ApkInfo(apkName, apkIcon, apkSize, apkPath, apkAbsolutePath, apkVersion, apkInfo, isInstalled);

    }


}
