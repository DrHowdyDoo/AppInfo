package com.drhowdydoo.appinfo.bottomsheet;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.BottomSheetSortApkBinding;
import com.drhowdydoo.appinfo.databinding.BottomSheetSortBinding;
import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.PermissionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SortBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    private Fragment fragment;
    private BottomSheetSortBinding appBinding;
    private BottomSheetSortApkBinding apkBinding;

    public SortBottomSheet(Fragment fragment) {
        this.fragment = fragment;
    }

    public SortBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (fragment == null) dismiss();
        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            appBinding = BottomSheetSortBinding.inflate(inflater, container, false);
            appBinding.btnSortByName.setOnClickListener(this);
            appBinding.btnSortBySize.setOnClickListener(this);
            appBinding.btnSortByLastUpdate.setOnClickListener(this);
            appBinding.btnSortByLastUsed.setOnClickListener(this);
            appBinding.btnSortByMostUsed.setOnClickListener(this);
            appBinding.switchReverseSort.setChecked(((AppFragment) fragment).isReversedSort());
            appBinding.btnSortByMostUsed.setText(textSwitcher(appBinding.switchReverseSort.isChecked()));
            appBinding.switchReverseSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
                appBinding.btnSortByMostUsed.setText(textSwitcher(isChecked));
                appFragment.sort(appFragment.getSortedState(), isChecked);
            });

            if (PermissionManager.hasUsageStatsPermission(requireActivity())) {
                appBinding.tvLockedTitle.setVisibility(View.GONE);
            }

            return appBinding.getRoot();
        } else {
            apkBinding = BottomSheetSortApkBinding.inflate(inflater, container, false);
            apkBinding.btnSortBySize.setOnClickListener(this);
            apkBinding.btnSortByName.setOnClickListener(this);
            return apkBinding.getRoot();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionManager.hasUsageStatsPermission(requireActivity())) {
            if (appBinding != null) appBinding.tvLockedTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            boolean isReverseSort = appBinding.switchReverseSort.isChecked();
            if (id == R.id.btnSortByName) appFragment.sort(Constants.SORT_BY_NAME, isReverseSort);
            else if (id == R.id.btnSortBySize)
                appFragment.sort(Constants.SORT_BY_SIZE, isReverseSort);
            else if (id == R.id.btnSortByLastUpdate)
                appFragment.sort(Constants.SORT_BY_LAST_UPDATED, isReverseSort);
            else if (id == R.id.btnSortByLastUsed) {
                if (!PermissionManager.hasUsageStatsPermission(requireActivity())) {
                    getPermission();
                    return;
                }
                appFragment.sort(Constants.SORT_BY_LAST_USED, isReverseSort);
            } else if (id == R.id.btnSortByMostUsed) {
                if (!PermissionManager.hasUsageStatsPermission(requireActivity())) {
                    getPermission();
                    return;
                }
                appFragment.sort(Constants.SORT_BY_MOST_USED, isReverseSort);
            }

        } else if (fragment instanceof ApkFragment) {
            ApkFragment apkFragment = (ApkFragment) fragment;
            if (id == R.id.btnSortByName) apkFragment.sort(Constants.SORT_BY_NAME);
            else if (id == R.id.btnSortBySize) apkFragment.sort(Constants.SORT_BY_SIZE);
        }
        dismiss();
    }

    private void getPermission() {
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .setData(Uri.parse("package:" + "com.drhowdydoo.appinfo")));
        } catch (ActivityNotFoundException activityNotFoundException) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.baseline_gpp_maybe_24)
                    .setTitle("Permission required   😅")
                    .setMessage("Usage access required to use this sort option\n" +
                            "To grant the permission, please follow these steps:\n" +
                            "Go to Settings -> Apps & notifications -> Advanced -> Special app access -> Usage access -> AppInfo\n" +
                            "Then look for the 'Permit usage access' permission and ensure it is enabled.\n")
                    .setPositiveButton("Got it", (dialog, which) -> dismiss())
                    .show();
        }

    }

    private String textSwitcher(boolean isReverse) {
        return isReverse ? "Least used" : "Most used";
    }

}
