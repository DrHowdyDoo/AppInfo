package com.drhowdydoo.appinfo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.drhowdydoo.appinfo.databinding.ActivitySettingsBinding;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.ThemeUtils;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("SettingsActivity Recreated with theme");
        preferences = getSharedPreferences("com.drhowdydoo.appinfo.preferences", MODE_PRIVATE);
        editor = preferences.edit();
        int themeMode = preferences.getInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        ThemeUtils.applyTheme(this, themeMode);
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.materialToolBar.setNavigationOnClickListener(v -> onBackPressed());
        init();
        handleTheme();
        handleScan();

    }

    private void handleScan() {
        binding.switchScanHiddenFiles.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("com.drhowdydoo.appinfo.scan-hidden-folders", isChecked).apply());
        binding.switchShowSplitApks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.switchShowSplitApks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.round_remove_red_eye,0,0,0);
            else binding.switchShowSplitApks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.round_visibility_off,0,0,0);
            editor.putBoolean("com.drhowdydoo.appinfo.show-split-apks", isChecked).apply();
        });

        CharSequence[] scanModes = {"Quick scan", "Full scan", "Smart scan"};
        String[] scanModesSubs = {"Checks folders in your system where apks are commonly found",
                                  "Checks all folders for apks. This could take a little longer",
                                  "Quick scan but with wider search"};

        SpannableString[] spans = new SpannableString[3];
        spans[0] = new SpannableString(scanModes[0] + "\n" + scanModesSubs[0]);
        spans[1] = new SpannableString(scanModes[1] + "\n" + scanModesSubs[1]);
        spans[2] = new SpannableString(scanModes[2] + "\n" + scanModesSubs[2]);


        spans[0].setSpan(new TextAppearanceSpan(this,com.google.android.material.R.style.TextAppearance_Material3_BodySmall), scanModes[0].length() + 1, (scanModes[0].length() + scanModesSubs[0].length() + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spans[1].setSpan(new TextAppearanceSpan(this,com.google.android.material.R.style.TextAppearance_Material3_BodySmall), scanModes[1].length() + 1, (scanModes[1].length() + scanModesSubs[1].length() + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spans[2].setSpan(new TextAppearanceSpan(this,com.google.android.material.R.style.TextAppearance_Material3_BodySmall), scanModes[2].length() + 1, (scanModes[2].length() + scanModesSubs[2].length() + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ArrayAdapter<SpannableString> adapter = new ArrayAdapter<>(this, R.layout.list_item_layout, spans) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(spans[position]);
                return textView;
            }
        };

        int scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode",2);

        binding.btnScanMode.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Scan mode")
                        .setSingleChoiceItems(adapter, scanMode, (dialog, which) -> {
                            editor.putInt("com.drhowdydoo.appinfo.scan-mode",which).apply();
                            setScanModeSub(which);
                            dialog.dismiss();
                        })
                        .setPositiveButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
        });
    }


    private void handleTheme() {
        binding.btnDarkTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_YES).apply();
            binding.btnDarkTheme.setChecked(true);
            binding.btnSystemTheme.setChecked(false);
            binding.btnLightTheme.setChecked(false);
            applyTheme();
        });
        binding.btnLightTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_NO).apply();
            binding.btnLightTheme.setChecked(true);
            binding.btnSystemTheme.setChecked(false);
            binding.btnDarkTheme.setChecked(false);
            applyTheme();
        });
        binding.btnSystemTheme.setOnClickListener(v -> {
            editor.putInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
            binding.btnSystemTheme.setChecked(true);
            binding.btnDarkTheme.setChecked(false);
            binding.btnLightTheme.setChecked(false);
            applyTheme();
        });
    }

    private void init() {
        int theme = preferences.getInt("com.drhowdydoo.appinfo.theme", 0);
        if (theme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            binding.btnSystemTheme.setChecked(true);
        } else if (theme == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.btnDarkTheme.setChecked(true);
        } else {
            binding.btnLightTheme.setChecked(true);
        }

        boolean scanHiddenFolders = preferences.getBoolean("com.drhowdydoo.appinfo.scan-hidden-folders", true);
        binding.switchScanHiddenFiles.setChecked(scanHiddenFolders);

        boolean showSplitApks = preferences.getBoolean("com.drhowdydoo.appinfo.show-split-apks", true);
        binding.switchShowSplitApks.setChecked(showSplitApks);
        if (showSplitApks) binding.switchShowSplitApks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.round_remove_red_eye,0,0,0);
        else binding.switchShowSplitApks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.round_visibility_off,0,0,0);

        int scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode",2);
        setScanModeSub(scanMode);
    }

    private void setScanModeSub(int mode) {
        String scanModeTitle = "Scan mode";
        String scanModeSubTitle = mode == 0 ? "Quick scan" : mode == 1 ? "Full scan" : "Smart scan";
        SpannableString span = new SpannableString(scanModeTitle + "\n" + scanModeSubTitle);
        span.setSpan(new TextAppearanceSpan(this,
                        com.google.android.material.R.style.TextAppearance_Material3_BodySmall),
                scanModeTitle.length(),
                (scanModeTitle.length() + scanModeSubTitle.length() + 1),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.btnScanMode.setText(span);
    }

    /**
     * Smoothly applies theme change with fading transition.
     */
    private void applyTheme() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}