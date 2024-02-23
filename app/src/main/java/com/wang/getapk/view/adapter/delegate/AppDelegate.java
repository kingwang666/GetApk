package com.wang.getapk.view.adapter.delegate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.wang.baseadapter.delegate.AdapterDelegate;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.R;
import com.wang.getapk.databinding.ItemAppBinding;
import com.wang.getapk.glide.GlideApp;
import com.wang.getapk.model.App;
import com.wang.getapk.model.ModelChangeEvent;
import com.wang.getapk.model.SelectEvent;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.view.adapter.BaseViewHolder;

import java.io.File;
import java.util.List;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class AppDelegate extends AdapterDelegate<AppDelegate.AppViewHolder> {

    private final AppAdapter.OnAppClickListener mListener;

    public AppDelegate(AppAdapter.OnAppClickListener listener) {
        mListener = listener;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(ItemArray itemArray, AppViewHolder vh, int position, List<Object> payloads) {
        if (payloads.size() != 1){
            super.onBindViewHolder(itemArray, vh, position, payloads);
            return;
        }
        Object payload = payloads.get(0);
        if (payload instanceof ModelChangeEvent) {
            App app = itemArray.get(position).getData();
            ModelChangeEvent event = (ModelChangeEvent) payload;
            int vis = event.isSelectMode ? View.VISIBLE : View.GONE;
            if (vis == vh.viewBinding.checkbox.getVisibility()) {
                return;
            }
            TransitionManager.endTransitions(vh.viewBinding.parent);
            vh.viewBinding.checkbox.setVisibility(vis);
            vh.viewBinding.checkbox.setChecked(app.isSelected);
            vh.itemView.setLongClickable(event.isSelectMode);
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.excludeChildren(vh.viewBinding.iconImg, true);
            TransitionManager.beginDelayedTransition(vh.viewBinding.parent, autoTransition);
            return;
        }
        if (payload instanceof SelectEvent) {
            vh.viewBinding.checkbox.setChecked(payload == SelectEvent.SELECTED);
            return;
        }
        super.onBindViewHolder(itemArray, vh, position, payloads);
    }

    @Override
    public void onBindViewHolder(ItemArray itemArray, AppViewHolder vh, int position) {
        App app = itemArray.get(position).getData();
        Context context = vh.itemView.getContext().getApplicationContext();
        vh.setApp(app);
        final boolean isSelectMode = vh.isSelectMode();
        if (isSelectMode) {
            vh.viewBinding.checkbox.setVisibility(View.VISIBLE);
            vh.viewBinding.checkbox.setChecked(app.isSelected);
            vh.itemView.setLongClickable(false);
        } else {
            vh.viewBinding.checkbox.setVisibility(View.GONE);
            vh.itemView.setLongClickable(true);
        }

        GlideApp.with(vh.itemView.getContext()).load(app.applicationInfo).into(vh.viewBinding.iconImg);
        vh.viewBinding.nameTv.setText(app.name);
        ColorStateList colorDagger = ContextCompat.getColorStateList(context, R.color.red500);
        ColorStateList colorNor = ContextCompat.getColorStateList(context, R.color.blue500);
        if (app.isSystem) {
            vh.viewBinding.systemTv.setTextColor(colorDagger);
            vh.viewBinding.systemTv.setText(R.string.system);
        } else {
            vh.viewBinding.systemTv.setTextColor(colorNor);
            vh.viewBinding.systemTv.setText(R.string.third_party);
        }
        if (app.isDebug) {
            vh.viewBinding.debugTv.setTextColor(colorNor);
            vh.viewBinding.debugTv.setText(R.string.debug);
        } else {
            vh.viewBinding.debugTv.setTextColor(colorDagger);
            vh.viewBinding.debugTv.setText(R.string.release);
        }
        vh.viewBinding.sizeTv.setText(Formatter.formatFileSize(context, new File(app.apkPath).length()));
        vh.viewBinding.versionNameTv.setText(app.versionName);
        vh.viewBinding.timeTv.setText(app.time);
    }

    @Override
    protected void onViewRecycled(AppViewHolder vh) {
        GlideApp.with(vh.itemView.getContext()).clear(vh.viewBinding.iconImg);
        vh.recycler();
    }


    static class AppViewHolder extends BaseViewHolder<ItemAppBinding> {

        private App mApp;

        public AppViewHolder(@NonNull ItemAppBinding viewBinding, final AppAdapter.OnAppClickListener listener) {
            super(viewBinding);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mApp != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                        if (isSelectMode()) {
                            viewBinding.checkbox.toggle();
                        } else {
                            listener.onDetail(mApp, viewBinding.iconImg);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mApp != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onSettings(mApp);
                    }
                    return true;
                }
            });
            viewBinding.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mApp != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION && mApp.isSelected != isChecked) {
                        mApp.isSelected = isChecked;
                        listener.onSelectChanged(mApp, isChecked, getBindingAdapterPosition());
                    }
                }
            });
            viewBinding.iconImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mApp != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION && mApp.launch != null) {
                        itemView.getContext().startActivity(mApp.launch);
                    }
                }
            });
        }

        private boolean isSelectMode() {
            AppAdapter adapter = (AppAdapter) getBindingAdapter();
            return adapter != null && adapter.isSelectMode();
        }

        public void setApp(App app) {
            mApp = app;
        }

        public App getApp() {
            return mApp;
        }


        public void recycler() {

        }

    }
}
