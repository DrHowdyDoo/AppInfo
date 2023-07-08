package com.drhowdydoo.appinfo.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drhowdydoo.appinfo.MainActivity;
import com.drhowdydoo.appinfo.adapter.AppRecyclerViewAdapter;
import com.drhowdydoo.appinfo.databinding.FragmentAppBinding;
import com.drhowdydoo.appinfo.interfaces.OnSortFilterListener;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.AppInfoComparator;
import com.drhowdydoo.appinfo.util.AppInfoManager;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.viewmodel.AppListViewModel;

import java.util.List;
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppFragment extends Fragment {

    public int sortedState;
    private FragmentAppBinding binding;
    private AppRecyclerViewAdapter adapter;
    private int filterState;
    private AppInfoManager appInfoManager;
    private OnSortFilterListener onSortFilterListener;
    private AppListViewModel appListViewModel;
    private MainActivity mainActivity;

    private boolean isPaused = false;

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

        appListViewModel = new ViewModelProvider(this).get(AppListViewModel.class);
        SharedPreferences preferences = requireContext().getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);
        adapter = new AppRecyclerViewAdapter(requireActivity(), appListViewModel.getSavedAppInfoList());
        adapter.setFlags(preferences.getInt("com.drhowdydoo.appinfo.icon-shape", Constants.SHOW_ROUND_APP_ICON));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(adapter);
        mainActivity = (MainActivity) requireActivity();

        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();
        appInfoManager = new AppInfoManager(requireActivity());
        onSortFilterListener = (OnSortFilterListener) requireActivity();
        sortedState = appListViewModel.getSortedState();
        filterState = appListViewModel.getFilterState();

        if (appListViewModel.getSavedAppInfoList().isEmpty()) {
            binding.notFound.setVisibility(View.GONE);
            binding.progressGroup.setVisibility(View.VISIBLE);
            appInfoManager.getAllApps(this);
        } else {
            onSortFilterListener.onSort(getSortButtonText(sortedState));
            onSortFilterListener.onFilter(getFilterButtonText(filterState, appListViewModel.getSavedAppInfoList().size()));
        }


        binding.fabScrollBack.setOnClickListener(v -> {
            binding.recyclerView.scrollToPosition(0);
            binding.fabScrollBack.hide();
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.fabScrollBack.show();
                }
                if (!recyclerView.canScrollVertically(-1)) {
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
    public void setData(List<AppInfo> appInfoList, boolean dispatch) {
        appListViewModel.setAppInfoList(appInfoList);
        if (dispatch) dispatchData();
    }

    public void dispatchData() {
        binding.progressGroup.setVisibility(View.GONE);
        List<AppInfo> filteredList = getFilteredList(filterState);
        AppInfoComparator appInfoComparator = new AppInfoComparator(sortedState);
        if (appListViewModel.isReverseSort()) filteredList.sort(appInfoComparator.reversed());
        else filteredList.sort(appInfoComparator);
        appListViewModel.setSavedAppInfoList(filteredList);
        if (!isPaused) {
            onSortFilterListener.onSort(getSortButtonText(sortedState));
            onSortFilterListener.onFilter(getFilterButtonText(filterState, filteredList.size()));
        }
        binding.notFound.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.setData(filteredList);
        binding.recyclerView.scrollToPosition(0);
    }

    public void hideProgressIndicators() {
        binding.progressGroup.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressLint("DefaultLocale")
    public void filter(int filterKey) {
        if (filterKey == filterState) return;
        filterState = filterKey;
        appListViewModel.setFilterState(filterState);
        dispatchData();
    }

    private List<AppInfo> getFilteredList(int filter) {
        if (filter == Constants.NO_FILTER) {
            return appListViewModel.getAppInfoList();
        } else if (filter == Constants.FILTER_SYSTEM_APPS) {
            return appListViewModel.getAppInfoList().stream().filter(AppInfo::isSystemApp).collect(Collectors.toList());
        } else if (filter == Constants.FILTER_NON_SYSTEM_APPS) {
            return appListViewModel.getAppInfoList().stream().filter(appInfo -> !appInfo.isSystemApp()).collect(Collectors.toList());
        }
        return appListViewModel.getAppInfoList();
    }

    public void sort(int sortBy, boolean isReverseSort) {
        if (sortBy == sortedState && isReverseSort == appListViewModel.isReverseSort()) return;
        appListViewModel.setReverseSort(isReverseSort);
        sortedState = sortBy;
        appListViewModel.setSortedState(sortedState);
        if (sortBy == Constants.SORT_BY_LAST_USED) {
            adapter.setFlags(Constants.SHOW_LAST_USED_TIME);
            appInfoManager.addLastUsedTimeToAppInfo(appListViewModel.getAppInfoList(), this);
            return;
        } else adapter.setFlags(Constants.SHOW_LAST_UPDATED_TIME);
        if (sortBy == Constants.SORT_BY_MOST_USED) {
            adapter.setFlags(Constants.HIDE_APP_STATS);
            appInfoManager.addForegroundTimeToAppInfo(appListViewModel.getAppInfoList(), this);
            return;
        }
        dispatchData();
    }

    private String getSortButtonText(int sortedState) {
        switch (sortedState) {
            case Constants.DEFAULT_SORT:
                return "Last updated";
            case Constants.SORT_BY_NAME:
                return "Name";
            case Constants.SORT_BY_SIZE:
                return "Size";
            case Constants.SORT_BY_LAST_USED:
                return "Last used";
            case Constants.SORT_BY_MOST_USED:
                if (appListViewModel.isReverseSort()) return "Least used";
                return "Most used";
        }
        return "Sort By";
    }


    @SuppressLint("DefaultLocale")
    private String getFilterButtonText(int filterState, int size) {
        switch (filterState) {
            case Constants.NO_FILTER:
                return size > 0 ? String.format("%s (%d) ", "All Apps", size) : "All Apps";
            case Constants.FILTER_SYSTEM_APPS:
                return size > 0 ? String.format("%s (%d) ", "System Apps", size) : "System Apps";
            case Constants.FILTER_NON_SYSTEM_APPS:
                return size > 0 ? String.format("%s (%d) ", "Non System Apps", size) : "Non System Apps";
        }

        return "Filter";
    }

    public boolean isReversedSort() {
        return appListViewModel.isReverseSort();
    }

    public int getSortedState() {
        return appListViewModel.getSortedState();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        mainActivity.onFilter(getFilterButtonText(filterState, adapter.getItemCount()));
        mainActivity.onSort(getSortButtonText(sortedState));
    }

    public void search(String input) {
        if (!input.isEmpty()) {
            List<AppInfo> searchResults = appListViewModel.getSavedAppInfoList()
                    .stream()
                    .filter(appInfo -> appInfo.getAppName().toLowerCase().startsWith(input.toLowerCase()) || appInfo.getPackageName().toLowerCase().startsWith(input.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateData(searchResults);
            onSortFilterListener.onFilter(getFilterButtonText(filterState, adapter.getItemCount()));
            if (searchResults.isEmpty()) binding.tvNoResultsFound.setVisibility(View.VISIBLE);
            else binding.tvNoResultsFound.setVisibility(View.GONE);
        } else {
            adapter.setData(appListViewModel.getSavedAppInfoList());
            onSortFilterListener.onFilter(getFilterButtonText(filterState, adapter.getItemCount()));
            binding.tvNoResultsFound.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }
}