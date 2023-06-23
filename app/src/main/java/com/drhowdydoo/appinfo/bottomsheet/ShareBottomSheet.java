package com.drhowdydoo.appinfo.bottomsheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.BottomSheetShareBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;

public class ShareBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{

    private final String packageName;
    private final String appName;
    private final String apkPath;
    private BottomSheetShareBinding binding;

    public ShareBottomSheet(String packageName, String appName, String apkPath) {
        this.packageName = packageName;
        this.appName = appName;
        this.apkPath = apkPath;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetShareBinding.inflate(inflater, container, false);
        binding.btnShareApk.setOnClickListener(this);
        binding.btnShareLink.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnShareLink) {
            // Share App's playstore link
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String PLAYSTORE_LINK = "https://play.google.com/store/apps/details?id=" + packageName;
            shareIntent.putExtra(Intent.EXTRA_TEXT, appName + "\n" + PLAYSTORE_LINK);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
        if (id == R.id.btnShareApk) {
            // Share App"s Apk itself
            try {
                shareApkFile(apkPath, appName);
            } catch (IllegalArgumentException exception) {
                exception.printStackTrace();
                //Show error
            }

        }
    }

    private void shareApkFile(String apkPath, String appName) {
        Uri apkUri = Uri.parse("content://com.drhowdydoo.appinfo.provider" + apkPath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.android.package-archive");
        shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
        shareIntent.putExtra(Intent.EXTRA_TITLE, appName);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share APK via"));
    }
}
