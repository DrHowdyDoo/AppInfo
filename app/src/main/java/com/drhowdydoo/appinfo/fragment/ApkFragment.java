package com.drhowdydoo.appinfo.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.MainActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.adapter.ApkRecyclerViewAdapter;
import com.drhowdydoo.appinfo.databinding.FragmentApkBinding;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.ApkInfoComparator;
import com.drhowdydoo.appinfo.util.ApkInfoManager;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.Utilities;
import com.drhowdydoo.appinfo.viewmodel.ApkListViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ApkFragment extends Fragment implements View.OnClickListener {

    private FragmentApkBinding binding;
    private ApkRecyclerViewAdapter adapter;
    private ApkListViewModel apkListViewModel;
    private ApkInfoManager apkInfoManager;
    private MainActivity mainActivity;
    private boolean isPaused = false;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private boolean permissionAskedForFirstTime = true;

    public ApkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApkBinding.inflate(inflater, container, false);

        checkStoragePermission();
        binding.btnStorageAccess.setOnClickListener(this);
        apkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);
        SharedPreferences preferences = requireContext().getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);
        adapter = new ApkRecyclerViewAdapter(requireActivity(), apkListViewModel.getSavedApkInfoList());
        adapter.setFlags(preferences.getInt("com.drhowdydoo.appinfo.icon-shape", Constants.SHOW_ROUND_APP_ICON));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(adapter);
        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();
        mainActivity = (MainActivity) requireActivity();

        apkInfoManager = new ApkInfoManager(requireActivity());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            apkInfoManager.getAllApks(Environment.getExternalStorageDirectory(), this);
        });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        getAllApks();
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        getAllApks();
    }

    private void getAllApks(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) return;

        binding.groupStoragePermission.setVisibility(View.GONE);
        if (apkListViewModel.getSavedApkInfoList().isEmpty() && Utilities.shouldSearchApks) {
            Utilities.shouldSearchApks = false;
            binding.progressGroup.setVisibility(View.VISIBLE);
            binding.notFound.setVisibility(View.GONE);
            apkInfoManager.getAllApks(Environment.getExternalStorageDirectory(), this);
        }
        mainActivity.onFilter(getFilterText());
        mainActivity.onSort(getSortText());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            binding.groupStoragePermission.setVisibility(View.VISIBLE);
        } else binding.groupStoragePermission.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            binding.tvStoragePermission.setText("App needs storage access\nto search for all apks");
            binding.groupStoragePermission.setVisibility(View.VISIBLE);
        }else binding.groupStoragePermission.setVisibility(View.GONE);
    }


    public void setData(List<ApkInfo> apkInfoList, boolean dispatch) {
        binding.notFound.setVisibility(apkInfoList.isEmpty() ? View.VISIBLE : View.GONE);
        apkListViewModel.setApkInfoList(apkInfoList);
        if (dispatch) dispatchData();
    }

    private void dispatchData() {
        binding.progressGroup.setVisibility(View.GONE);
        List<ApkInfo> filteredList = getFilteredList(apkListViewModel.getFilterState());
        filteredList.sort(new ApkInfoComparator(apkListViewModel.getSortState()));
        apkListViewModel.setSavedApkInfoList(filteredList);
        adapter.setData(filteredList);
        binding.recyclerView.scrollToPosition(0);
        if (!isPaused) {
            mainActivity.onFilter(getFilterText());
            mainActivity.onSort(getSortText());
        }
    }

    public void hideProgressBar() {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.progressGroup.setVisibility(View.GONE);
    }


    private String getSortText() {
        if (apkListViewModel.getSortState() == Constants.SORT_BY_NAME) return "Name";
        else return "Size";
    }

    private String getFilterText() {
        int filter = apkListViewModel.getFilterState();
        int count = adapter.getItemCount();
        String suffix = count > 0 ? "(" + count + ")" : "";
        if (filter == Constants.FILTER_INSTALLED_APKS)
            return "Installed " + suffix;
        else if (filter == Constants.FILTER_NOT_INSTALLED_APKS)
            return "Not Installed " + suffix;
        else return "All Apks " + suffix;
    }


    public void sort(int sortBy) {
        if (sortBy == apkListViewModel.getSortState()) return;
        apkListViewModel.setSortState(sortBy);
        dispatchData();
    }

    public void filter(int filter) {
        if (filter == apkListViewModel.getFilterState()) return;
        apkListViewModel.setFilterState(filter);
        dispatchData();
    }

    private List<ApkInfo> getFilteredList(int filter) {
        if (filter == Constants.NO_FILTER) return apkListViewModel.getApkInfoList();
        if (filter == Constants.FILTER_INSTALLED_APKS)
            return apkListViewModel.getApkInfoList().stream().filter(ApkInfo::isInstalled).collect(Collectors.toList());
        if (filter == Constants.FILTER_NOT_INSTALLED_APKS)
            return apkListViewModel.getApkInfoList().stream().filter(apkInfo -> !apkInfo.isInstalled()).collect(Collectors.toList());
        return apkListViewModel.getApkInfoList();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnStorageAccess) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
                startActivity(intent);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                //ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !permissionAskedForFirstTime) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
                    startActivity(intent);
                } else {
                    permissionAskedForFirstTime = false;
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        }
    }

    public void search(String input) {
        if (!input.isEmpty()) {
            List<ApkInfo> searchResults = apkListViewModel.getSavedApkInfoList()
                    .stream()
                    .filter(apkInfo -> apkInfo.getApkName().toLowerCase().startsWith(input.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateData(searchResults);
            mainActivity.onFilter(getFilterText());
            if (searchResults.isEmpty()) binding.tvNoResultsFound.setVisibility(View.VISIBLE);
            else binding.tvNoResultsFound.setVisibility(View.GONE);
        } else {
            adapter.setData(apkListViewModel.getSavedApkInfoList());
            mainActivity.onFilter(getFilterText());
            binding.tvNoResultsFound.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }
}