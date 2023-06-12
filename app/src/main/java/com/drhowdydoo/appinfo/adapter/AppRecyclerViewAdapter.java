package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.AppListItemBinding;
import com.drhowdydoo.appinfo.model.AppInfo;
import com.drhowdydoo.appinfo.util.AppInfoDiffCallback;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.TimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder> {

    private List<AppInfo> appInfoList;
    private Context context;
    private HashSet<Integer> flagSet = new HashSet<>();


    public AppRecyclerViewAdapter(Context context, List<AppInfo> appInfoList) {
        this.appInfoList = new ArrayList<>(appInfoList);
        this.context = context;
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

        if (flagSet.contains(Constants.SHOW_SQUARE_APP_ICON)) {
            Glide.with(context)
                    .load(appInfo
                            .getAppIcon())
                    .transform(new RoundedCorners(32))
                    .error(R.drawable.round_pest_24)
                    .into(holder.imgAppIcon);
        } else {
            Glide.with(context)
                    .load(appInfo
                            .getAppIcon())
                    .circleCrop()
                    .error(R.drawable.round_pest_24)
                    .into(holder.imgAppIcon);
        }

        holder.tvAppStats.setVisibility(View.VISIBLE);
        holder.dotSeperator.setVisibility(View.VISIBLE);

        if (flagSet.contains(Constants.SHOW_LAST_USED_TIME)) {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getLastTimeUsed(), "Used"));
        } else if (flagSet.contains(Constants.HIDE_APP_STATS)) {
            holder.tvAppStats.setText(TimeFormatter.formatDuration(appInfo.getTotalForegroundTime()));
        } else {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getAppLastUpdateTime(), "Updated"));
        }

        holder.tvAppVersion.setText("v" + appInfo.getAppVersion());

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

    public void updateData(List<AppInfo> newAppInfoList){
        AppInfoDiffCallback appInfoDiffCallback = new AppInfoDiffCallback(appInfoList, newAppInfoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(appInfoDiffCallback);
        appInfoList.clear();
        appInfoList.addAll(newAppInfoList);
        diffResult.dispatchUpdatesTo(this);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAppIcon, imgSplitApp;
        public TextView tvAppName, tvAppSize, tvAppStats, tvAppVersion, tvSystemApp, dotSeperator;

        public ViewHolder(@NonNull AppListItemBinding binding) {
            super(binding.getRoot());
            imgAppIcon = binding.imgAppIcon;
            tvAppName = binding.tvAppName;
            tvAppSize = binding.tvAppSize;
            tvAppStats = binding.tvAppStats;
            tvAppVersion = binding.tvAppVersion;
            imgSplitApp = binding.imgSplitApp;
            tvSystemApp = binding.tvSystemApp;
            dotSeperator = binding.dotSeperator;
        }
    }

}
