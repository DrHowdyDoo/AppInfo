package com.drhowdydoo.appinfo.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drhowdydoo.appinfo.databinding.BottomSheetShareBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShareBottomSheet extends BottomSheetDialogFragment {


    public ShareBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetShareBinding binding = BottomSheetShareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
