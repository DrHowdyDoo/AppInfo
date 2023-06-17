package com.drhowdydoo.appinfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Utilities;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.DynamicColors;

import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppDetailsActivity extends AppCompatActivity {

    private ActivityAppDetailsBinding binding;
    private AppDetailsManager appDetailsManager;
    private boolean isApk = false;
    private boolean isInstalled = true;
    private String apkPath = "";
    private String apkAbsolutePath = "";

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        isApk = intent.getBooleanExtra("isApk",false);
        if (isApk) {
            PackageInfo apkInfo = intent.getParcelableExtra("apkInfo");
            appDetailsManager = new AppDetailsManager(this,apkInfo);
            apkPath = intent.getStringExtra("apkPath");
            apkAbsolutePath = intent.getStringExtra("apkAbsolutePath");
            isInstalled = intent.getBooleanExtra("isInstalled",true);
            init(apkInfo);
        } else {
            ApplicationInfo appInfo = intent.getParcelableExtra("appInfo");
            Observable.fromCallable(() -> getPackageInfoByAppInfo(appInfo))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(packageInfo -> {
                        packageInfo.ifPresent(value -> appDetailsManager = new AppDetailsManager(this,value));
                        packageInfo.ifPresent(this::init);
                    });
        }

        handleToolbarContentAlignment();
        if (!isInstalled) {
            binding.btnInfo.setEnabled(false);
        }

    }

    private Optional<PackageInfo> getPackageInfoByAppInfo(ApplicationInfo appInfo) {
        try {
            return Optional.ofNullable(getPackageManager().getPackageInfo(appInfo.packageName,
                    PackageManager.GET_PERMISSIONS |
                            PackageManager.GET_RECEIVERS |
                            PackageManager.GET_PROVIDERS |
                            PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_SERVICES |
                            PackageManager.GET_META_DATA |
                            PackageManager.GET_SIGNATURES));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @SuppressLint({"CheckResult", "SetTextI18n"})
    private void init(PackageInfo packageInfo) {

        setUpClickListeners(packageInfo);
        binding.materialToolBar.setTitle(getIntent().getStringExtra("appName"));
        binding.tvVersion.setText("v" + getIntent().getStringExtra("appVersion"));
        binding.tvMinSdkValue.setText(appDetailsManager.getMinSdk());
        binding.tvTargetSdkValue.setText(appDetailsManager.getTargetSdk());
        binding.tvSourceValue.setText(appDetailsManager.getInstallSource());
        binding.tvInstalledDtValue.setText(appDetailsManager.getInstalledDate());
        binding.tvUpdatedDtValue.setText(appDetailsManager.getUpdatedDate());


        Observable.fromCallable(() -> appDetailsManager.getIcon(isApk,apkAbsolutePath))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> {
                    binding.imgIcon.setImageDrawable(icon);
                });

        Observable.fromCallable(() -> appDetailsManager.getCategory())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(category -> binding.tvCategoryValue.setText(category));

    }

    private void setUpClickListeners(PackageInfo packageInfo) {
        binding.btnInfo.setOnClickListener(v -> openSystemInfo(packageInfo.packageName));
        binding.btnPlayStore.setOnClickListener(v -> openInPlayStore(packageInfo.packageName));
    }

    private void handleToolbarContentAlignment(){
        binding.tvVersion.post(() -> {
            if (binding.tvVersion.getLineCount() > 1) {
                System.out.println("handleToolbarContentAlignment()");
                binding.collapsingToolBar.setExpandedTitleMarginBottom(Utilities.dpToPx(this,58));
                CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) binding.tvVersion.getLayoutParams();
                layoutParams.bottomMargin = Utilities.dpToPx(this,10);
                binding.tvVersion.setLayoutParams(layoutParams);
            }
        });

    }

    private void openSystemInfo(String packageName){
        Intent systemInfo = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        systemInfo.setData(Uri.parse("package:" + packageName));
        startActivity(systemInfo);
    }

    private void openInPlayStore(String packageName){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
    }

}