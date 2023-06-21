package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.model.ApkInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings({"FieldCanBeLocal"})
public class ApkInfoManager {

    private Context context;

    private PackageManager packageManager;

    public ApkInfoManager() {

    }

    public ApkInfoManager(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
    }

    @SuppressLint("CheckResult")
    public void getAllApks(File directory, ApkFragment apkFragment) {

        File[] files = directory.listFiles();
        if (files == null) return;
        Observable<File> directoryObservable = Observable.fromArray(files)
                .flatMap(file -> file.isDirectory() ? Observable.just(file) : Observable.empty())
                .flatMap(file -> Utilities.skipDirectoriesSet.contains(file.getName()) ? Observable.empty() : Observable.just(file));

        Observable<List<File>> apkFilesObservable = directoryObservable
                .map(this::findAPKFiles);

        apkFilesObservable
                .flatMap(Observable::fromIterable)
                .subscribeOn(Schedulers.io())
                .map(this::getApkInfo)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(apkFragment::hideProgressBar)
                .subscribe(apkInfoList -> {
                    System.out.println("Apk List : " + apkInfoList.size());
                    apkFragment.setData(apkInfoList, true);
                }, Throwable::printStackTrace);

    }

    private List<File> findAPKFiles(File directory) throws IOException {
        Path path = directory.toPath();
        List<File> apkFiles = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().toString().toLowerCase().contains("wallpaper"))
                    return FileVisitResult.SKIP_SUBTREE;
                else return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".apk") && !file.getFileName().toString().startsWith("split_config")) {
                    apkFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

        });
        return apkFiles;
    }


    private ApkInfo getApkInfo(File apkFile) {

        long apkSize = apkFile.length();
        String apkName = apkFile.getName();
        String apkPath = apkFile.getParent();
        String apkAbsolutePath = apkFile.getAbsolutePath();
        Drawable apkIcon = AppCompatResources.getDrawable(context, R.drawable.round_android_24);
        String apkVersion = "";
        boolean isInstalled = true;
        PackageInfo apkInfo = null;
        try {
            apkInfo = packageManager.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_META_DATA);
            if (apkInfo != null) {
                apkVersion = apkInfo.versionName;
                apkIcon = packageManager.getApplicationIcon(apkInfo.packageName);
            }
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
