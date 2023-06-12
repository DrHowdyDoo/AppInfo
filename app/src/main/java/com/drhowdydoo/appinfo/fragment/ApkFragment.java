package com.drhowdydoo.appinfo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.drhowdydoo.appinfo.MainActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.adapter.ApkRecyclerViewAdapter;
import com.drhowdydoo.appinfo.databinding.FragmentApkBinding;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.ApkInfoManager;
import com.drhowdydoo.appinfo.util.AppInfoComparator;
import com.drhowdydoo.appinfo.viewmodel.ApkListViewModel;

import java.util.List;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ApkFragment extends Fragment implements View.OnClickListener{

    private FragmentApkBinding binding;
    private ApkRecyclerViewAdapter adapter;

    private ApkListViewModel apkListViewModel;
    private ApkInfoManager apkInfoManager;

    private MainActivity mainActivity;

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
        adapter = new ApkRecyclerViewAdapter(requireActivity(),apkListViewModel.getApkInfoList());
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

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                binding.groupStoragePermission.setVisibility(View.GONE);
                if (apkListViewModel.getApkInfoList().isEmpty()) {
                    binding.progressGroup.setVisibility(View.VISIBLE);
                    apkInfoManager.getAllApks(Environment.getExternalStorageDirectory(), this);
                }
            }
        }
        mainActivity.onFilter("All Apks (" + adapter.getItemCount() + ")");
        mainActivity.onSort("Size");

    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                binding.groupStoragePermission.setVisibility(View.VISIBLE);
            } else binding.groupStoragePermission.setVisibility(View.GONE);
        }
    }

    public void setData(List<ApkInfo> apkInfoList, boolean dispatch) {
        apkListViewModel.setApkInfoList(apkInfoList);
        if (dispatch) dispatchData();
    }

    private void dispatchData() {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.progressGroup.setVisibility(View.GONE);
        adapter.setData(apkListViewModel.getApkInfoList());
        mainActivity.onFilter("All Apks (" + adapter.getItemCount() + ")");
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnStorageAccess && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + "com.drhowdydoo.appinfo"));
            startActivity(intent);
        }
    }
}