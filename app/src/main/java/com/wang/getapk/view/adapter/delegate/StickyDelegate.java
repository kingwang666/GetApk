package com.wang.getapk.view.adapter.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.baseadapter.delegate.AdapterDelegate;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.R;
import com.wang.getapk.model.App;
import com.wang.getapk.model.StickyTime;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class StickyDelegate extends AdapterDelegate<StickyDelegate.StickyViewHolder> {

    @Override
    public StickyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticky, parent, false);
        return new StickyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemArray itemArray, StickyViewHolder vh, int position) {
        App sticky = itemArray.get(position).getData();
        if (sticky instanceof StickyTime) {
            vh.mNameTV.setText(sticky.time);
        }else {
            vh.mNameTV.setText(sticky.namePinyin);
        }
    }

    static class StickyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name_tv)
        AppCompatTextView mNameTV;

        public StickyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
