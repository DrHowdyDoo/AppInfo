package com.drhowdydoo.appinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.drhowdydoo.appinfo.databinding.ActivitySettingsBinding;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.ThemeUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("com.drhowdydoo.appinfo.preferences", MODE_PRIVATE);
        editor = preferences.edit();

        init();
        handleTheme();
        handleIconShape();
        handleScan();


    }

    private void handleScan() {
        binding.switchScanHiddenFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("com.drhowdydoo.appinfo.scan-hidden-folders",isChecked).apply();
        });
    }

    private void handleIconShape() {
        binding.btnRoundIcon.setOnClickListener(v -> {
            binding.btnRoundIcon.setChecked(true);
            binding.btnSquircleIcon.setChecked(false);
            editor.putInt("com.drhowdydoo.appinfo.icon-shape", Constants.SHOW_ROUND_APP_ICON).apply();
        });
        binding.btnSquircleIcon.setOnClickListener(v -> {
            binding.btnSquircleIcon.setChecked(true);
            binding.btnRoundIcon.setChecked(false);
            editor.putInt("com.drhowdydoo.appinfo.icon-shape",Constants.SHOW_SQUARE_APP_ICON).apply();
        });
    }

    private void handleTheme() {
        binding.btnDarkTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_YES).apply();
            binding.btnDarkTheme.setChecked(true);
            binding.btnSystemTheme.setChecked(false);
            binding.btnLightTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
        });
        binding.btnLightTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme",AppCompatDelegate.MODE_NIGHT_NO).apply();
            binding.btnLightTheme.setChecked(true);
            binding.btnSystemTheme.setChecked(false);
            binding.btnDarkTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.btnSystemTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
            binding.btnSystemTheme.setChecked(true);
            binding.btnDarkTheme.setChecked(false);
            binding.btnLightTheme.setChecked(false);
            applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });
    }

    private void init() {
        int theme = preferences.getInt("com.drhowdydoo.appinfo.theme",0);
        if (theme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            binding.btnSystemTheme.setChecked(true);
        } else if (theme == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.btnDarkTheme.setChecked(true);
        } else {
            binding.btnLightTheme.setChecked(true);
        }

        int iconShape = preferences.getInt("com.drhowdydoo.appinfo.icon-shape",0);
        if (iconShape == Constants.SHOW_ROUND_APP_ICON) binding.btnRoundIcon.setChecked(true);
        else binding.btnSquircleIcon.setChecked(true);

        boolean scanHiddenFolders = preferences.getBoolean("com.drhowdydoo.appinfo.scan-hidden-folders",true);
        binding.switchScanHiddenFiles.setChecked(scanHiddenFolders);
    }

    /**
     * @param mode Smoothly applies theme change with fading transition.
     *             You have to add android:configChanges="uiMode" in the activity manifest.
     */
    private void applyTheme(int mode) {
        ThemeUtils.applyTheme(this,mode);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}