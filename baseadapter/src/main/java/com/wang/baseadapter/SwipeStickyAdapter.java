package com.wang.baseadapter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by wang
 * on 2017/2/20
 */

public interface SwipeStickyAdapter {

    void onBindSwipeViewHolder(RecyclerView.ViewHolder holder, int position);

}
