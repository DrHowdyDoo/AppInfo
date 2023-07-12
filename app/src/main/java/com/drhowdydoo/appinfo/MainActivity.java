package com.drhowdydoo.appinfo;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.drhowdydoo.appinfo.adapter.ViewPagerAdapter;
import com.drhowdydoo.appinfo.bottomsheet.FilterBottomSheet;
import com.drhowdydoo.appinfo.bottomsheet.SortBottomSheet;
import com.drhowdydoo.appinfo.databinding.ActivityMainBinding;
import com.drhowdydoo.appinfo.databinding.DeleteDialogLayoutBinding;
import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.interfaces.OnSortFilterListener;
import com.drhowdydoo.appinfo.util.ThemeUtils;
import com.drhowdydoo.appinfo.viewmodel.MainViewModel;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnSortFilterListener {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private boolean isPageSelected = false;
    private ActionMode actionMode;
    private boolean isAllItemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("com.drhowdydoo.appinfo.preferences", MODE_PRIVATE);
        int themeMode = preferences.getInt("com.drhowdydoo.appinfo.theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        ThemeUtils.applyTheme(this, themeMode);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        binding.viewPager.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        pagerAdapter.addFragment(new AppFragment());
        pagerAdapter.addFragment(new ApkFragment());
        binding.viewPager.setAdapter(pagerAdapter);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if (mainViewModel.isSearchVisible()) {
            binding.searchBar.setVisibility(View.VISIBLE);
        }

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                isPageSelected = true;
                binding.bottomNavigation.getMenu().getItem(position).setChecked(true);
                onFragmentChanged(position);
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int position = item.getItemId() == R.id.app ? 0 : 1;
            binding.viewPager.setCurrentItem(position);
            onFragmentChanged(position);
            return true;
        });


        binding.materialToolBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.search) {
                if (binding.searchBar.isShown()) {
                    binding.searchBar.setVisibility(View.GONE);
                    mainViewModel.setSearchVisible(false);
                } else {
                    binding.searchBar.setVisibility(View.VISIBLE);
                    mainViewModel.setSearchVisible(true);
                    setSearchHint();
                    binding.searchInput.requestFocus();
                    imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            } else if (item.getItemId() == R.id.settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            return false;
        });

        binding.btnFilter.setOnClickListener(v -> {
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet(getCurrentFragment());
            filterBottomSheet.show(getSupportFragmentManager(), "filterBottomSheet");
        });

        binding.btnSort.setOnClickListener(v -> {
            SortBottomSheet sortBottomSheet = new SortBottomSheet(getCurrentFragment());
            sortBottomSheet.show(getSupportFragmentManager(), "sortBottomSheet");
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isPageSelected)
                    return;                       // To prevent execution before the viewpager2's onPageSelected is called to avoid wrong value on device rotation
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof AppFragment) {
                    mainViewModel.setAppSearchText(s.toString());
                    ((AppFragment) fragment).search(s.toString());
                } else if (fragment instanceof ApkFragment) {
                    mainViewModel.setApkSearchText(s.toString());
                    ((ApkFragment) fragment).search(s.toString());
                }
            }
        });
    }


    /**
     * Changes the search bar hint with respective to the fragments.
     */
    private void setSearchHint() {
        if (binding.viewPager.getCurrentItem() == 0) binding.searchBar.setHint("Search apps by name or package");
        else binding.searchBar.setHint("Search apks by name or package");
    }


    /**
     * To close keyboard when the user touches anywhere on the screen.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onSort(String text) {
        binding.btnSort.setText(text);
    }

    @Override
    public void onFilter(String text) {
        binding.btnFilter.setText(text);
    }

    private Fragment getCurrentFragment() {
        if (binding.viewPager.getAdapter() == null || binding.viewPager.getAdapter().getItemCount() == 0)
            return null;
        int current = binding.viewPager.getCurrentItem();
        if (current == 0) return getSupportFragmentManager().findFragmentByTag("f0");
        else return getSupportFragmentManager().findFragmentByTag("f1");
    }

    private void onFragmentChanged(int position) {
        setSearchHint();
        if (position == 0) {
            binding.searchInput.setText(mainViewModel.getAppSearchText());
            if (actionMode != null) actionMode.finish();
        } else if (position == 1) {
            binding.searchInput.setText(mainViewModel.getApkSearchText());
        }
    }

    public void showContextualBar() {
        ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);
                menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (isAllItemSelected) menu.getItem(2).setTitle("Deselect all");
                else menu.getItem(2).setTitle("Select all");
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                ApkFragment apkFragment = (ApkFragment) getSupportFragmentManager().findFragmentByTag("f1");
                List<String> paths;
                if (apkFragment != null) {
                    paths = apkFragment.getSelectedApkPaths();
                } else {
                    paths = new ArrayList<>();
                }


                if (item.getItemId() == R.id.delete) {

                    DeleteDialogLayoutBinding deleteDialogLayoutBinding = DeleteDialogLayoutBinding.inflate(LayoutInflater.from(MainActivity.this));
                    AlertDialog alertDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                            .setView(deleteDialogLayoutBinding.getRoot())
                            .create();
                    alertDialog.show();

                    deleteDialogLayoutBinding.tvTitle.setText(paths.size() == 1 ? "Delete this apk ?" : "Delete " + "these " + paths.size() + " apks ?");
                    deleteDialogLayoutBinding.btnDelete.setOnClickListener(v -> {
                        if (apkFragment != null) apkFragment.updateList();
                        paths.forEach(apkPath -> {
                            File file = new File(apkPath);
                            if (file.exists()) file.delete();
                        });
                        mode.finish();
                        alertDialog.dismiss();
                        Snackbar.make(MainActivity.this, binding.getRoot(), "Deleted " + paths.size() + " apk(s)", BaseTransientBottomBar.LENGTH_SHORT).show();
                    });

                    deleteDialogLayoutBinding.btnCancel.setOnClickListener(v -> {
                        alertDialog.dismiss();
                        mode.finish();
                    });

                    return true;
                } else if (item.getItemId() == R.id.share) {
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("application/vnd.android.package-archive");
                    ArrayList<Uri> uriList = new ArrayList<>();
                    paths.forEach(path -> {
                        Uri uri = FileProvider.getUriForFile(
                                MainActivity.this,
                                "com.drhowdydoo.appinfo.fileprovider", new File(path));
                        uriList.add(uri);
                    });
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share apk(s) via"));
                    mode.finish();
                    return true;
                } else if (item.getItemId() == R.id.selection) {
                    if (apkFragment != null) {
                        if (isAllItemSelected) {
                            apkFragment.deselectAllApk();
                            isAllItemSelected = false;
                            mode.finish();
                            return true;
                        }
                        apkFragment.selectAllApk();
                        isAllItemSelected = true;
                    }
                    mode.invalidate();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                ApkFragment apkFragment = (ApkFragment) getSupportFragmentManager().findFragmentByTag("f1");
                if (apkFragment != null) apkFragment.contextualBarRemoved();
                getWindow().setStatusBarColor(ThemeUtils.getColorAttr(MainActivity.this, android.R.attr.colorBackground));
            }
        };


        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        actionMode = startActionMode(actionModeCallback);


    }

    public boolean isContextualBarShown() {
        return actionMode != null;
    }

    public void removeContextualBar() {
        actionMode.finish();
    }

    public void setContextualBarTitle(int count) {
        actionMode.setTitle(count + " selected");
    }

    public void allItemSelected() {
        isAllItemSelected = true;
        actionMode.invalidate();
    }
}