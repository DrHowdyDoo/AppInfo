package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.drhowdydoo.appinfo.AppDetailsActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.databinding.ApkListItemBinding;
import com.drhowdydoo.appinfo.interfaces.AdapterListener;
import com.drhowdydoo.appinfo.model.ApkInfo;
import com.drhowdydoo.appinfo.util.ApkInfoDiffCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
public class ApkRecyclerViewAdapter extends RecyclerView.Adapter<ApkRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<ApkInfo> apkInfoList;
    private HashSet<Integer> flagSet = new HashSet<>();
    private AdapterListener adapterListener;
    private int selectedItemCount = 0;

    public ApkRecyclerViewAdapter(Context context, List<ApkInfo> apkInfoList,
                                  AdapterListener adapterListener) {
        this.context = context;
        this.apkInfoList = new ArrayList<>(apkInfoList);
        this.adapterListener = adapterListener;
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

        if (adapterListener.isContextualBarShown()) {
            holder.checkbox.setVisibility(apkInfo.isSelected() ? View.VISIBLE : View.GONE);
            holder.parent.setActivated(apkInfo.isSelected());
        } else {
            holder.checkbox.setVisibility(View.GONE);
            holder.parent.setActivated(false);
        }

        holder.tvApkName.setText(apkInfo.getApkName());
        holder.tvApkSize.setText(Formatter.formatShortFileSize(context, apkInfo.getApkSize()));
        String apkVersion = apkInfo.getApkVersion();
        if (apkVersion.isBlank()) {
            holder.tvApkVersion.setText("");
        } else {
            holder.tvApkVersion.setText("v" + apkVersion);
        }
        holder.tvApkPath.setText(apkInfo.getApkPath());
        holder.tvApkPath.setSelected(true);
        String apkStatus = apkInfo.isInstalled() ? "installed" : "not installed";
        holder.tvApkStatus.setText(apkStatus);

        Glide.with(context)
                .load(apkInfo.getApkIcon())
                .circleCrop()
                .error(R.drawable.round_pest_24)
                .into(holder.imgApkIcon);
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

    public void updateData(List<ApkInfo> newApkInfoList) {
        ApkInfoDiffCallback apkInfoDiffCallback = new ApkInfoDiffCallback(apkInfoList, newApkInfoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(apkInfoDiffCallback);
        apkInfoList.clear();
        apkInfoList.addAll(newApkInfoList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        public ImageView imgApkIcon;
        public TextView tvApkName, tvApkSize, tvApkVersion, tvApkPath, tvApkStatus;
        public ImageView checkbox;
        public ConstraintLayout parent;

        public ViewHolder(@NonNull ApkListItemBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
            imgApkIcon = binding.imgApkIcon;
            tvApkName = binding.tvApkName;
            tvApkSize = binding.tvApkSize;
            tvApkVersion = binding.tvApkVersion;
            tvApkPath = binding.tvApkPath;
            tvApkStatus = binding.tvApkStatus;
            checkbox = binding.checkbox;
            parent = binding.getRoot();
        }

        @Override
        public void onClick(View v) {
            if (adapterListener.isContextualBarShown()) {
                int position = getBindingAdapterPosition();
                ApkInfo apkInfo = apkInfoList.get(position);
                apkInfo.setSelected(!apkInfo.isSelected());
                selectedItemCount = apkInfo.isSelected() ? selectedItemCount + 1 : selectedItemCount - 1;
                adapterListener.setContextualBarTitle(selectedItemCount);
                notifyItemChanged(position);
                if (selectedItemCount == 0) adapterListener.removeContextualBar();
            }
            else {
                openApkInfo();
            }

        }

        private void openApkInfo() {
            Intent intent = new Intent(context, AppDetailsActivity.class);
            ApkInfo apkInfo = apkInfoList.get(getBindingAdapterPosition());
            String packageName = apkInfo.getApkInfo() != null ? apkInfo.getApkInfo().packageName : "";
            intent.putExtra("isApk", true);
            intent.putExtra("appName", apkInfo.getApkName());
            intent.putExtra("appVersion", apkInfo.getApkVersion());
            intent.putExtra("appSize", apkInfo.getApkSize());
            intent.putExtra("apkAbsolutePath", apkInfo.getApkAbsolutePath());
            intent.putExtra("isInstalled", apkInfo.isInstalled());
            intent.putExtra("packageName", packageName);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            if (adapterListener.isContextualBarShown()) return false;
            adapterListener.onItemLongClicked();
            int position = getBindingAdapterPosition();
            apkInfoList.get(position).setSelected(true);
            checkbox.setVisibility(View.VISIBLE);
            parent.setActivated(true);
            selectedItemCount = selectedItemCount + 1;
            adapterListener.setContextualBarTitle(selectedItemCount);
            return true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void contextualBarRemoved() {
        selectedItemCount = 0;
        apkInfoList.forEach(apkInfo -> apkInfo.setSelected(false));
        notifyDataSetChanged();
    }

    public List<String> getSelectedApkPaths() {
        return apkInfoList.stream().filter(ApkInfo::isSelected).map(ApkInfo::getApkAbsolutePath).collect(Collectors.toList());
    }

    public void removeDeletedApksFromList() {
        apkInfoList.removeIf(ApkInfo::isSelected);
        notifyDataSetChanged();
    }

}
