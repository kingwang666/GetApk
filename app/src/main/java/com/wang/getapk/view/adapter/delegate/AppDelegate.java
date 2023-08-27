package com.wang.getapk.view.adapter.delegate;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.wang.baseadapter.delegate.AdapterDelegate;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.R;
import com.wang.getapk.databinding.ItemAppBinding;
import com.wang.getapk.model.App;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.view.adapter.BaseViewHolder;

import java.io.File;
import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;

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
    public void onBindViewHolder(ItemArray itemArray, AppViewHolder vh, int position) {
        App app = itemArray.get(position).getData();
        Context context = vh.itemView.getContext().getApplicationContext();
        vh.mApp = app;
        vh.getIcon();
        vh.viewBinding.nameTv.setText(app.name);
        int colorDagger = ContextCompat.getColor(context, R.color.red500);
        int colorNor = ContextCompat.getColor(context, R.color.blue500);
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
        vh.recycler();
    }


    static class AppViewHolder extends BaseViewHolder<ItemAppBinding> {

        App mApp;

        Disposable mDisposable;

        public AppViewHolder(@NonNull ItemAppBinding viewBinding, final AppAdapter.OnAppClickListener listener) {
            super(viewBinding);
         
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDetail(mApp, viewBinding.iconImg);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onSettings(mApp);
                    return true;
                }
            });
            viewBinding.iconImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mApp.launch != null) {
                        itemView.getContext().startActivity(mApp.launch);
                    }
                }
            });
        }
        
        public void getIcon() {
            recycler();
            final WeakReference<App> appWeak = new WeakReference<>(mApp);
            mDisposable = Flowable.just(itemView.getContext().getApplicationContext().getPackageManager())
                    .map(new Function<PackageManager, Drawable>() {
                        @Override
                        public Drawable apply(PackageManager pm) throws Exception {
                            App app = appWeak.get();
                            if (app != null){
                                return app.applicationInfo.loadIcon(pm);
                            }else {
                                throw new Exception("app is null");
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSubscriber<Drawable>() {
                        @Override
                        public void onNext(Drawable drawable) {
                            viewBinding.iconImg.setImageDrawable(drawable);
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        public void recycler() {
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }


    }
}
