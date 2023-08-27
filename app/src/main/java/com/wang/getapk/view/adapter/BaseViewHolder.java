package com.wang.getapk.view.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * Created by wangxiaojie on 2023/8/24.
 */
public class BaseViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder {

    public final T viewBinding;

    public BaseViewHolder(@NonNull T viewBinding) {
        super(viewBinding.getRoot());
        this.viewBinding = viewBinding;
    }
}
