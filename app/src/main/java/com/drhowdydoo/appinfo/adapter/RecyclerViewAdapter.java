package com.drhowdydoo.appinfo.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.drhowdydoo.appinfo.util.AppInfoDiffCallback;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.util.TimeFormatter;
import com.drhowdydoo.appinfo.databinding.AppListItemBinding;
import com.drhowdydoo.appinfo.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<AppInfo> appInfoList;
    private Context context;
    private HashSet<Integer> flagSet = new HashSet<>();


    public RecyclerViewAdapter(Context context, List<AppInfo> appInfoList) {
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
        holder.tvAppSize.setText(Formatter.formatShortFileSize(context,appInfo.getSize()));

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

        if (flagSet.contains(Constants.SHOW_LAST_USED_TIME)) {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getLastTimeUsed(),"Used"));
        } else {
            holder.tvAppStats.setText(TimeFormatter.format(appInfo.getAppLastUpdateTime(),"Updated"));
        }


    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }


    public void setFlags(Integer... flags){
        flagSet.clear();
        Collections.addAll(flagSet,flags);
    }

    public void setData(List<AppInfo> newAppInfoList){
        AppInfoDiffCallback appInfoDiffCallback = new AppInfoDiffCallback(appInfoList,newAppInfoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(appInfoDiffCallback);
        appInfoList.clear();
        appInfoList.addAll(newAppInfoList);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAppIcon;
        public TextView tvAppName,tvAppSize,tvAppStats;
            public ViewHolder(@NonNull AppListItemBinding binding) {
                super(binding.getRoot());
                imgAppIcon = binding.imgAppIcon;
                tvAppName = binding.tvAppName;
                tvAppSize = binding.tvAppSize;
                tvAppStats = binding.tvAppStats;
            }
        }

}
