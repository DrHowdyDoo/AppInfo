package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.ApkListItemBinding;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ApkRecyclerViewAdapter extends RecyclerView.Adapter<ApkRecyclerViewAdapter.ViewHolder>{

    private Context context;
    private List<ApkInfo> apkInfoList;
    private HashSet<Integer> flagSet = new HashSet<>();

    public ApkRecyclerViewAdapter(Context context, List<ApkInfo> apkInfoList) {
        this.context = context;
        this.apkInfoList = new ArrayList<>(apkInfoList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ApkListItemBinding apkListItemBinding = ApkListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ApkRecyclerViewAdapter.ViewHolder(apkListItemBinding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ApkInfo apkInfo = apkInfoList.get(position);
        holder.tvApkName.setText(apkInfo.getApkName());
        holder.tvApkSize.setText(Formatter.formatShortFileSize(context, apkInfo.getApkSize()));
        holder.tvApkVersion.setText("v" + apkInfo.getApkVersion());
        holder.tvApkPath.setText(apkInfo.getApkPath());
        String apkStatus = apkInfo.isInstalled() ? "installed" : "not installed";
        holder.tvApkStatus.setText(apkStatus);

        if (flagSet.contains(Constants.SHOW_SQUARE_APP_ICON)) {
            Glide.with(context)
                    .load(apkInfo
                            .getApkIcon())
                    .transform(new RoundedCorners(32))
                    .error(R.drawable.round_pest_24)
                    .into(holder.imgApkIcon);
        } else {
            Glide.with(context)
                    .load(apkInfo
                            .getApkIcon())
                    .circleCrop()
                    .error(R.drawable.round_pest_24)
                    .into(holder.imgApkIcon);
        }
    }

    @Override
    public int getItemCount() {
        return apkInfoList.size();
    }

    public void setFlags(Integer... flags) {
        flagSet.clear();
        Collections.addAll(flagSet, flags);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ApkInfo> newApkInfoList) {
        apkInfoList.clear();
        apkInfoList.addAll(newApkInfoList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgApkIcon;
        public TextView tvApkName, tvApkSize, tvApkVersion, tvApkPath, tvApkStatus;
        public ViewHolder(@NonNull ApkListItemBinding binding) {
            super(binding.getRoot());
            imgApkIcon = binding.imgApkIcon;
            tvApkName = binding.tvApkName;
            tvApkSize = binding.tvApkSize;
            tvApkVersion = binding.tvApkVersion;
            tvApkPath = binding.tvApkPath;
            tvApkStatus = binding.tvApkStatus;
        }
    }

}
