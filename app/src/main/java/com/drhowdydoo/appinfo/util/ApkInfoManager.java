package com.drhowdydoo.appinfo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.ViewModelProvider;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.model.BundleInfo;
import com.drhowdydoo.appinfo.model.LRUCache;
import com.drhowdydoo.appinfo.viewmodel.ApkListViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    private final String apkFileExtension = ".apk";

    private LRUCache lruCache;


    public ApkInfoManager() {

    }

    public ApkInfoManager(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        preferences = context.getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);
        lruCache = LRUCache.getInstance();
    }

    @SuppressLint("CheckResult")
    public void getAllApks(File directory, ApkFragment apkFragment) {

        scanHiddenFolders = preferences.getBoolean("com.drhowdydoo.appinfo.scan-hidden-folders", true);
        showSplitApks = preferences.getBoolean("com.drhowdydoo.appinfo.show-split-apks", true);
        int scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode", 2);
        ApkListViewModel apkListViewModel = new ViewModelProvider(apkFragment).get(ApkListViewModel.class);

        File[] files = directory.listFiles();

        if (files == null) return;
        Observable<File> directoryObservable = Observable.fromArray(files)
                .flatMap(file -> {
                    if (scanMode == 2) {
                        return Utilities.skipDirectoriesSet.contains(file.getName()) ? Observable.empty() : Observable.just(file);
                    } else return Observable.just(file);
                });

        Observable<List<File>> apkFilesObservable = directoryObservable
                .map(this::findAPKFiles);

        apkFilesObservable
                .flatMap(Observable::fromIterable)
                .subscribeOn(Schedulers.io())
                .map(this::getApkInfo)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> apkListViewModel.setBackgroundTaskState(false))
                .subscribe(apkInfoList -> {
                    //apkFragment.setData(apkInfoList, true);
                    apkListViewModel.setFetchedApkInfoList(apkInfoList);
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
                if (Utilities.isSplitApk(file.getFileName().toString().toLowerCase()) && !showSplitApks)
                    return FileVisitResult.SKIP_SUBTREE;
                if (isApk(file.getFileName().toString())) {
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

    private boolean isApk(String fileName) {
        String extension = Utilities.getExtension(fileName);
        if (extension.isEmpty()) return false;
        return Constants.apkBundleExtensions.contains(extension) || apkFileExtension.equalsIgnoreCase(extension);
    }


    private ApkInfo getApkInfo(File apkFile) {

        long apkSize = apkFile.length();
        String apkName = apkFile.getName();
        String apkPath = apkFile.getParent();
        String apkAbsolutePath = apkFile.getAbsolutePath();
        Drawable apkIcon = AppCompatResources.getDrawable(context, R.drawable.empty_icon_placeholder);
        if (Utilities.isSplitApk(apkName.toLowerCase())) apkIcon = AppCompatResources.getDrawable(context, R.drawable.ic_split_config_apk);
        String apkVersion = "";
        boolean isInstalled = false;
        PackageInfo apkInfo = null;
        try {
            if (Constants.apkBundleExtensions.contains(Utilities.getExtension(apkName))) {
                return getBundleApkInfo(apkName, apkSize, apkFile);
            } else {
                apkInfo = packageManager.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_META_DATA);
            }
            if (apkInfo != null) {
                apkVersion = apkInfo.versionName;
                apkIcon = packageManager.getApplicationIcon(apkInfo.packageName);
                isInstalled = Utilities.isInstalled(apkInfo.packageName, packageManager);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            apkInfo.applicationInfo.sourceDir = apkFile.getAbsolutePath();
            apkInfo.applicationInfo.publicSourceDir = apkFile.getAbsolutePath();
            apkIcon = apkInfo.applicationInfo.loadIcon(packageManager);
        }

        return new ApkInfo(apkName, apkIcon, apkSize, apkPath, apkAbsolutePath, apkVersion, apkInfo, isInstalled, Utilities.isSplitApk(apkName.toLowerCase()));

    }

    public ApkInfo getBundleApkInfo(String apkName, long apkSize, File apksFile) {
        String apkFilePath = apksFile.getAbsolutePath();
        String apkVersion = "";
        boolean isInstalled = false;
        PackageInfo info = null;
        Drawable apkIcon = null;
        File tempApk = null;
        int counter = 0;
        int flags = Constants.packageManagerFlags;
        try (ZipFile zipFile = new ZipFile(apkFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Look for base.apk or other APK files
                if (!Utilities.isSplitApk(entry.getName()) && entry.getName().endsWith(".apk")) {
                    // Extract to temporary file and analyze
                    tempApk = Utilities.extractToTempFile(zipFile, entry, context);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        flags |= PackageManager.GET_SIGNING_CERTIFICATES;
                    info = packageManager.getPackageArchiveInfo(
                            tempApk.getAbsolutePath(),
                            flags
                    );
                    if (info != null) {
                        apkVersion = info.versionName;
                        isInstalled = Utilities.isInstalled(info.packageName, packageManager);
                    }
                    counter++;
                }

                if (entry.getName().equalsIgnoreCase("icon.png")) {
                    File iconPngFile = Utilities.extractToTempFile(zipFile, entry, context);
                    apkIcon = Drawable.createFromPath(iconPngFile.getAbsolutePath());
                    boolean iconDeleted = iconPngFile.delete();
                    if (!iconDeleted) iconPngFile.deleteOnExit();
                    counter++;
                }

                if (counter == 2) break; // Exit loop if both APK and icon are found
            }

            if (apkIcon == null) {
                if (info != null) {
                    try {
                        apkIcon = packageManager.getApplicationIcon(info.packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (tempApk != null) {
                            info.applicationInfo.sourceDir = tempApk.getAbsolutePath();
                            info.applicationInfo.publicSourceDir = tempApk.getAbsolutePath();
                            apkIcon = info.applicationInfo.loadIcon(packageManager);
                        }
                    }
                } else {
                    apkIcon = AppCompatResources.getDrawable(context, R.drawable.empty_icon_placeholder);
                }
            }


            lruCache.put(apkFilePath, new BundleInfo(info, Utilities.getBitmapFromDrawable(apkIcon)));

            // Clean up temporary file
            if (tempApk != null) {
                boolean deleteSuccess = tempApk.delete();
                if (!deleteSuccess) tempApk.deleteOnExit();
            }

            return new ApkInfo(apkName, apkIcon, apkSize, apksFile.getParent(), apksFile.getAbsolutePath(), apkVersion, info, isInstalled, false);

        } catch (IOException e) {
            Log.e("ApksAnalyzer", "Error analyzing APKS file", e);
        }

        return null;
    }

}
