package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drhowdydoo.appinfo.AppDetailsActivity;
import com.drhowdydoo.appinfo.databinding.AppDeatilsListBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.PermissionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldMayBeFinal")
public class AppDetailsListAdapter extends RecyclerView.Adapter<AppDetailsListAdapter.ViewHolder> {

    private List<AppDetailItem> appDetails;
    private final Context context;
    private String appName;
    private String apkFilePath;

    public AppDetailsListAdapter(List<AppDetailItem> appDetails, Context context) {
        this.appDetails = appDetails;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppDeatilsListBinding binding = AppDeatilsListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppDetailItem appDetail = appDetails.get(position);
        holder.icon.setImageResource(appDetail.getIcon());
        holder.tvTitle.setText(appDetail.getTitle());
        holder.tvValue.setText(appDetail.getValue().getText());

        if (position == 2) {
            if (appDetail.getValue().isValueEmpty()) {
                holder.progressBar.setVisibility(View.VISIBLE);
            }else {
                holder.btnExtractFont.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.btnExtractFont.setOnClickListener(v -> extractFont(holder.btnExtractFont));
            }
        } else holder.btnExtractFont.setVisibility(View.GONE);

        if (appDetail.getValue().getText().equalsIgnoreCase("not found")) holder.btnExtractFont.setVisibility(View.GONE);

        int lineCount = appDetail.getValue().getCount();
        boolean isExpandable = lineCount > Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES;
        if (isExpandable) {
            if (appDetail.isExpanded()) {
                holder.tvExpandIndicator.setVisibility(View.GONE);
            } else {
                holder.tvExpandIndicator.setVisibility(View.VISIBLE);
                holder.tvValue.setMaxLines(Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES);
            }
        }

        holder.tvValue.setOnClickListener(v -> {
            boolean isExpanded = appDetail.isExpanded();
            if (!isExpandable) return;
            if (isExpanded) {
                holder.tvValue.setMaxLines(Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES);
                holder.tvExpandIndicator.setVisibility(View.VISIBLE);
                appDetail.setExpanded(false);
            } else {
                holder.tvValue.setMaxLines(Integer.MAX_VALUE);
                holder.tvExpandIndicator.setVisibility(View.GONE);
                appDetail.setExpanded(true);
            }
            notifyItemChanged(position);
        });

    }

    @SuppressLint("CheckResult")
    private void extractFont(View view){
        boolean haveStorageAccess = ((AppDetailsActivity)context).checkStoragePermission();
        if (!haveStorageAccess) return;
        view.setEnabled(false);
        Observable.fromCallable(() -> AppDetailsManager.extractFonts(apkFilePath, appName)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(path -> {
                    view.setEnabled(true);
                    if (path.isBlank()) Toast.makeText(context, "Something went wrong !", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(context, "Fonts extracted to " + path, Toast.LENGTH_SHORT).show();
                });
    }


    public void addApkDetails(String appName, String apkFilePath) {
        this.appName = appName;
        this.apkFilePath = apkFilePath;
    }

    @Override
    public int getItemCount() {
        return appDetails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView tvTitle, tvValue, tvExpandIndicator;
        public MaterialCardView container;
        public CircularProgressIndicator progressBar;
        public Button btnExtractFont;

        public ViewHolder(@NonNull AppDeatilsListBinding binding) {
            super(binding.getRoot());
            icon = binding.icon;
            tvTitle = binding.tvTitle;
            tvValue = binding.tvValue;
            tvExpandIndicator = binding.tvExpandIndicator;
            container = binding.listContainer;
            btnExtractFont = binding.btnExtractFont;
            progressBar = binding.progressBar;

        }
    }

}
