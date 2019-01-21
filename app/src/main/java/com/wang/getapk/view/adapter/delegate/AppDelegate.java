package com.wang.getapk.view.adapter.delegate;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.baseadapter.delegate.AdapterDelegate;
import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.R;
import com.wang.getapk.model.App;
import com.wang.getapk.view.adapter.AppAdapter;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class AppDelegate extends AdapterDelegate<AppDelegate.AppViewHolder> {

    private AppAdapter.OnAppClickListener mListener;

    public AppDelegate(AppAdapter.OnAppClickListener listener) {
        mListener = listener;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(ItemArray itemArray, AppViewHolder vh, int position) {
        App app = itemArray.get(position).getData();
        Context context = vh.itemView.getContext().getApplicationContext();
        vh.mApp = app;
        vh.getIcon();
        vh.mNameTV.setText(app.name);
        int colorDagger = ContextCompat.getColor(context, R.color.red500);
        int colorNor = ContextCompat.getColor(context, R.color.blue500);
        if (app.isSystem) {
            vh.mSystemTV.setTextColor(colorDagger);
            vh.mSystemTV.setText(R.string.system);
        } else {
            vh.mSystemTV.setTextColor(colorNor);
            vh.mSystemTV.setText(R.string.third_party);
        }
        if (app.isDebug) {
            vh.mDebugTV.setTextColor(colorNor);
            vh.mDebugTV.setText(R.string.debug);
        } else {
            vh.mDebugTV.setTextColor(colorDagger);
            vh.mDebugTV.setText(R.string.release);
        }
        vh.mSizeTV.setText(Formatter.formatFileSize(context, new File(app.apkPath).length()));
        vh.mVersionNameTV.setText(app.versionName);
        vh.mTimeTV.setText(app.time);
    }

    @Override
    protected void onViewRecycled(AppViewHolder vh) {
        vh.recycler();
    }


    static class AppViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon_img)
        AppCompatImageView mIconImg;
        @BindView(R.id.name_tv)
        AppCompatTextView mNameTV;
        @BindView(R.id.system_tv)
        AppCompatTextView mSystemTV;
        @BindView(R.id.debug_tv)
        AppCompatTextView mDebugTV;
        @BindView(R.id.size_tv)
        AppCompatTextView mSizeTV;
        @BindView(R.id.version_name_tv)
        AppCompatTextView mVersionNameTV;
        @BindView(R.id.time_tv)
        AppCompatTextView mTimeTV;

        App mApp;

        Disposable mDisposable;

        public AppViewHolder(View itemView, final AppAdapter.OnAppClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDetail(mApp, mIconImg);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onSave(mApp);
                    return true;
                }
            });
        }

        @OnClick(R.id.icon_img)
        public void onClick(){
            if (mApp.launch != null) {
                itemView.getContext().startActivity(mApp.launch);
            }
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
                            mIconImg.setImageDrawable(drawable);
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
