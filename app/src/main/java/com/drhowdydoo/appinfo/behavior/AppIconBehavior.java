package com.drhowdydoo.appinfo.behavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.drhowdydoo.appinfo.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;

public class AppIconBehavior extends CoordinatorLayout.Behavior<ShapeableImageView> {


    private final Context mContext;
    private final float mAvatarMaxSize = 76f;
    private int mStartYPosition;
    private int mFinalYPosition;
    private int finalHeight;
    private int mStartHeight;
    private int mFinalXPosition;
    private float mStartToolbarPosition;


    public AppIconBehavior(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull ShapeableImageView child, @NonNull View dependency) {
        return dependency instanceof MaterialToolbar;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull ShapeableImageView child, @NonNull View dependency) {
        maybeInitProperties(child, dependency);

        final int maxScrollDistance = (int) (mStartToolbarPosition - getStatusBarHeight());
        float expandedPercentageFactor = dependency.getY() / maxScrollDistance;

        float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                * (1f - expandedPercentageFactor)) + (child.getHeight()/2);

        float heightToSubtract = ((mStartHeight - finalHeight) * (1f - expandedPercentageFactor));

        child.setY(mStartYPosition - distanceYToSubtract);

        int proportionalAvatarSize = (int) (mAvatarMaxSize * (expandedPercentageFactor));

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.width = (int) (mStartHeight - heightToSubtract);
        lp.height = (int) (mStartHeight - heightToSubtract);
        child.setLayoutParams(lp);
        return true;
    }

    @SuppressLint("PrivateResource")
    private void maybeInitProperties(ShapeableImageView child, View dependency) {
        if (mStartYPosition == 0)
            mStartYPosition = (int) (dependency.getY());

        if (mFinalYPosition == 0)
            mFinalYPosition = (dependency.getHeight() /2);

        if (mStartHeight == 0)
            mStartHeight = child.getHeight();

        if (finalHeight == 0)
            finalHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.app_icon_small);

        if (mFinalXPosition == 0)
            mFinalXPosition = mContext.getResources().getDimensionPixelOffset(com.google.android.material.R.dimen.abc_action_bar_content_inset_material) + (finalHeight / 2);

        if (mStartToolbarPosition == 0)
            mStartToolbarPosition = dependency.getY() + (dependency.getHeight()/2);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
