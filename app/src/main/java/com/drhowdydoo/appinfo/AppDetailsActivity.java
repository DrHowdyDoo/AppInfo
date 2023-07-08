package com.drhowdydoo.appinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.adapter.AppDetailsListAdapter;
import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.model.AppMetadata;
import com.drhowdydoo.appinfo.model.StringCount;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Utilities;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class AppDetailsActivity extends AppCompatActivity {

    private static String TAG = "AppDetailsActivity";
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
    private List<Object> appDetails = new ArrayList<>();
    private AppDetailsListAdapter adapter;
    private boolean isSplitApp;
    private String appName;
    private boolean isInstalled;
    private boolean permissionAskedForFirstTime = true;

    @SuppressLint({"CheckResult", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        appName = intent.getStringExtra("appName");
        binding.materialToolBar.setTitle(appName);
        String appVersion = intent.getStringExtra("appVersion");
        if (appVersion != null && !appVersion.isBlank())
            binding.tvVersion.setText("v" + appVersion);
        isSplitApp = intent.getBooleanExtra("isSplitApp", false);
        binding.imgSplitIndicator.setVisibility(isSplitApp ? View.VISIBLE : View.GONE);
        isApk = intent.getBooleanExtra("isApk", false);
        apkAbsolutePath = intent.getStringExtra("apkAbsolutePath");
        isInstalled = intent.getBooleanExtra("isInstalled", true);
        String packageName = intent.getStringExtra("packageName");
        String identifier = (isApk && !isInstalled) ? apkAbsolutePath : packageName;

        //Initial conditional setups
        handleToolbarContentAlignment();

        adapter = new AppDetailsListAdapter(appDetails, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);
        System.out.println("Before Observable : " + (System.currentTimeMillis() - startTime) + "ms");
        Observable.just(getPackageInfo(identifier))
                .subscribeOn(Schedulers.io())
                .subscribe(packageInfo -> {
                    packageInfo.ifPresent(value -> {
                        appDetailsManager = new AppDetailsManager(this, value);
                        adapter.addApkDetails(appName, value.applicationInfo.publicSourceDir, isInstalled, isApk, isSplitApp);
                        adapter.setPackageInfo(value);
                        init(value);
                    });
                    if (!packageInfo.isPresent()) {
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvPackageNotFound.setVisibility(View.VISIBLE);
                        binding.appBar.setVisibility(View.GONE);
                        if (appName.toLowerCase().startsWith("split")) {
                            binding.tvPackageNotFound.setText("Split Config APK\nContains device-specific resources/configs\nNot a complete APK");
                            binding.tvPackageNotFound.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_split_config_apk, 0, 0);
                            binding.tvPackageNotFound.setCompoundDrawableTintList(null);
                        }
                    }

                });
        System.out.println("Activity startup : " + (System.currentTimeMillis() - startTime) + "ms");

    }


    private Optional<PackageInfo> getPackageInfo(String identifier) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                packageManagerFlags |= PackageManager.GET_SIGNING_CERTIFICATES;
            if (isApk && !isInstalled)
                return Optional.ofNullable(getPackageManager().getPackageArchiveInfo(identifier, packageManagerFlags));
            return Optional.ofNullable(getPackageManager().getPackageInfo(identifier, packageManagerFlags));
        } catch (PackageManager.NameNotFoundException e) {
            return Optional.empty();
        }
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "NotifyDataSetChanged"})
    @SuppressWarnings("CheckResult")
    private void init(PackageInfo packageInfo) {


        Disposable iconDisposable = Observable.just(appDetailsManager.getIcon(isApk, apkAbsolutePath))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> binding.imgIcon.setImageDrawable(icon));

        appDetails.add(new AppMetadata());

        Observable.zip(Observable.just(appDetailsManager.getCategory()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getMinSdk()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getTargetSdk()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getInstallSource()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getInstalledDate()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getUpdatedDate()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getMainClass()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getTheme()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getColors()).subscribeOn(Schedulers.io()),
                        (category, minSdk, targetSdk, installSource,
                         installDt, updatedDt, mainClass, theme, color) -> {

                            String primaryColor = "#" + Integer.toHexString(color.getColorPrimary());
                            String secondaryColor = "#" + Integer.toHexString(color.getColorSecondary());
                            String colors = "NOT FOUND 😅";
                            if (primaryColor.equalsIgnoreCase("#ffffffff") && secondaryColor.equalsIgnoreCase("#ffffffff")) {
                                colors = "NOT FOUND 😅";
                            } else {
                                colors = primaryColor + "\n" + secondaryColor;
                            }
                            return new AppMetadata(category, minSdk, targetSdk, installDt, updatedDt,
                                    installSource, packageInfo.packageName, mainClass, theme, colors);
                        }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appMetadata -> {
                    //Grid Data Set
                    appDetails.set(0, appMetadata);
                    adapter.notifyItemChanged(0);
                });

        String backupAgent = packageInfo.applicationInfo.backupAgentName;
        appDetails.add(new AppDetailItem(R.drawable.outline_shield_24, "Permissions", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.outline_touch_app_24, "Activities", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.round_fonts_24, "Fonts", ""));
        appDetails.add(new AppDetailItem(R.drawable.round_cell_tower_24, "Broadcast receivers", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.round__services_24, "Services", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.outline_extension_24, "Providers", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.outline_stars_24, "Features", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.outline_backup_24, "Backup agent name", backupAgent != null ? backupAgent : "NOT SPECIFIED"));
        appDetails.add(new AppDetailItem(R.drawable.outline_folder_24, "Data dir path", appDetailsManager.getDataDirPath()));
        appDetails.add(new AppDetailItem(R.drawable.outline_source_24, "Source dir path", appDetailsManager.getSourceDirPath()));
        appDetails.add(new AppDetailItem(R.drawable.outline_folder_shared_24, "Native library path", appDetailsManager.getNativeLibraryPath()));
        appDetails.add(new AppDetailItem(R.drawable.outline_vpn_key_24, "Signature", "loading • • • • •"));
        appDetails.add(new AppDetailItem(R.drawable.round_fingerprint_24, "Certificate fingerprints", "loading • • • • •"));
        runOnUiThread(() -> adapter.notifyDataSetChanged());


        Observable.just(appDetailsManager.getPermissions())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(permissions -> {
                    getAppDetail(appDetails.get(1)).setValue(permissions);
                    adapter.notifyItemChanged(1);
                });

        Observable.just(appDetailsManager.getActivities())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activities -> {
                    getAppDetail(appDetails.get(2)).setValue(activities);
                    adapter.notifyItemChanged(2);
                });

        Observable.just(appDetailsManager.findFontFiles(packageInfo.applicationInfo.publicSourceDir))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(font -> {
                    getAppDetail(appDetails.get(3)).setValue(font);
                    adapter.notifyItemChanged(3);
                });

        Observable.just(appDetailsManager.getBroadcastReceivers())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(receivers -> {
                    getAppDetail(appDetails.get(4)).setValue(receivers);
                    adapter.notifyItemChanged(4);
                });

        Observable.just(appDetailsManager.getServices())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(services -> {
                    getAppDetail(appDetails.get(5)).setValue(services);
                    adapter.notifyItemChanged(5);
                });

        Observable.just(appDetailsManager.getProviders())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(providers -> {
                    getAppDetail(appDetails.get(6)).setValue(providers);
                    adapter.notifyItemChanged(6);
                });


        Observable.zip(Observable.just(appDetailsManager.getFeatures()).subscribeOn(Schedulers.io()),
                        Observable.just(appDetailsManager.getSignatures()).subscribeOn(Schedulers.io()),
                        (features, signatureMap) -> {
                            getAppDetail(appDetails.get(7)).setValue(features);
                            signatureMap.ifPresent(signatures -> {
                                getAppDetail(appDetails.get(12)).setValue(new StringCount(signatures.get("certificates")));
                                getAppDetail(appDetails.get(13)).setValue(new StringCount(signatures.get("signing_keys")));
                            });
                            return appDetails;
                        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appDetailItemList -> {
                    adapter.notifyItemChanged(7);
                    adapter.notifyItemRangeChanged(12, 2);
                });

    }


    public boolean checkStoragePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
            return true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !permissionAskedForFirstTime) {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.baseline_gpp_maybe_24)
                        .setTitle("Permission required   😅")
                        .setMessage("Storage access required to perform this action\nTo grant storage permission, please follow these steps:\n" +
                                "1. Press 'Allow' to navigate to the app's settings page\n" +
                                "2. In the settings, navigate to the 'Permissions tab.\n" +
                                "3. Look for the 'Storage' permission and ensure it is enabled.\n")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
                            startActivity(intent);
                        })
                        .setNegativeButton("Deny", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                permissionAskedForFirstTime = false;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            return false;
        }

        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.baseline_gpp_maybe_24)
                .setTitle("Permission required   😅")
                .setMessage("Manage external storage permission required to perform this action\n" +
                        "To grant storage permission, please follow these steps:\n" +
                        "Press 'Allow' to navigate to the ALL FILES ACCESS page for this app\n" +
                        "Then look for the 'Allow access to manage all files' permission and ensure it is enabled.\n")
                .setPositiveButton("Allow", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
                    startActivity(intent);
                })
                .setNegativeButton("Deny", (dialog, which) -> dialog.dismiss())
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

    public void openSystemInfo(String packageName) {
        Intent systemInfo = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        systemInfo.setData(Uri.parse("package:" + packageName));
        startActivity(systemInfo);
    }

    public void openInPlayStore(String packageName) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
    }

    private AppDetailItem getAppDetail(Object obj) {
        return (AppDetailItem) obj;
    }

    public void hideProgressBar(boolean hide) {
        binding.progressBar.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

}