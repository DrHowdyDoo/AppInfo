package com.drhowdydoo.appinfo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeScroll;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.drhowdydoo.appinfo.AppDetailsActivity;
import com.drhowdydoo.appinfo.R;
import com.drhowdydoo.appinfo.bottomsheet.ShareBottomSheet;
import com.drhowdydoo.appinfo.databinding.AppDeatilsListBinding;
import com.drhowdydoo.appinfo.databinding.AppDetailsGridLayoutBinding;
import com.drhowdydoo.appinfo.model.AppDetailItem;
import com.drhowdydoo.appinfo.model.AppMetadata;
import com.drhowdydoo.appinfo.util.ApkExtractor;
import com.drhowdydoo.appinfo.util.AppDetailsManager;
import com.drhowdydoo.appinfo.util.Constants;
import com.drhowdydoo.appinfo.util.Utilities;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("FieldMayBeFinal")
public class AppDetailsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Object> appDetails;
    private String appName;
    private String apkFilePath, apkAbsolutePath;
    private boolean isInstalled, isApk, isSplitApp;
    private PackageInfo packageInfo;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public AppDetailsListAdapter(List<Object> appDetails, Context context, ActivityResultLauncher<Intent> activityResultLauncher) {
        this.appDetails = appDetails;
        this.context = context;
        this.activityResultLauncher = activityResultLauncher;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new HeaderViewHolder(AppDetailsGridLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else
            return new ItemViewHolder(AppDeatilsListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 0;
        else return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        if (viewType == 0) {
            setHeader((HeaderViewHolder) holder, position);
        } else {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            AppDetailItem appDetail = (AppDetailItem) appDetails.get(position);
            itemViewHolder.icon.setImageResource(appDetail.getIcon());
            itemViewHolder.tvTitle.setText(appDetail.getTitle());
            itemViewHolder.tvValue.setText(appDetail.getValue().getText());

            if (position == 3) {
                if (appDetail.getValue().isValueEmpty()) {
                    itemViewHolder.progressBar.setVisibility(View.VISIBLE);
                } else {
                    if (appDetail.getValue().getText().equalsIgnoreCase("not found")) {
                        itemViewHolder.fontBtnContainer.setVisibility(View.GONE);
                    } else {
                        itemViewHolder.fontBtnContainer.setVisibility(View.VISIBLE);
                        itemViewHolder.btnExtractFont.setVisibility(View.VISIBLE);
                        itemViewHolder.btnExtractFont.setOnClickListener(v -> extractFont(itemViewHolder));
                    }
                    itemViewHolder.progressBar.setVisibility(View.GONE);
                }
            } else {
                itemViewHolder.fontBtnContainer.setVisibility(View.GONE);
                itemViewHolder.btnExtractFont.setVisibility(View.GONE);
                itemViewHolder.progressBar.setVisibility(View.GONE);
            }

            if (appDetail.getValue().getText().equalsIgnoreCase("not found"))
                itemViewHolder.btnExtractFont.setVisibility(View.GONE);

            int lineCount = appDetail.getValue().getCount();
            boolean isExpandable = lineCount > Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES;
            if (isExpandable) {
                if (appDetail.isExpanded()) {
                    itemViewHolder.tvExpandIndicator.setVisibility(View.GONE);
                } else {
                    itemViewHolder.tvExpandIndicator.setVisibility(View.VISIBLE);
                    itemViewHolder.btnExpandView.setVisibility(View.VISIBLE);
                    itemViewHolder.tvValue.setMaxLines(Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES);
                }
            } else {
                itemViewHolder.tvExpandIndicator.setVisibility(View.GONE);
                itemViewHolder.btnExpandView.setVisibility(View.GONE);
            }

            itemViewHolder.btnExpandView.setOnClickListener(v -> {
                boolean isExpanded = appDetail.isExpanded();
                if (!isExpandable) return;
                if (isExpanded) {
                    itemViewHolder.btnExpandView.setIcon(ContextCompat.getDrawable(context, R.drawable.round_expand_more_24));
                    itemViewHolder.tvValue.setMaxLines(Constants.EXPENDABLE_TEXT_VIEW_MAX_LINES);
                    itemViewHolder.tvExpandIndicator.setVisibility(View.VISIBLE);
                    appDetail.setExpanded(false);
                } else {
                    itemViewHolder.btnExpandView.setIcon(ContextCompat.getDrawable(context, R.drawable.round_expand_less_24));
                    itemViewHolder.tvValue.setMaxLines(Integer.MAX_VALUE);
                    itemViewHolder.tvExpandIndicator.setVisibility(View.GONE);
                    appDetail.setExpanded(true);
                }
                TransitionManager.beginDelayedTransition(itemViewHolder.horizontalScrollView, new AutoTransition());
                notifyItemChanged(position);
            });
        }

    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    private void setHeader(HeaderViewHolder holder, int position) {
        AppMetadata appMetadata = (AppMetadata) appDetails.get(position);
        holder.gridBinding.tvCategoryValue.setText(appMetadata.getCategory());
        holder.gridBinding.tvMinSdkValue.setText(appMetadata.getMinSdk());
        holder.gridBinding.tvTargetSdkValue.setText(appMetadata.getTargetSdk());
        holder.gridBinding.tvSourceValue.setText(appMetadata.getSource());
        holder.gridBinding.tvInstalledDtValue.setText(appMetadata.getInstallDt());
        holder.gridBinding.tvUpdatedDtValue.setText(appMetadata.getUpdatedDt());
        holder.gridBinding.tvPackageNameValue.setText(appMetadata.getPackageName());
        holder.gridBinding.tvMainClassValue.setText(appMetadata.getMainClass());
        holder.gridBinding.tvThemeValue.setText(appMetadata.getTheme());
        holder.gridBinding.tvColorValue.setText(appMetadata.getColors());

        if (!isInstalled) holder.gridBinding.btnInfo.setEnabled(false);
        if (isApk) holder.gridBinding.splitBtn.setEnabled(false);
        setUpClickListeners(holder);
    }

    private void setUpClickListeners(HeaderViewHolder holder) {
        //Log.d(TAG, "setUpClickListeners()");
        AppDetailsActivity activity = (AppDetailsActivity) context;
        holder.gridBinding.btnInfo.setOnClickListener(v -> activity.openSystemInfo(packageInfo.packageName));
        holder.gridBinding.btnPlayStore.setOnClickListener(v -> activity.openInPlayStore(packageInfo.packageName));
        holder.gridBinding.btnShare.setOnClickListener(v -> {
            ShareBottomSheet shareBottomSheet = new ShareBottomSheet();
            Bundle args = new Bundle();
            args.putString("packageName", packageInfo.packageName);
            args.putString("appName", appName);
            args.putString("apkPath", isApk ? apkAbsolutePath : apkFilePath);
            args.putBoolean("isSplitApp", isSplitApp);
            args.putBoolean("isApk", isApk);
            shareBottomSheet.setArguments(args);
            shareBottomSheet.show(activity.getSupportFragmentManager(), "shareBottomSheet");
        });
        holder.gridBinding.btnExtractApk.setOnClickListener(v -> {
            boolean haveStorageAccess = activity.checkStoragePermission();
            if (!haveStorageAccess) return;
            if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(context, "Storage not accessible", Toast.LENGTH_SHORT).show();
                return;
            }

            TransitionManager.beginDelayedTransition(holder.gridBinding.getRoot(), new ChangeBounds());
            holder.gridBinding.splitBtn.setVisibility(View.INVISIBLE);
            holder.gridBinding.extractApkLoadingIndicator.setVisibility(View.VISIBLE);
            Utilities.shouldSearchApks = true;
            activity.hideProgressBar(false);
            Observable.fromAction(() -> ApkExtractor.extractApk(appName, packageInfo))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> {
                        holder.gridBinding.splitBtn.setVisibility(View.VISIBLE);
                        holder.gridBinding.extractApkLoadingIndicator.setVisibility(View.INVISIBLE);
                        activity.hideProgressBar(true);
                        Toast.makeText(context, "APK files extracted to AppInfo folder in the root directory", Toast.LENGTH_SHORT).show();
                    })
                    .subscribe();
        });

        holder.gridBinding.expandMoreOrLessFilled.setOnClickListener(v -> {
            int visibility = holder.gridBinding.btnUninstallApp.getVisibility();
            if (visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(holder.gridBinding.containerSplitBtnOptions, new Slide(Gravity.TOP));
                holder.gridBinding.btnUninstallApp.setVisibility(View.INVISIBLE);
            } else {
                TransitionManager.beginDelayedTransition(holder.gridBinding.containerSplitBtnOptions, new Slide(Gravity.TOP));
                holder.gridBinding.btnUninstallApp.setVisibility(View.VISIBLE);
            }
        });

        holder.gridBinding.btnUninstallApp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + packageInfo.packageName));
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            activityResultLauncher.launch(intent);
        });
    }

    @SuppressLint("CheckResult")
    private void extractFont(ItemViewHolder holder) {
        boolean haveStorageAccess = ((AppDetailsActivity) context).checkStoragePermission();
        if (!haveStorageAccess) return;
        holder.btnExtractFont.setEnabled(false);
        holder.fontExtractionIndicator.setVisibility(View.VISIBLE);
        holder.fileIcon.setVisibility(View.VISIBLE);
        Observable.fromCallable(() -> AppDetailsManager.extractFonts(apkFilePath, appName)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(path -> {
                    holder.btnExtractFont.setEnabled(true);
                    holder.fontExtractionIndicator.setVisibility(View.GONE);
                    holder.fileIcon.setVisibility(View.GONE);
                    if (path.isBlank())
                        Toast.makeText(context, "Something went wrong !", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, "Fonts extracted to " + path, Toast.LENGTH_SHORT).show();
                });
    }


    public void addApkDetails(String appName, String apkFilePath, boolean isInstalled, boolean isApk, boolean isSplitApp, String apkAbsolutePath) {
        this.appName = appName;
        this.apkFilePath = apkFilePath;
        this.isApk = isApk;
        this.isInstalled = isInstalled;
        this.isSplitApp = isSplitApp;
        this.apkAbsolutePath = apkAbsolutePath;
    }

    @Override
    public int getItemCount() {
        return appDetails.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon, fileIcon;
        public TextView tvTitle, tvValue, tvExpandIndicator;
        public MaterialCardView container;
        public HorizontalScrollView horizontalScrollView;
        public LoadingIndicator progressBar;
        public LinearProgressIndicator fontExtractionIndicator;
        public Button btnExtractFont;
        public MaterialButton btnExpandView;
        public LinearLayout fontBtnContainer;

        public ItemViewHolder(@NonNull AppDeatilsListBinding binding) {
            super(binding.getRoot());
            icon = binding.icon;
            tvTitle = binding.tvTitle;
            tvValue = binding.tvValue;
            tvExpandIndicator = binding.tvExpandIndicator;
            container = binding.listContainer;
            horizontalScrollView = binding.horizontalScrollView;
            btnExtractFont = binding.btnExtractFont;
            progressBar = binding.progressBar;
            fontExtractionIndicator = binding.pbFontExtraction;
            fileIcon = binding.imgFontFile;
            fontBtnContainer = binding.fontBtnContainer;
            btnExpandView = binding.btnExpandView;
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public AppDetailsGridLayoutBinding gridBinding;

        public HeaderViewHolder(@NonNull AppDetailsGridLayoutBinding binding) {
            super(binding.getRoot());
            gridBinding = binding;
        }
    }

}
