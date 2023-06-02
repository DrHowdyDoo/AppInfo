package com.drhowdydoo.appinfo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drhowdydoo.appinfo.AppInfoManager;
import com.drhowdydoo.appinfo.adapter.RecyclerViewAdapter;
import com.drhowdydoo.appinfo.bottomsheet.FilterBottomSheet;
import com.drhowdydoo.appinfo.bottomsheet.SortBottomSheet;
import com.drhowdydoo.appinfo.databinding.FragmentAppBinding;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.AppInfoComparator;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.PermissionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppFragment extends Fragment {

    private FragmentAppBinding binding;
    private RecyclerViewAdapter adapter;
    private List<AppInfo> appInfoList = new ArrayList<>();
    public int sortedState = Constants.DEFAULT_SORT;
    private int filterState = Constants.NO_FILTER;
    private AppInfoManager appInfoManager;

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
        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();
        appInfoManager = new AppInfoManager(requireActivity());
        binding.progressGroup.setVisibility(View.VISIBLE);
        appInfoManager.getAllApps(this);

        binding.btnFilter.setOnClickListener(v -> {
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet(this);
            filterBottomSheet.show(requireActivity().getSupportFragmentManager(), "filterBottomSheet");
        });

        binding.btnSort.setOnClickListener(v -> {
            SortBottomSheet sortBottomSheet = new SortBottomSheet(this);
            sortBottomSheet.show(requireActivity().getSupportFragmentManager(),"sortBottomSheet");
        });


        binding.fabScrollBack.setOnClickListener(v -> binding.recyclerView.smoothScrollToPosition(0));

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.fabScrollBack.show();
                }
                else if (dy < 0) {
                    binding.fabScrollBack.hide();
                }
            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            appInfoManager.getAllApps(this);
        });

        return rootView;
    }

    @SuppressLint("DefaultLocale")
    public void setData(List<AppInfo> appInfoList, boolean dispatch){
        this.appInfoList = appInfoList;
        if (dispatch) dispatchData();
    }

    public void dispatchData(){
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.progressGroup.setVisibility(View.GONE);
        List<AppInfo> filteredList = getFilteredList(filterState);
        AppInfoComparator appInfoComparator = new AppInfoComparator(sortedState);
        filteredList.sort(appInfoComparator);
        binding.btnSort.setText(getSortButtonText(sortedState));
        binding.btnFilter.setText(getFilterButtonText(filterState,filteredList.size()));
        adapter.setData(filteredList);
    }

    @SuppressLint("DefaultLocale")
    public void filter(int filterKey){
        filterState = filterKey;
        dispatchData();
    }

    private List<AppInfo> getFilteredList(int filter){
        if (filter == Constants.NO_FILTER) {
            return appInfoList;
        } else if (filter == Constants.FILTER_SYSTEM_APPS) {
            return appInfoList.stream().filter(AppInfo::isSystemApp).collect(Collectors.toList());
        } else if (filter == Constants.FILTER_NON_SYSTEM_APPS) {
            return appInfoList.stream().filter(appInfo -> !appInfo.isSystemApp()).collect(Collectors.toList());
        }
        return appInfoList;
    }

    public void sort(int sortBy) {
        sortedState = sortBy;
        if (sortBy == Constants.SORT_BY_LAST_USED) {
            adapter.setFlags(Constants.SHOW_LAST_USED_TIME);
            appInfoManager.addLastUsedTimeToAppInfo(appInfoList,this);
            return;
        } else adapter.setFlags(Constants.SHOW_LAST_UPDATED_TIME);
        dispatchData();
    }

    private String getSortButtonText(int sortedState) {
        switch (sortedState) {
            case Constants.DEFAULT_SORT: return "Last updated";
            case Constants.SORT_BY_NAME: return "Name";
            case Constants.SORT_BY_SIZE: return "Size";
            case Constants.SORT_BY_LAST_USED: return "Last used";
        }
        return "Sort By";
    }


    @SuppressLint("DefaultLocale")
    private String getFilterButtonText(int filterState, int size){
        switch (filterState) {
            case Constants.NO_FILTER: return String.format("%s (%d) ", "All Apps" , size);
            case Constants.FILTER_SYSTEM_APPS: return String.format("%s (%d) ", "System Apps" , size);
            case Constants.FILTER_NON_SYSTEM_APPS: return  String.format("%s (%d) ", "Non System Apps" , size);
        }

        return "Filter";
    }

}