package com.wang.getapk.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/17
 */

@SuppressWarnings("unchecked")
abstract class BaseBuilder<T extends BaseBuilder> {

    protected final Context context;
    protected CharSequence title;

    protected int titleColor;
    protected boolean titleColorSet = false;

    protected int softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;

    protected CharSequence positive;
    protected CharSequence neutral;
    protected CharSequence negative;

    protected BaseDialog.OnButtonClickListener onPositiveListener;
    protected BaseDialog.OnButtonClickListener onNegativeListener;
    protected BaseDialog.OnButtonClickListener onNeutralListener;

    protected boolean cancelable = true;
    protected boolean canceledOnTouchOutside = true;
    protected boolean autoDismiss = true;

    protected DialogInterface.OnShowListener showListener;
    protected DialogInterface.OnDismissListener dismissListener;
    protected DialogInterface.OnCancelListener cancelListener;
    protected DialogInterface.OnKeyListener keyListener;


    public BaseBuilder(@NonNull Context context) {
        this.context = context;
    }

    public T title(@StringRes int titleRes) {
        title(this.context.getText(titleRes));
        return (T) this;
    }

    public T title(@NonNull CharSequence title) {
        this.title = title;
        return (T) this;
    }


    public T titleColor(@ColorInt int color) {
        this.titleColor = color;
        this.titleColorSet = true;
        return (T) this;
    }

    public T softInputMode(int mode){
        this.softInputMode = mode;
        return (T) this;
    }

    public T titleColorRes(@ColorRes int colorRes) {
        return titleColor(ContextCompat.getColor(this.context, colorRes));
    }

    public T positive(@StringRes int positiveRes) {
        if (positiveRes == 0) {
            return (T) this;
        }
        positive(this.context.getText(positiveRes));
        return (T) this;
    }

    public T positive(@NonNull CharSequence message) {
        this.positive = message;
        return (T) this;
    }

    public T neutral(@StringRes int neutralRes) {
        if (neutralRes == 0) {
            return (T) this;
        }
        return neutral(this.context.getText(neutralRes));
    }

    public T neutral(@NonNull CharSequence message) {
        this.neutral = message;
        return (T) this;
    }

    public T negative(@StringRes int negativeRes) {
        if (negativeRes == 0) {
            return (T) this;
        }
        return negative(this.context.getText(negativeRes));
    }

    public T negative(@NonNull CharSequence message) {
        this.negative = message;
        return (T) this;
    }

    public T onPositive(@NonNull BaseDialog.OnButtonClickListener callback) {
        this.onPositiveListener = callback;
        return (T) this;
    }

    public T onNegative(@NonNull BaseDialog.OnButtonClickListener callback) {
        this.onNegativeListener = callback;
        return (T) this;
    }

    public T onNeutral(@NonNull BaseDialog.OnButtonClickListener callback) {
        this.onNeutralListener = callback;
        return (T) this;
    }

    public T onAny(@NonNull BaseDialog.OnButtonClickListener callback) {
        this.onPositiveListener = callback;
        this.onNegativeListener = callback;
        this.onNeutralListener = callback;
        return (T) this;
    }

    public T cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        this.canceledOnTouchOutside = cancelable;
        return (T) this;
    }

    public T canceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
        return (T) this;
    }

    /**
     * This defaults to true. If set to false, the dialog will not automatically be dismissed when
     * an action button is pressed, and not automatically dismissed when the user selects a list
     * item.
     *
     * @param dismiss Whether or not to dismiss the dialog automatically.
     * @return The Builder instance so you can chain calls to it.
     */
    public T autoDismiss(boolean dismiss) {
        this.autoDismiss = dismiss;
        return (T) this;
    }

    public T showListener(@NonNull DialogInterface.OnShowListener listener) {
        this.showListener = listener;
        return (T) this;
    }

    public T dismissListener(@NonNull DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
        return (T) this;
    }

    public T cancelListener(@NonNull DialogInterface.OnCancelListener listener) {
        this.cancelListener = listener;
        return (T) this;
    }

    public T keyListener(@NonNull DialogInterface.OnKeyListener listener) {
        this.keyListener = listener;
        return (T) this;
    }

}