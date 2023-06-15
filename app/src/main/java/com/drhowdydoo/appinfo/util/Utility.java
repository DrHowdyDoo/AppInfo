package com.drhowdydoo.appinfo.util;

import android.content.Context;

public class Utility {

    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
