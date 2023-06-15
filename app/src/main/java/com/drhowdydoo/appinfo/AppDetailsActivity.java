package com.drhowdydoo.appinfo;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Utility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.color.DynamicColors;

import java.util.Optional;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private ActivityAppDetailsBinding binding;
    private AppDetailsManager appDetailsManager;

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

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

        binding.tvVersion.setText("v " + getIntent().getStringExtra("appVersion"));
        binding.tvTitle.setText(getIntent().getStringExtra("appName"));
        binding.appBar.addOnOffsetChangedListener(this);
        startAlphaAnimation(binding.tvTitle, 0, View.INVISIBLE);

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

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(binding.tvTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(binding.tvTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.linearLayoutTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.linearLayoutTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }
}