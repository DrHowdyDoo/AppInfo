package com.drhowdydoo.appinfo.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.BottomSheetFilterBinding;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.util.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {


    private AppFragment fragment;

    public FilterBottomSheet(AppFragment fragment) {
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

        if (id == R.id.btnFilterAllApps) {
            fragment.filter(Constants.NO_FILTER);
        } else if (id == R.id.btnFilterSystemApps) {
            fragment.filter(Constants.FILTER_SYSTEM_APPS);
        } else if (id == R.id.btnFilterNonSystemApps) {
            fragment.filter(Constants.FILTER_NON_SYSTEM_APPS);
        }

        dismiss();
    }
}
