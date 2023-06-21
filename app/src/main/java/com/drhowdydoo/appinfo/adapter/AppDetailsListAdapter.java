package com.drhowdydoo.appinfo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drhowdydoo.appinfo.databinding.AppDeatilsListBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.util.Constants;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class AppDetailsListAdapter extends RecyclerView.Adapter<AppDetailsListAdapter.ViewHolder> {

    private List<AppDetailItem> appDetails;

    public AppDetailsListAdapter(List<AppDetailItem> appDetails) {
        this.appDetails = appDetails;
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

        if (position == 2) holder.btnExtractFont.setVisibility(View.VISIBLE);
        else holder.btnExtractFont.setVisibility(View.GONE);

        if (appDetail.getValue().isValueEmpty()) holder.btnExtractFont.setVisibility(View.GONE);

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


    @Override
    public int getItemCount() {
        return appDetails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView tvTitle, tvValue, tvExpandIndicator;
        public MaterialCardView container;

        public Button btnExtractFont;

        public ViewHolder(@NonNull AppDeatilsListBinding binding) {
            super(binding.getRoot());
            icon = binding.icon;
            tvTitle = binding.tvTitle;
            tvValue = binding.tvValue;
            tvExpandIndicator = binding.tvExpandIndicator;
            container = binding.listContainer;
            btnExtractFont = binding.btnExtractFont;

        }
    }

}
