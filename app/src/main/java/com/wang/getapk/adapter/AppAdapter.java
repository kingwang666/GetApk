package com.wang.getapk.adapter;

import android.view.View;

import com.wang.baseadapter.BaseRecyclerViewAdapter;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.adapter.delegate.AppDelegate;
import com.wang.getapk.adapter.delegate.StickyDelegate;
import com.wang.getapk.listener.OnRecyclerClickListener;
import com.wang.getapk.model.App;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class AppAdapter extends BaseRecyclerViewAdapter {

    public static final int TYPE_STICKY = 1;
    public static final int TYPE_APP = 2;

    public AppAdapter(ItemArray itemArray, OnAppClickListener listener) {
        super(itemArray);
        delegatesManager.addDelegate(TYPE_STICKY, new StickyDelegate());
        delegatesManager.addDelegate(TYPE_APP, new AppDelegate(listener));
    }

    public interface OnAppClickListener {

        void onDetail(App app, View iconImg);

        void onSave(App app);

    }
}
