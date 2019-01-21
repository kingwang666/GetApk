package com.wang.getapk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;


/**
 * Author: wangxiaojie6
 * Date: 2018/1/9
 */
@SuppressLint("RestrictedApi")
public class DrawableHelper {

    private static final String TAG = DrawableHelper.class.getSimpleName();

    public static void tintProgressDrawable(@NonNull Drawable drawable, @ColorInt int... colors) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable)
                && drawable.mutate() != drawable) {
            Log.e(TAG, "Mutated drawable is not the same instance as the input.");
            return;
        }
        if (colors == null || colors.length == 0) {
            drawable.clearColorFilter();
            return;
        }
        if (drawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) drawable;
            tintDrawable(ld.findDrawableByLayerId(android.R.id.background), colors[0], null);
            tintDrawable(ld.findDrawableByLayerId(android.R.id.secondaryProgress), colors.length > 1 ? colors[1] : colors[0], null);
            tintDrawable(ld.findDrawableByLayerId(android.R.id.progress), colors.length > 2 ? colors[2] : colors[0], null);
        } else {
            tintDrawable(drawable, colors[0], null);
        }
    }

    public static void tintDrawable(@NonNull Drawable drawable, @ColorInt int color, PorterDuff.Mode mode) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable)
                && drawable.mutate() != drawable) {
            Log.e(TAG, "Mutated drawable is not the same instance as the input.");
            return;
        }
        drawable.setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(color, mode == null ? PorterDuff.Mode.SRC_IN : mode));
    }


    public static void tintDrawable(@NonNull Drawable drawable, ColorStateList tintList, PorterDuff.Mode mode, int[] state) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable)
                && drawable.mutate() != drawable) {
            Log.e(TAG, "Mutated drawable is not the same instance as the input.");
            return;
        }

        if (tintList != null || mode != null) {
            drawable.setColorFilter(createTintFilter(tintList, mode == null ? PorterDuff.Mode.SRC_IN : mode, state));
        } else {
            drawable.clearColorFilter();
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            // Pre-v23 there is no guarantee that a state change will invoke an invalidation,
            // so we force it ourselves
            drawable.invalidateSelf();
        }
    }

    public static Drawable tintDrawable(@NonNull Context context, @NonNull Drawable drawable, int colorAttr, int alpha, PorterDuff.Mode tintMode) {
        if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
            drawable = drawable.mutate();
        }
        final int color = getThemeAttrColor(context, colorAttr);
        drawable.setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(color, tintMode));

        if (alpha != -1) {
            drawable.setAlpha(alpha);
        }

        return drawable;
    }


    @Nullable
    public static Drawable getTintDrawable(@NonNull Context context, @DrawableRes int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (drawable != null) {
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
        }
        return drawable;
    }

    @Nullable
    public static Drawable tintDrawable(@NonNull Context context, @DrawableRes int resId, @ColorRes int tint, PorterDuff.Mode tintMode) {
        ColorStateList tintList = ContextCompat.getColorStateList(context, tint);
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (tintList != null && drawable != null) {
            // First mutate the Drawable, then wrap it and set the tint list

            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);

            if (tintMode != null) {
                DrawableCompat.setTintMode(drawable, tintMode);
            }
        }
        return drawable;
    }

    @Nullable
    public static Drawable tintDrawable(@NonNull Context context, @DrawableRes int resId, ColorStateList tintList, PorterDuff.Mode tintMode) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (tintList != null && drawable != null) {
            // First mutate the Drawable, then wrap it and set the tint list

            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);

            if (tintMode != null) {
                DrawableCompat.setTintMode(drawable, tintMode);
            }
        }
        return drawable;
    }

    public static Drawable tintDrawable(@NonNull Context context, @NonNull Drawable drawable, @ColorRes int tint, PorterDuff.Mode tintMode) {
        ColorStateList tintList = ContextCompat.getColorStateList(context, tint);
        if (tintList != null) {
            // First mutate the Drawable, then wrap it and set the tint list
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);

            if (tintMode != null) {
                DrawableCompat.setTintMode(drawable, tintMode);
            }
        }
        return drawable;
    }

    public static Drawable tintDrawable(@NonNull Drawable drawable, ColorStateList tintList, PorterDuff.Mode tintMode) {
        if (tintList != null) {
            // First mutate the Drawable, then wrap it and set the tint list
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);

            if (tintMode != null) {
                DrawableCompat.setTintMode(drawable, tintMode);
            }
        }
        return drawable;
    }


    public static int getThemeAttrColor(Context context, int attr) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, new int[]{attr});
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    private static PorterDuffColorFilter createTintFilter(ColorStateList tint, PorterDuff.Mode tintMode, final int[] state) {
        if (tint == null || tintMode == null) {
            return null;
        }
        final int color = tint.getColorForState(state, Color.TRANSPARENT);
        return AppCompatDrawableManager.getPorterDuffColorFilter(color, tintMode);
    }

}
