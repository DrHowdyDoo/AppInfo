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
import com.drhowdydoo.appinfo.databinding.BottomSheetSortBinding;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.PermissionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{

    private Fragment fragment;

    public SortBottomSheet(Fragment fragment) {
        this.fragment = fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetSortBinding binding = BottomSheetSortBinding.inflate(inflater,container,false);
        binding.btnSortByName.setOnClickListener(this);
        binding.btnSortBySize.setOnClickListener(this);
        binding.btnSortByLastUpdate.setOnClickListener(this);
        binding.btnSortByLastUsed.setOnClickListener(this);

        if (PermissionManager.hasUsageStatsPermission(requireActivity())) {
            binding.tvLockedTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.round_lock_open_24,0,0,0);
        } else {
            binding.tvLockedTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_lock_24,0,0,0);
        }

        return binding.getRoot();
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            if (id == R.id.btnSortByName) appFragment.sort(Constants.SORT_BY_NAME);
            else if (id == R.id.btnSortBySize) appFragment.sort(Constants.SORT_BY_SIZE);
            else if (id == R.id.btnSortByLastUpdate) appFragment.sort(Constants.SORT_BY_LAST_UPDATED);
            else if (id == R.id.btnSortByLastUsed) {
                if (!PermissionManager.hasUsageStatsPermission(requireActivity())) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            .setData(Uri.parse("package:" + "com.drhowdydoo.appinfo")));
                    return;
                }
                appFragment.sort(Constants.SORT_BY_LAST_USED);
            }
        }
        dismiss();
    }
}
