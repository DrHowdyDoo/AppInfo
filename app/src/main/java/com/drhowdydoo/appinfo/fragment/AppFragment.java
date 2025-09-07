package com.drhowdydoo.appinfo.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
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

    private SharedPreferences preferences;
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
        preferences = requireContext().getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Constants.RESULT_CODE_UNINSTALL) {
                        appListViewModel.getAppInfoList().removeIf(appInfo -> appInfo.getPackageName() != null &&
                                result.getData() != null &&
                                result.getData().getStringExtra("appUninstalled") != null &&
                                appInfo.getPackageName().equalsIgnoreCase(result.getData().getStringExtra("appUninstalled")));
                        appListViewModel.getSavedAppInfoList().removeIf(appInfo -> appInfo.getPackageName() != null &&
                                result.getData() != null &&
                                result.getData().getStringExtra("appUninstalled") != null &&
                                appInfo.getPackageName().equalsIgnoreCase(result.getData().getStringExtra("appUninstalled")));
                        adapter.updateData(appListViewModel.getSavedAppInfoList());
                    }
                });

        adapter = new AppRecyclerViewAdapter(requireActivity(), appListViewModel.getSavedAppInfoList(),activityResultLauncher);
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
        adapter.setData(filteredList);
        binding.recyclerView.scheduleLayoutAnimation();
        binding.recyclerView.scrollToPosition(0);
        binding.notFound.setVisibility(adapter.getItemCount() == 0
                && !binding.progressGroup.isShown() ?
                View.VISIBLE : View.GONE);
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
        else if (filter == Constants.FILTER_NON_PLAYSTORE_APPS) {
            return appListViewModel.getAppInfoList().stream().filter(appInfo -> !appInfo.getInstallSource().equalsIgnoreCase("com.android.vending") && !appInfo.isSystemApp()).collect(Collectors.toList());
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
        } else if (sortBy == Constants.SORT_BY_INSTALL_DATE) {
            adapter.setFlags(Constants.SHOW_INSTALL_DATE);
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
            case Constants.SORT_BY_INSTALL_DATE:
                return "Install date";
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
            case Constants.FILTER_NON_PLAYSTORE_APPS:
                return size > 0 ? String.format("%s (%d) ", "Sideloaded", size) : "Sideloaded";
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
                    .filter(appInfo -> (appInfo.getAppName() != null && searchIn(input.toLowerCase(), appInfo.getAppName().toLowerCase())) ||
                    (appInfo.getPackageName() != null && searchIn(input.toLowerCase(), appInfo.getPackageName().toLowerCase())))
                    .collect(Collectors.toList());
            adapter.updateData(searchResults);
            onSortFilterListener.onFilter(getFilterButtonText(filterState, adapter.getItemCount()));
        } else {
            adapter.setData(appListViewModel.getSavedAppInfoList());
            onSortFilterListener.onFilter(getFilterButtonText(filterState, adapter.getItemCount()));
        }
        binding.notFound.setVisibility(adapter.getItemCount() == 0
                && !(binding.progressGroup.getVisibility() == View.VISIBLE) ?
                View.VISIBLE : View.GONE);
    }

    private boolean searchIn(String input, String target) {
        int searchType = preferences.getInt("com.drhowdydoo.appinfo.search-type", 0);
        if (searchType == 0) return target.startsWith(input);
        else return target.contains(input);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }
}