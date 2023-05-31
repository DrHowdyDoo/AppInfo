package com.drhowdydoo.appinfo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.drhowdydoo.appinfo.databinding.FragmentApkBinding;

public class ApkFragment extends Fragment {

    public ApkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentApkBinding binding = FragmentApkBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        return rootView;
    }
}