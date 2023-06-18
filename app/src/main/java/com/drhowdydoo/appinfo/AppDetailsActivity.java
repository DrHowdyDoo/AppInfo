package com.drhowdydoo.appinfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.adapter.AppDetailsListAdapter;
import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.model.AppMetadata;
import com.drhowdydoo.appinfo.model.StringCount;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Utilities;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldCanBeLocal")
public class AppDetailsActivity extends AppCompatActivity {

    private ActivityAppDetailsBinding binding;
    private AppDetailsManager appDetailsManager;
    private boolean isApk = false;
    private boolean isInstalled = true;
    private String apkPath = "";
    private String apkAbsolutePath = "";
    private List<AppDetailItem> appDetailItems = new ArrayList<>();
    private AppDetailsListAdapter adapter;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        isApk = intent.getBooleanExtra("isApk", false);
        if (isApk) {
            PackageInfo apkInfo = intent.getParcelableExtra("apkInfo");
            appDetailsManager = new AppDetailsManager(this, apkInfo);
            apkPath = intent.getStringExtra("apkPath");
            apkAbsolutePath = intent.getStringExtra("apkAbsolutePath");
            isInstalled = intent.getBooleanExtra("isInstalled", true);
            init(apkInfo);
        } else {
            ApplicationInfo appInfo = intent.getParcelableExtra("appInfo");
            Observable.fromCallable(() -> getPackageInfoByAppInfo(appInfo))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(packageInfo -> {
                        packageInfo.ifPresent(value -> appDetailsManager = new AppDetailsManager(this, value));
                        packageInfo.ifPresent(this::init);
                    });
        }

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

    }


    private Optional<PackageInfo> getPackageInfoByAppInfo(ApplicationInfo appInfo) {
        try {
            int flags = PackageManager.GET_PERMISSIONS |
                    PackageManager.GET_RECEIVERS |
                    PackageManager.GET_PROVIDERS |
                    PackageManager.GET_ACTIVITIES |
                    PackageManager.GET_SERVICES |
                    PackageManager.GET_META_DATA |
                    PackageManager.GET_SIGNATURES;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                flags |= PackageManager.GET_SIGNING_CERTIFICATES;
            }
            return Optional.ofNullable(getPackageManager().getPackageInfo(appInfo.packageName, flags));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "NotifyDataSetChanged"})
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void init(PackageInfo packageInfo) {

        setUpClickListeners(packageInfo);
        binding.materialToolBar.setTitle(getIntent().getStringExtra("appName"));
        binding.tvVersion.setText("v" + getIntent().getStringExtra("appVersion"));


        Observable.zip(
                Observable.just(appDetailsManager.getCategory()),
                Observable.just(appDetailsManager.getMinSdk()),
                Observable.just(appDetailsManager.getTargetSdk()),
                Observable.just(appDetailsManager.getInstallSource()),
                Observable.just(appDetailsManager.getInstalledDate()),
                Observable.just(appDetailsManager.getUpdatedDate()),
                Observable.just(packageInfo.packageName),
                Observable.just(appDetailsManager.getMainClass()),
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

        Observable.fromCallable(() -> appDetailsManager.getIcon(isApk, apkAbsolutePath))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> {
                    binding.imgIcon.setImageDrawable(icon);
                });

        Observable.zip(Observable.just(appDetailsManager.getPermissions()),
                        Observable.just(appDetailsManager.getActivities()),
                        Observable.just(appDetailsManager.getBroadcastReceivers()),
                        Observable.just(appDetailsManager.getServices()),
                        Observable.just(appDetailsManager.getProviders()),
                        Observable.just(appDetailsManager.getFeatures()),
                        Observable.just(appDetailsManager.getSignatures()),

                        (permissions, activities,receivers, services, providers, features, signatureMap) -> {
                            List<AppDetailItem> appDetailItems = new ArrayList<>();
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_shield_24, "Permissions", permissions));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_touch_app_24, "Activities", activities));
                            appDetailItems.add(new AppDetailItem(R.drawable.round_cell_tower_24, "Broadcast receivers", receivers));
                            appDetailItems.add(new AppDetailItem(R.drawable.round__services_24, "Services", services));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_extension_24, "Providers", providers));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_stars_24, "Features", features));
                            appDetailItems.add(new AppDetailItem(R.drawable.outline_backup_24, "Backup agent name", packageInfo.applicationInfo.backupAgentName));
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appDetailItemList -> {
                    appDetailItems.clear();
                    appDetailItems.addAll(appDetailItemList);
                    adapter.notifyDataSetChanged();
                    System.out.println("Size : " + adapter.getItemCount());
                });

    }

    private void setUpClickListeners(PackageInfo packageInfo) {
        binding.btnInfo.setOnClickListener(v -> openSystemInfo(packageInfo.packageName));
        binding.btnPlayStore.setOnClickListener(v -> openInPlayStore(packageInfo.packageName));
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