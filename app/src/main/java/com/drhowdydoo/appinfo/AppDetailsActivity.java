package com.drhowdydoo.appinfo;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;

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

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean isApk = getIntent().getBooleanExtra("isApk",false);
        if (isApk) {
            PackageInfo apkInfo = getIntent().getParcelableExtra("apkInfo");
            appDetailsManager = new AppDetailsManager(this,apkInfo);
            init();
        } else {
            ApplicationInfo appInfo = getIntent().getParcelableExtra("appInfo");
            Observable.fromCallable(() -> getPackageInfoByAppInfo(appInfo))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(packageInfo -> {
                        packageInfo.ifPresent(value -> appDetailsManager = new AppDetailsManager(this,value));
                        init();
                    });
        }

        binding.materialToolBar.setTitle(getIntent().getStringExtra("appName"));
        binding.tvVersion.setText("v " + getIntent().getStringExtra("appVersion"));
        handleToolbarContentAlignment();

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

    @SuppressLint("CheckResult")
    private void init() {
        Observable.fromCallable(() -> appDetailsManager.getIcon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> {
                    binding.imgIcon.setImageDrawable(icon);
                });

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

}