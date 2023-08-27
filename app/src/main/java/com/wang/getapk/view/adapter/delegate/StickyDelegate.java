package com.wang.getapk.view.adapter.delegate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.wang.baseadapter.delegate.AdapterDelegate;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.databinding.ItemStickyBinding;
import com.wang.getapk.model.App;
import com.wang.getapk.model.StickyTime;
import com.wang.getapk.view.adapter.BaseViewHolder;


/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class StickyDelegate extends AdapterDelegate<StickyDelegate.StickyViewHolder> {

    @Override
    public StickyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickyViewHolder(ItemStickyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ItemArray itemArray, StickyViewHolder vh, int position) {
        App sticky = itemArray.get(position).getData();
        if (sticky instanceof StickyTime) {
            vh.viewBinding.nameTv.setText(sticky.time);
        } else {
            vh.viewBinding.nameTv.setText(sticky.namePinyin);
        }
    }

    static class StickyViewHolder extends BaseViewHolder<ItemStickyBinding> {

        public StickyViewHolder(@NonNull ItemStickyBinding viewBinding) {
            super(viewBinding);
        }
    }
}
