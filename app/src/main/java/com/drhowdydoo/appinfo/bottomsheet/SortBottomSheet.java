package com.drhowdydoo.appinfo.bottomsheet;

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

public class SortBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    private Fragment fragment;
    private BottomSheetSortBinding binding;
    private BottomSheetSortApkBinding apkBinding;

    public SortBottomSheet(Fragment fragment) {
        this.fragment = fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            binding = BottomSheetSortBinding.inflate(inflater, container, false);
            binding.btnSortByName.setOnClickListener(this);
            binding.btnSortBySize.setOnClickListener(this);
            binding.btnSortByLastUpdate.setOnClickListener(this);
            binding.btnSortByLastUsed.setOnClickListener(this);
            binding.btnSortByMostUsed.setOnClickListener(this);
            binding.switchReverseSort.setChecked(((AppFragment) fragment).isReversedSort());
            binding.btnSortByMostUsed.setText(textSwitcher(binding.switchReverseSort.isChecked()));
            binding.switchReverseSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
                binding.btnSortByMostUsed.setText(textSwitcher(isChecked));
                appFragment.sort(appFragment.getSortedState(),isChecked);
                dismiss();
            });

            if (PermissionManager.hasUsageStatsPermission(requireActivity())) {
                binding.tvLockedTitle.setVisibility(View.GONE);
            }

            return binding.getRoot();
        }
        else {
            apkBinding = BottomSheetSortApkBinding.inflate(inflater, container, false);
            return apkBinding.getRoot();
        }

    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            boolean isReverseSort = binding.switchReverseSort.isChecked();
            if (id == R.id.btnSortByName) appFragment.sort(Constants.SORT_BY_NAME,isReverseSort);
            else if (id == R.id.btnSortBySize) appFragment.sort(Constants.SORT_BY_SIZE,isReverseSort);
            else if (id == R.id.btnSortByLastUpdate) appFragment.sort(Constants.SORT_BY_LAST_UPDATED,isReverseSort);
            else if (id == R.id.btnSortByLastUsed) {
                if (!PermissionManager.hasUsageStatsPermission(requireActivity())) {
                    getPermission();
                    return;
                }
                appFragment.sort(Constants.SORT_BY_LAST_USED,isReverseSort);
            } else if (id == R.id.btnSortByMostUsed) {
                if (!PermissionManager.hasUsageStatsPermission(requireActivity())) {
                    getPermission();
                    return;
                }
                appFragment.sort(Constants.SORT_BY_MOST_USED,isReverseSort);
            }

        } else if (fragment instanceof ApkFragment){
            ApkFragment apkFragment = (ApkFragment) fragment;
        }
        dismiss();
    }

    private void getPermission() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .setData(Uri.parse("package:" + "com.drhowdydoo.appinfo")));
    }

    private String textSwitcher(boolean isReverse){
        return isReverse ? "Least used" : "Most used";
    }

}
