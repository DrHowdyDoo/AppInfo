package com.drhowdydoo.appinfo;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.drhowdydoo.appinfo.databinding.ActivityAppDetailsBinding;
import com.drhowdydoo.appinfo.util.AppDetailsManager;

public class AppDetailsActivity extends AppCompatActivity {

    private ActivityAppDetailsBinding appDetailsBinding;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDetailsBinding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(appDetailsBinding.getRoot());
        boolean isApk = getIntent().getBooleanExtra("isApk",false);
        if (isApk) {
            PackageInfo apkInfo = getIntent().getParcelableExtra("apkInfo");
        } else {
            ApplicationInfo appInfo = getIntent().getParcelableExtra("appInfo");
        }


        
    }

}