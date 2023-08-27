package com.wang.getapk.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.getapk.R;
import com.wang.getapk.databinding.ItemFileBinding;
import com.wang.getapk.model.FileItem;
import com.wang.getapk.view.listener.OnRecyclerClickListener;

import java.util.List;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/11
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    @NonNull
    private final List<FileItem> mFileItems;
    @NonNull
    private final OnRecyclerClickListener mListener;

    public FileAdapter(@NonNull List<FileItem> fileItems, @NonNull OnRecyclerClickListener listener) {
        mFileItems = fileItems;
        mListener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(ItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder vh, int position) {
        FileItem item = mFileItems.get(position);
        if (item.isDirectory) {
            vh.viewBinding.nameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_folder_blue_24dp, 0, 0, 0);
        } else {
            vh.viewBinding.nameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_android_green_24dp, 0, 0, 0);
        }
        vh.viewBinding.nameTv.setText(item.name);
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    class FileViewHolder extends BaseViewHolder<ItemFileBinding> {

        public FileViewHolder(@NonNull ItemFileBinding viewBinding) {
            super(viewBinding);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(0, getAdapterPosition(), null);
                }
            });
        }
    }
}
