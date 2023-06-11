package com.drhowdydoo.appinfo.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.BottomSheetFilterBinding;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.util.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {


    private Fragment fragment;

    public FilterBottomSheet(Fragment fragment) {
        this.fragment = fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetFilterBinding binding = BottomSheetFilterBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.btnFilterAllApps.setOnClickListener(this);
        binding.btnFilterSystemApps.setOnClickListener(this);
        binding.btnFilterNonSystemApps.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (fragment instanceof AppFragment) {
            AppFragment appFragment = (AppFragment) fragment;
            if (id == R.id.btnFilterAllApps) {
                appFragment.filter(Constants.NO_FILTER);
            } else if (id == R.id.btnFilterSystemApps) {
                appFragment.filter(Constants.FILTER_SYSTEM_APPS);
            } else if (id == R.id.btnFilterNonSystemApps) {
                appFragment.filter(Constants.FILTER_NON_SYSTEM_APPS);
            }
        }

        dismiss();
    }
}
