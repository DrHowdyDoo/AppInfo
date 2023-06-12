package com.drhowdydoo.appinfo;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.drhowdydoo.appinfo.adapter.ViewPagerAdapter;
import com.drhowdydoo.appinfo.bottomsheet.FilterBottomSheet;
import com.drhowdydoo.appinfo.bottomsheet.SortBottomSheet;
import com.drhowdydoo.appinfo.databinding.ActivityMainBinding;
import com.drhowdydoo.appinfo.fragment.ApkFragment;
import com.drhowdydoo.appinfo.fragment.AppFragment;
import com.drhowdydoo.appinfo.interfaces.OnSortFilterListener;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;

public class MainActivity extends AppCompatActivity implements OnSortFilterListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
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

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                binding.bottomNavigation.getMenu().getItem(position).setChecked(true);
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
            return true;
        });

        binding.materialToolBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.search) {
                if (binding.searchBar.isShown()) {
                    binding.searchBar.setVisibility(View.GONE);
                } else {
                    binding.searchBar.setVisibility(View.VISIBLE);
                    binding.searchInput.requestFocus();
                    imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
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
    }

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

    private Fragment getCurrentFragment(){
        int current = binding.viewPager.getCurrentItem();
        if (current == 0) return getSupportFragmentManager().findFragmentByTag("f0");
        else return getSupportFragmentManager().findFragmentByTag("f1");
    }
}