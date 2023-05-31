package com.drhowdydoo.appinfo.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.AppInfoManager;
import com.drhowdydoo.appinfo.adapter.RecyclerViewAdapter;
import com.drhowdydoo.appinfo.bottomsheet.FilterBottomSheet;
import com.drhowdydoo.appinfo.databinding.FragmentAppBinding;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppFragment extends Fragment {

    private FragmentAppBinding binding;
    private RecyclerViewAdapter adapter;
    private List<AppInfo> appInfoList = new ArrayList<>();

    public AppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        adapter = new RecyclerViewAdapter(requireActivity(), appInfoList);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(adapter);
        AppInfoManager appInfoManager = new AppInfoManager(requireActivity());
        try {
            binding.progressGroup.setVisibility(View.VISIBLE);
            appInfoManager.getAllApps(this);
        } catch (PackageManager.NameNotFoundException | IOException e) {
            binding.progressGroup.setVisibility(View.GONE);
            throw new RuntimeException(e);
        }

        binding.btnFilter.setOnClickListener(v -> {
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet(this);
            filterBottomSheet.show(requireActivity().getSupportFragmentManager(), "filterBottomSheet");
        });

        return rootView;
    }

    public void setData(List<AppInfo> appInfoList){
        this.appInfoList = appInfoList;
        dispatchData(appInfoList, "All Apps");
    }

    public void dispatchData(List<AppInfo> appInfoList, String filterType){
        binding.btnFilter.setText(String.format("%s (%d) ", filterType, appInfoList.size()));
        binding.progressGroup.setVisibility(View.GONE);
        adapter.setData(appInfoList);
    }

    public void filter(int filterKey){
        if (filterKey == Constants.NO_FILTER) {
            dispatchData(appInfoList, "All Apps");
        } else if (filterKey == Constants.FILTER_SYSTEM_APPS) {
            List<AppInfo> filteredAppInfoList = appInfoList.stream().filter(AppInfo::isSystemApp).collect(Collectors.toList());
            dispatchData(filteredAppInfoList, "System Apps");
        } else if (filterKey == Constants.FILTER_NON_SYSTEM_APPS) {
            List<AppInfo> filteredAppInfoList = appInfoList.stream().filter(appInfo -> !appInfo.isSystemApp()).collect(Collectors.toList());
            dispatchData(filteredAppInfoList, "Non System Apps");
        }
    }

}