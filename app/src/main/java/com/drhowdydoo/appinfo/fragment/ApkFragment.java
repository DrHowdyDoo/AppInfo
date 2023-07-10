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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drhowdydoo.appinfo.MainActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.adapter.ApkRecyclerViewAdapter;
import com.drhowdydoo.appinfo.databinding.FragmentApkBinding;
import com.drhowdydoo.appinfo.interfaces.AdapterListener;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.ApkInfoComparator;
import com.drhowdydoo.appinfo.util.ApkInfoManager;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.Utilities;
import com.drhowdydoo.appinfo.viewmodel.ApkListViewModel;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ApkFragment extends Fragment implements View.OnClickListener,AdapterListener {

    private FragmentApkBinding binding;
    private ApkRecyclerViewAdapter adapter;
    private ApkListViewModel apkListViewModel;
    private ApkInfoManager apkInfoManager;
    private MainActivity mainActivity;
    private boolean isPaused = false;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private SharedPreferences preferences;
    private int scanMode;

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
        adapter = new ApkRecyclerViewAdapter(requireActivity(), apkListViewModel.getSavedApkInfoList(), this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(adapter);
        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();
        mainActivity = (MainActivity) requireActivity();

        apkInfoManager = new ApkInfoManager(requireActivity());

        preferences = requireContext().getSharedPreferences("com.drhowdydoo.appinfo.preferences", Context.MODE_PRIVATE);
        scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode", 2);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            apkInfoManager.getAllApks(getApkSearchDir(scanMode), this);
        });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        getAllApks();
                    }
                });


        return binding.getRoot();
    }

    private File getApkSearchDir(int scanMode) {
        if (scanMode == 0) {
            return new File(Environment.getExternalStorageDirectory(), "Download");
        } else return Environment.getExternalStorageDirectory();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        getAllApks();
    }

    private void getAllApks() {

        scanMode = preferences.getInt("com.drhowdydoo.appinfo.scan-mode", 2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())
            return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            return;

        binding.groupStoragePermission.setVisibility(View.GONE);
        if (apkListViewModel.getSavedApkInfoList().isEmpty() && Utilities.shouldSearchApks) {
            Utilities.shouldSearchApks = false;
            binding.progressGroup.setVisibility(View.VISIBLE);
            binding.notFound.setVisibility(View.GONE);
            apkInfoManager.getAllApks(getApkSearchDir(scanMode), this);
        }
        mainActivity.onFilter(getFilterText());
        mainActivity.onSort(getSortText());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                binding.groupStoragePermission.setVisibility(View.VISIBLE);
            } else binding.groupStoragePermission.setVisibility(View.GONE);
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            binding.tvStoragePermission.setText("App needs storage access\nto search for all apks");
            binding.groupStoragePermission.setVisibility(View.VISIBLE);
        } else binding.groupStoragePermission.setVisibility(View.GONE);
    }


    public void setData(List<ApkInfo> apkInfoList, boolean dispatch) {
        apkListViewModel.setApkInfoList(apkInfoList);
        if (dispatch) dispatchData();
    }

    private void dispatchData() {
        binding.progressGroup.setVisibility(View.GONE);
        List<ApkInfo> filteredList = getFilteredList(apkListViewModel.getFilterState());
        filteredList.sort(new ApkInfoComparator(apkListViewModel.getSortState()));
        apkListViewModel.setSavedApkInfoList(filteredList);
        binding.notFound.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
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
                    .filter(apkInfo -> searchIn(input.toLowerCase(), apkInfo.getApkName().toLowerCase()))
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

    @Override
    public void onItemLongClicked() {
       mainActivity.showContextualBar();
    }

    @Override
    public void removeContextualBar() {
        mainActivity.removeContextualBar();
    }

    @Override
    public void setContextualBarTitle(int count) {
        mainActivity.setContextualBarTitle(count);
    }

    @Override
    public boolean isContextualBarShown() {
        return mainActivity.isContextualBarShown();
    }

    public void contextualBarRemoved() {
        if (adapter != null) adapter.contextualBarRemoved();
    }

    public List<String> getSelectedApkPaths() {
        return adapter.getSelectedApkPaths();
    }

    public void updateList() {
        apkListViewModel.getSavedApkInfoList().removeIf(ApkInfo::isSelected);
        adapter.removeDeletedApksFromList();
    }

    public void selectAllApk() {
        adapter.selectAllApk();
    }

    public void deselectAllApk(){
        adapter.deselectAllApk();
    }

    @Override
    public void allItemSelected() {
        mainActivity.allItemSelected();
    }
}