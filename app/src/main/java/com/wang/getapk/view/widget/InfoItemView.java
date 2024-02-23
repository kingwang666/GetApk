package com.wang.getapk.view.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textview.MaterialTextView;
import com.wang.getapk.R;

/**
 * Created by wangxiaojie on 2023/8/24.
 */
public class InfoItemView extends LinearLayout {

    private MaterialTextView mTitleTextView;
    private MaterialTextView mTextView;

    boolean mTextIsSelectable;

    private int mTitleTextAppearance;
    private int mTextAppearance;

    private CharSequence mTitleText;
    private CharSequence mText;

    private ColorStateList mTitleTextColor;
    private ColorStateList mTextColor;

    private int mTitleMargin;

    public InfoItemView(Context context) {
        this(context, null);
    }

    public InfoItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.infoItemViewStyle);
    }

    public InfoItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public InfoItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.InfoItemView, defStyleAttr, 0);
        mTextIsSelectable = a.getBoolean(R.styleable.InfoItemView_android_textIsSelectable, false);
        mTitleTextAppearance = a.getResourceId(R.styleable.InfoItemView_titleTextAppearance, 0);
        mTextAppearance = a.getResourceId(R.styleable.InfoItemView_textAppearance, 0);
        mTitleMargin = a.getDimensionPixelOffset(R.styleable.InfoItemView_titleMargin, 0);

        final CharSequence title = a.getText(R.styleable.InfoItemView_title);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        final CharSequence text = a.getText(R.styleable.InfoItemView_android_text);
        if (!TextUtils.isEmpty(text)) {
            setText(text);
        }
        if (a.hasValue(R.styleable.InfoItemView_titleTextColor)) {
            setTitleTextColor(a.getColorStateList(R.styleable.InfoItemView_titleTextColor));
        }

        if (a.hasValue(R.styleable.InfoItemView_android_textColor)) {
            setTextColor(a.getColorStateList(R.styleable.InfoItemView_android_textColor));
        }
        a.recycle();
    }

    private boolean isChild(View child) {
        return child.getParent() == this;
    }


    public CharSequence getTitle() {
        return mTitleText;
    }


    public void setTitle(@StringRes int resId) {
        setTitle(getContext().getText(resId));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (mTitleTextView == null) {
                final Context context = getContext();
                mTitleTextView = new MaterialTextView(context);
                if (mTitleTextAppearance != 0) {
                    mTitleTextView.setTextAppearance(context, mTitleTextAppearance);
                }
                if (mTitleTextColor != null) {
                    mTitleTextView.setTextColor(mTitleTextColor);
                }
            }
            if (!isChild(mTitleTextView)) {
                final ViewGroup.LayoutParams vlp = mTitleTextView.getLayoutParams();
                final LinearLayout.LayoutParams lp;
                if (vlp == null) {
                    lp = generateDefaultLayoutParams();
                } else if (!checkLayoutParams(vlp)) {
                    lp = generateLayoutParams(vlp);
                } else {
                    lp = (LinearLayout.LayoutParams) vlp;
                }
                lp.setMarginEnd(mTitleMargin);
                addView(mTitleTextView, 0, lp);
            }
        } else if (mTitleTextView != null && isChild(mTitleTextView)) {
            removeView(mTitleTextView);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
        mTitleText = title;
    }

    public CharSequence getText() {
        return mText;
    }

    public void setText(@StringRes int resId) {
        setText(getContext().getText(resId));
    }


    public void setText(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            if (mTextView == null) {
                final Context context = getContext();
                mTextView = new MaterialTextView(context);
                mTextView.setTextIsSelectable(mTextIsSelectable);
                if (mTextAppearance != 0) {
                    mTextView.setTextAppearance(context, mTextAppearance);
                }
                if (mTextColor != null) {
                    mTextView.setTextColor(mTextColor);
                }
            }
            if (!isChild(mTextView)) {
                addView(mTextView);
            }
        } else if (mTextView != null && isChild(mTextView)) {
            removeView(mTextView);
        }
        if (mTextView != null) {
            mTextView.setText(text);
        }
        mText = text;
    }

    public void setTextIsSelectable(boolean selectable){
        mTextIsSelectable = selectable;
        if (mTextView != null){
            mTextView.setTextIsSelectable(selectable);
        }
    }

    public void setTitleTextAppearance(Context context, @StyleRes int resId) {
        mTitleTextAppearance = resId;
        if (mTitleTextView != null) {
            mTitleTextView.setTextAppearance(context, resId);
        }
    }

    public void setTextAppearance(Context context, @StyleRes int resId) {
        mTextAppearance = resId;
        if (mTextView != null) {
            mTextView.setTextAppearance(context, resId);
        }
    }

    public void setTitleTextColor(@ColorInt int color) {
        setTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setTitleTextColor(@NonNull ColorStateList color) {
        mTitleTextColor = color;
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
    }

    public void setTextColor(@ColorInt int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    public void setTextColor(@NonNull ColorStateList color) {
        mTextColor = color;
        if (mTextView != null) {
            mTextView.setTextColor(color);
        }
    }

    public void setTitleMargin(int titleMargin) {
        if (mTitleMargin == titleMargin) {
            return;
        }
        mTitleMargin = titleMargin;
        if (mTitleTextView != null && isChild(mTitleTextView)) {
            LinearLayout.LayoutParams params = (LayoutParams) mTitleTextView.getLayoutParams();
            params.setMarginEnd(titleMargin);
            mTitleTextView.setLayoutParams(params);
        }
    }
}
