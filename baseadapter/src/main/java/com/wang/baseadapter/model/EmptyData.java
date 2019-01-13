package com.wang.baseadapter.model;

import androidx.annotation.DrawableRes;

/**
 * Created by wang
 * on 2016/12/28
 */

public class EmptyData {

    @DrawableRes
    public int mResId;

    public CharSequence mTitle;

    public CharSequence mDesc;

    public EmptyData(int resId, CharSequence title, CharSequence desc) {
        mResId = resId;
        mTitle = title;
        mDesc = desc;
    }

    public EmptyData(int resId) {
        this(resId, null, null);
    }

    public EmptyData(CharSequence desc) {
        this(-1, null, desc);
    }

    public EmptyData() {
        this(-1, null, null);
    }
}
