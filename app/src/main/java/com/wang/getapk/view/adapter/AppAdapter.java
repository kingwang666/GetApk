package com.wang.getapk.view.adapter;

import com.wang.baseadapter.BaseRecyclerViewAdapter;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.view.adapter.delegate.AppDelegate;
import com.wang.getapk.view.adapter.delegate.StickyDelegate;
import com.wang.getapk.view.listener.OnRecyclerClickListener;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class AppAdapter extends BaseRecyclerViewAdapter {

    public static final int TYPE_STICKY = 1;
    public static final int TYPE_APP = 2;

    public AppAdapter(ItemArray itemArray, OnRecyclerClickListener listener) {
        super(itemArray);
        delegatesManager.addDelegate(TYPE_STICKY, new StickyDelegate());
        delegatesManager.addDelegate(TYPE_APP, new AppDelegate(listener));
    }
}
