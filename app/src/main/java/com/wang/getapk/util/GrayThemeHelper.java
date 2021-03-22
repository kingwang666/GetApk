package com.wang.getapk.util;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

/**
 * Created on 3/19/21
 * Author: wangxiaojie
 * Description:
 */
public class GrayThemeHelper {

    private static final int NO_STYLE = -1;

    private int mOldLayerType = NO_STYLE;
    private Paint mPaint;

    public void applyGrayTheme(@Nullable Window window) {
        if (window == null) {
            return;
        }
        applyGrayTheme(window.getDecorView());
    }

    public void applyGrayTheme(@Nullable View view) {
        if (view == null) {
            return;
        }
        if (mPaint == null) {
            mPaint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            mPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        }
        int oldLayerType = view.getLayerType();
        if (oldLayerType == View.LAYER_TYPE_NONE) {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
            mOldLayerType = oldLayerType;
        } else {
            view.setLayerPaint(mPaint);
        }
    }

    public void removeGrayTheme(@Nullable Window window) {
        if (window == null) {
            return;
        }
        removeGrayTheme(window.getDecorView());
    }

    public void removeGrayTheme(@Nullable View view) {
        if (view == null) {
            return;
        }
        Paint currentPaint;
        if (view.getLayerType() == View.LAYER_TYPE_NONE
                || mPaint == null
                || ((currentPaint = getCurrentPaint(view)) != null && currentPaint != mPaint)) {
            mPaint = null;
            mOldLayerType = NO_STYLE;
            return;
        }

        if (mOldLayerType == View.LAYER_TYPE_NONE) {
            view.setLayerType(View.LAYER_TYPE_NONE, null);
        } else {
            view.setLayerPaint(null);
        }
        mPaint = null;
        mOldLayerType = NO_STYLE;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private Paint getCurrentPaint(View view) {
        try {
            Field field = View.class.getDeclaredField("mLayerPaint");
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(view);
            if (o instanceof Paint) {
                return (Paint) o;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
