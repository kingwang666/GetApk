package com.wang.getapk.util;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;


/**
 * Created on 2015/3/9.
 * Author: wang
 */
public class SizeUtil {

    public static float dp2px(Context context, float dpValue) {
        if (context == null) {
            return dpValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    public static int dp2pxOffset(Context context, float dpValue) {
        return (int) dp2px(context, dpValue);
    }

    public static int dp2pxSize(Context context, float dpValue) {
        final float f = dp2px(context, dpValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (dpValue == 0) return 0;
        if (dpValue > 0) return 1;
        return -1;
    }

    public static float px2dp(Context context, float pxValue) {
        if (context == null) {
            return pxValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    public static int px2dpOffset(Context context, float pxValue) {
        return (int) px2dp(context, pxValue);
    }

    public static int px2dpSize(Context context, float pxValue) {
        final float f = px2dp(context, pxValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (pxValue == 0) return 0;
        if (pxValue > 0) return 1;
        return -1;
    }

    public static float sp2px(Context context, float spValue) {
        if (context == null) {
            return spValue;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    public static int sp2pxOffset(Context context, float spValue) {
        return (int) sp2px(context, spValue);
    }

    public static int sp2pxSize(Context context, float spValue) {
        final float f = sp2px(context, spValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (spValue == 0) return 0;
        if (spValue > 0) return 1;
        return -1;
    }

    public static float px2sp(Context context, float pxValue) {
        if (context == null) {
            return pxValue;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale;
    }

    public static int px2spOffset(Context context, float pxValue) {
        return (int) px2sp(context, pxValue);
    }

    public static int px2spSize(Context context, float pxValue) {
        final float f = px2sp(context, pxValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (pxValue == 0) return 0;
        if (pxValue > 0) return 1;
        return -1;
    }

    public static float mm2px(Context context, float mmValue) {
        if (context == null) {
            return mmValue;
        }
        final float xdpi = context.getResources().getDisplayMetrics().xdpi;
        return mmValue * xdpi * (1.0f / 25.4f);
    }

    public static int mm2pxOffset(Context context, float mmValue) {
        return (int) mm2px(context, mmValue);
    }

    public static int mm2pxSize(Context context, float mmValue) {
        final float f = mm2px(context, mmValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (mmValue == 0) return 0;
        if (mmValue > 0) return 1;
        return -1;
    }

    public static float px2mm(Context context, float pxValue) {
        if (context == null) {
            return pxValue;
        }
        final float xdpi = context.getResources().getDisplayMetrics().xdpi;
        return 25.4f * pxValue / xdpi;
    }

    public static int px2mmOffset(Context context, float pxValue) {
        return (int) px2mm(context, pxValue);
    }

    public static int px2mmSize(Context context, float pxValue) {
        final float f = px2mm(context, pxValue);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (pxValue == 0) return 0;
        if (pxValue > 0) return 1;
        return -1;
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }
}
