package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.drhowdydoo.appinfo.AppDetailsActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.AppListItemBinding;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.AppInfoDiffCallback;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.TimeFormatter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder> {


    private List<AppInfo> appInfoList;
    private Context context;
    private HashSet<Integer> flagSet = new HashSet<>();
    private ActivityResultLauncher<Intent> activityResultLauncher;


    public AppRecyclerViewAdapter(Context context, List<AppInfo> appInfoList, ActivityResultLauncher<Intent> activityResultLauncher) {
        this.appInfoList = new ArrayList<>(appInfoList);
        this.context = context;
        this.activityResultLauncher = activityResultLauncher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppListItemBinding appListItemBinding = AppListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(appListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AppInfo appInfo = appInfoList.get(position);
        holder.tvAppName.setText(appInfo.getAppName());
        holder.tvAppSize.setText(Formatter.formatShortFileSize(context, appInfo.getSize()));


        Glide.with(context)
                .load(appInfo.getAppIcon())
                .circleCrop()
                .error(R.drawable.round_pest_24)
                .into(holder.imgAppIcon);


        holder.tvAppStats.setVisibility(View.VISIBLE);
        holder.dotSeperator.setVisibility(View.VISIBLE);

        if (flagSet.contains(Constants.SHOW_LAST_USED_TIME)) {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getLastTimeUsed(), "Used"));
        } else if (flagSet.contains(Constants.HIDE_APP_STATS)) {
            holder.tvAppStats.setText(TimeFormatter.formatDuration(appInfo.getTotalForegroundTime()));
        } else if (flagSet.contains(Constants.SHOW_INSTALL_DATE)) {
            holder.tvAppStats.setText(String.format("Installed on %s", DateFormat.getDateInstance().format(appInfo.getInstallTmStamp())));
        } else {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getAppLastUpdateTime(), "Updated"));
        }

        String version = appInfo.getAppVersion();
        version = Character.isDigit(version.charAt(0)) ? "v" + version : version;
        holder.tvAppVersion.setText(version);

        if (appInfo.isSplitApp()) {
            holder.imgSplitApp.setVisibility(View.VISIBLE);
        } else {
            holder.imgSplitApp.setVisibility(View.GONE);
        }

        if (appInfo.isSystemApp()) {
            holder.tvSystemApp.setVisibility(View.VISIBLE);
        } else holder.tvSystemApp.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    public void setFlags(Integer... flags) {
        flagSet.clear();
        Collections.addAll(flagSet, flags);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<AppInfo> newAppInfoList) {
        appInfoList.clear();
        appInfoList.addAll(newAppInfoList);
        notifyDataSetChanged();
    }

    public void updateData(List<AppInfo> newAppInfoList) {
        AppInfoDiffCallback appInfoDiffCallback = new AppInfoDiffCallback(appInfoList, newAppInfoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(appInfoDiffCallback);
        appInfoList.clear();
        appInfoList.addAll(newAppInfoList);
        diffResult.dispatchUpdatesTo(this);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imgAppIcon, imgSplitApp;
        public TextView tvAppName, tvAppSize, tvAppStats, tvAppVersion, tvSystemApp, dotSeperator;

        public ViewHolder(@NonNull AppListItemBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            imgAppIcon = binding.imgAppIcon;
            tvAppName = binding.tvAppName;
            tvAppSize = binding.tvAppSize;
            tvAppStats = binding.tvAppStats;
            tvAppVersion = binding.tvAppVersion;
            imgSplitApp = binding.imgSplitApp;
            tvSystemApp = binding.tvSystemApp;
            dotSeperator = binding.dotSeperator;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, AppDetailsActivity.class);
            AppInfo appInfo = appInfoList.get(getBindingAdapterPosition());
            intent.putExtra("isApk", false);
            intent.putExtra("appName", appInfo.getAppName());
            intent.putExtra("appVersion", appInfo.getAppVersion());
            intent.putExtra("appSize", appInfo.getSize());
            intent.putExtra("isSplitApp", appInfo.isSplitApp());
            intent.putExtra("packageName", appInfo.getAppInfo().packageName);
            final View transitionView = imgAppIcon;
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation((Activity) context, transitionView, "transitionAppIcon");
            activityResultLauncher.launch(intent, options);
        }
    }

}
