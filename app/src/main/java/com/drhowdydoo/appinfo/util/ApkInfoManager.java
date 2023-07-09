package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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

    private SharedPreferences preferences;

    private boolean scanHiddenFolders;
    private boolean showSplitApks;

    public ApkInfoManager() {

    }

    public ApkInfoManager(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        preferences = context.getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);
    }

    @SuppressLint("CheckResult")
    public void getAllApks(File directory, ApkFragment apkFragment) {

        scanHiddenFolders = preferences.getBoolean("com.drhowdydoo.appinfo.scan-hidden-folders", true);
        showSplitApks = preferences.getBoolean("com.drhowdydoo.appinfo.show-split-apks", true);
        int scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode",2);

        File[] files = directory.listFiles();

        if (files == null) return;
        Observable<File> directoryObservable = Observable.fromArray(files)
                .flatMap(file -> {
                    if (scanMode == 2) {
                        return Utilities.skipDirectoriesSet.contains(file.getName()) ? Observable.empty() : Observable.just(file);
                    }else return Observable.just(file);
                });

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
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                boolean isHidden = Files.isHidden(dir);
                if (!scanHiddenFolders && isHidden) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isDirectory()) return FileVisitResult.CONTINUE;
                if (file.getFileName().toString().toLowerCase().startsWith("split") && !showSplitApks)
                    return FileVisitResult.SKIP_SUBTREE;
                if (file.getFileName().toString().endsWith(".apk")) {
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
        Drawable apkIcon = AppCompatResources.getDrawable(context, R.drawable.empty_icon_placeholder);
        if (apkName.toLowerCase().startsWith("split"))
            apkIcon = AppCompatResources.getDrawable(context, R.drawable.ic_split_config_apk);
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
