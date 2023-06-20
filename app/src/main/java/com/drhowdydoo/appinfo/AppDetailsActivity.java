package com.drhowdydoo.appinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.adapter.AppDetailsListAdapter;
import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.model.AppMetadata;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Utilities;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class AppDetailsActivity extends AppCompatActivity {

    private static int packageManagerFlags = PackageManager.GET_PERMISSIONS |
            PackageManager.GET_RECEIVERS |
            PackageManager.GET_PROVIDERS |
            PackageManager.GET_ACTIVITIES |
            PackageManager.GET_SERVICES |
            PackageManager.GET_META_DATA |
            PackageManager.GET_SIGNATURES;
    private ActivityAppDetailsBinding binding;
    private AppDetailsManager appDetailsManager;
    private boolean isApk = false;
    private String apkAbsolutePath = "";
    private List<AppDetailItem> appDetailItems = new ArrayList<>();
    private AppDetailsListAdapter adapter;

    @SuppressLint({"CheckResult", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate : " + System.currentTimeMillis());
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        binding.materialToolBar.setTitle(intent.getStringExtra("appName"));
        binding.tvVersion.setText("v" + intent.getStringExtra("appVersion"));
        isApk = intent.getBooleanExtra("isApk", false);
        apkAbsolutePath = intent.getStringExtra("apkAbsolutePath");
        boolean isInstalled = intent.getBooleanExtra("isInstalled", true);
        String packageName = intent.getStringExtra("packageName");
        String identifier = isApk ? apkAbsolutePath : packageName;
        System.out.println("onObservable : " + System.currentTimeMillis());
        Observable.just(getPackageInfo(identifier, isApk))
                .subscribeOn(Schedulers.io())
                .subscribe(packageInfo -> {
                    packageInfo.ifPresent(value -> appDetailsManager = new AppDetailsManager(this, value));
                    packageInfo.ifPresent(this::init);
                    runOnUiThread(() -> packageInfo.ifPresent(this::setUpClickListeners));
                });

        System.out.println("afterObservable : " + System.currentTimeMillis());
        //Initial conditional setups
        handleToolbarContentAlignment();
        if (!isInstalled) {
            binding.btnInfo.setEnabled(false);
        }

        adapter = new AppDetailsListAdapter(appDetailItems);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setAdapter(adapter);

        System.out.println("onCreateCompletes : " + System.currentTimeMillis());
    }


    private Optional<PackageInfo> getPackageInfo(String identifier, boolean isApk) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                packageManagerFlags |= PackageManager.GET_SIGNING_CERTIFICATES;
            if (isApk)
                return Optional.ofNullable(getPackageManager().getPackageArchiveInfo(identifier, packageManagerFlags));
            return Optional.ofNullable(getPackageManager().getPackageInfo(identifier, packageManagerFlags));
        } catch (PackageManager.NameNotFoundException e) {
            return Optional.empty();
        }
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "NotifyDataSetChanged"})
    @SuppressWarnings("CheckResult")
    private void init(PackageInfo packageInfo) {

        System.out.println("inti : " + System.currentTimeMillis());

        Disposable iconDisposable = Observable.just(appDetailsManager.getIcon(isApk, apkAbsolutePath))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> binding.imgIcon.setImageDrawable(icon));

        Observable.just(appDetailsManager.getColors())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(color -> {
                    String primaryColor = "#" + Integer.toHexString(color.getColorPrimary());
                    String secondaryColor = "#" + Integer.toHexString(color.getColorSecondary());
                    if (primaryColor.equalsIgnoreCase("#ffffffff") && secondaryColor.equalsIgnoreCase("#ffffffff")) {
                        binding.tvColorValue.setText("NOT FOUND 😅");
                    } else {
                        binding.tvColorValue.setText(primaryColor + "\n" + secondaryColor);
                    }
                });

        Observable.just(appDetailsManager.getTheme())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(theme -> binding.tvThemeValue.setText(theme));


        System.out.println("zip grid : " + System.currentTimeMillis());
        Observable.zip(
                        Observable.just(appDetailsManager.getCategory()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getMinSdk()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getTargetSdk()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getInstallSource()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getInstalledDate()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getUpdatedDate()).subscribeOn(Schedulers.io()),
                        Observable.just(packageInfo.packageName).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getMainClass()).subscribeOn(Schedulers.io()),
                        (category, minSdk, targetSdk, installSource,
                         installDt, updatedDt, packageName, mainClass) -> new AppMetadata(category, minSdk, targetSdk, installDt, updatedDt,
                                installSource, packageName, mainClass)
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appMetadata -> {
                    binding.tvCategoryValue.setText(appMetadata.getCategory());
                    binding.tvMinSdkValue.setText(appMetadata.getMinSdk());
                    binding.tvTargetSdkValue.setText(appMetadata.getTargetSdk());
                    binding.tvSourceValue.setText(appMetadata.getSource());
                    binding.tvInstalledDtValue.setText(appMetadata.getInstallDt());
                    binding.tvUpdatedDtValue.setText(appMetadata.getUpdatedDt());
                    binding.tvPackageNameValue.setText(appMetadata.getPackageName());
                    binding.tvMainClassValue.setText(appMetadata.getMainClass());
                });

        System.out.println("zip list : " + System.currentTimeMillis());
        Observable.zip(Observable.just(appDetailsManager.getPermissions()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getActivities()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getBroadcastReceivers()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getServices()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getProviders()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getFeatures()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getSignatures()).subscribeOn(Schedulers.io()),
                        Observable.just(AppDetailsManager.findFontFiles(packageInfo.applicationInfo.publicSourceDir)).subscribeOn(Schedulers.newThread()),
                        (permissions, activities, receivers, services, providers, features, signatureMap, fonts) -> {
                            List<AppDetailItem> appDetailItems = new ArrayList<>();
                            String backupAgent = packageInfo.applicationInfo.backupAgentName;
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_shield_24, "Permissions", permissions));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_touch_app_24, "Activities", activities));
                            appDetailItems.add(new AppDetailItem(R.drawable.round_fonts_24, "Fonts", fonts));
                            appDetailItems.add(new AppDetailItem(R.drawable.round_cell_tower_24, "Broadcast receivers", receivers));
                            appDetailItems.add(new AppDetailItem(R.drawable.round__services_24, "Services", services));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_extension_24, "Providers", providers));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_stars_24, "Features", features));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_backup_24, "Backup agent name", backupAgent != null ? backupAgent : "NOT SPECIFIED"));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_folder_24, "Data dir path", appDetailsManager.getDataDirPath()));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_source_24, "Source dir path", appDetailsManager.getSourceDirPath()));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_folder_shared_24, "Native library path", appDetailsManager.getNativeLibraryPath()));

                            signatureMap.ifPresent(signatures -> {
                                appDetailItems.add(new AppDetailItem(R.drawable.outline_vpn_key_24, "Signature", signatures.get("certificates")));
                                appDetailItems.add(new AppDetailItem(R.drawable.round_fingerprint_24, "Certificate fingerprints", signatures.get("signing_keys")));
                            });
                            return appDetailItems;
                        })
                .subscribeOn(Schedulers.io())
                .subscribe(appDetailItemList -> {
                    appDetailItems.clear();                 // Still In Background thread use runOnUiThread method for UI stuffs.
                    appDetailItems.addAll(appDetailItemList);
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });

        System.out.println("init end : " + System.currentTimeMillis());

    }

    private void setUpClickListeners(PackageInfo packageInfo) {
        binding.btnInfo.setOnClickListener(v -> openSystemInfo(packageInfo.packageName));
        binding.btnPlayStore.setOnClickListener(v -> openInPlayStore(packageInfo.packageName));
        binding.btnExtractApk.setOnClickListener(v -> {
            boolean haveStorageAccess = checkStoragePermission();
            if (!haveStorageAccess) return;
            if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "Storage not accessible", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.btnExtractApk.setEnabled(false);
            Utilities.shouldSearchApks = true;
            Observable.fromAction(() -> {
                        runOnUiThread(() -> binding.progressBar.setVisibility(View.VISIBLE));
                        extractApk(packageInfo);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> {
                        binding.btnExtractApk.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "APK files extracted to AppInfo folder in the root directory", Toast.LENGTH_SHORT).show();
                    })
                    .subscribe();
        });
    }

    private void extractApk(PackageInfo packageInfo) {

        try {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            String sourceDir = appInfo.publicSourceDir;
            String apkName = (String) this.getPackageManager().getApplicationLabel(appInfo);
            File destinationRootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AppInfo");
            File destinationAppFolder = new File(destinationRootFolder, apkName);
            boolean created = destinationAppFolder.mkdirs();

            // Check if the app has split APKs
            String[] splitSourceDirs = appInfo.splitPublicSourceDirs;
            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                for (String splitSourceDir : splitSourceDirs) {
                    extractApkFileAtPath(splitSourceDir, destinationAppFolder);
                }
            }

            extractApkFileAtPath(sourceDir, destinationAppFolder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractApkFileAtPath(String apkFilePath, File destinationAppFolder) {
        try {
            File sourceFile = new File(apkFilePath);
            String appName = sourceFile.getName().toLowerCase().startsWith("base") ? destinationAppFolder.getName() + ".apk" : sourceFile.getName();
            File destinationFile = new File(destinationAppFolder, appName);
            Utilities.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                || Environment.isExternalStorageManager()) return true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return false;
        }

        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.baseline_gpp_maybe_24)
                .setTitle("Permission required   😅")
                .setMessage("Manage external storage permission required to perform this action ^_^")
                .setPositiveButton("Allow", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
                    startActivity(intent);
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
        return false;
    }

    private void handleToolbarContentAlignment() {
        binding.tvVersion.post(() -> {
            if (binding.tvVersion.getLineCount() > 1) {
                System.out.println("handleToolbarContentAlignment()");
                binding.collapsingToolBar.setExpandedTitleMarginBottom(Utilities.dpToPx(this, 58));
                CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) binding.tvVersion.getLayoutParams();
                layoutParams.bottomMargin = Utilities.dpToPx(this, 10);
                binding.tvVersion.setLayoutParams(layoutParams);
            }
        });

    }

    private void openSystemInfo(String packageName) {
        Intent systemInfo = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        systemInfo.setData(Uri.parse("package:" + packageName));
        startActivity(systemInfo);
    }

    private void openInPlayStore(String packageName) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
    }


}