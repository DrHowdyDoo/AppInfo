package com.drhowdydoo.appinfo.bottomsheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.BottomSheetShareBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShareBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{

    private final String packageName;
    private final String appName;

    public ShareBottomSheet(String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetShareBinding binding = BottomSheetShareBinding.inflate(inflater, container, false);
        binding.btnShareApk.setOnClickListener(this);
        binding.btnShareLink.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnShareLink) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String PLAYSTORE_LINK = "https://play.google.com/store/apps/details?id=" + packageName;
            shareIntent.putExtra(Intent.EXTRA_TEXT, appName + "\n" + PLAYSTORE_LINK);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
        if (id == R.id.btnShareApk) {
            // Share Apk
        }
    }
}
