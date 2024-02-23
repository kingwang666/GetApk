package com.wang.getapk.view.adapter;

import android.view.View;

import com.wang.baseadapter.BaseRecyclerViewAdapter;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.model.App;
import com.wang.getapk.model.ModelChangeEvent;
import com.wang.getapk.view.adapter.delegate.AppDelegate;
import com.wang.getapk.view.adapter.delegate.StickyDelegate;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class AppAdapter extends BaseRecyclerViewAdapter {

    public static final int TYPE_STICKY = 1;
    public static final int TYPE_APP = 2;

    private boolean mIsSelectMode;

    public AppAdapter(ItemArray itemArray, OnAppClickListener listener) {
        super(itemArray);
        delegatesManager.addDelegate(TYPE_STICKY, new StickyDelegate());
        delegatesManager.addDelegate(TYPE_APP, new AppDelegate(listener));
    }

    public void setSelectMode(boolean selectMode) {
        if (mIsSelectMode != selectMode) {
            mIsSelectMode = selectMode;
            notifyItemRangeChanged(0, getItemCount(), new ModelChangeEvent(selectMode));
        }
    }

    public boolean isSelectMode() {
        return mIsSelectMode;
    }

    public interface OnAppClickListener {
        void onDetail(App app, View iconImg);

        void onSettings(App app);

        void onSelectChanged(App app, boolean isChecked, int position);
    }
}
