package com.wang.getapk.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.getapk.model.App;
import com.wang.getapk.adapter.AppAdapter;
import com.wang.getapk.model.StickyPinyin;
import com.wang.getapk.model.StickyTime;
import com.wang.getapk.util.PinyinComparator;
import com.wang.getapk.util.TimeComparator;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class MainActivityPresenter {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Handler mHandler;
    private IView mView;
    private List<App> mApps;
    private final ReadWriteLock mLock = new ReentrantReadWriteLock();

    public MainActivityPresenter(IView view) {
        mHandler = new Handler(Looper.getMainLooper());
        mView = view;
    }

    private Publisher<List<App>> getApps(Context context) {
        final Context appContext = context.getApplicationContext();
        return Flowable.just(appContext)
                .map(new Function<Context, List<App>>() {
                    @Override
                    public List<App> apply(Context context) throws Exception {
                        final PackageManager pm = context.getPackageManager();
                        List<PackageInfo> infos = pm.getInstalledPackages(0);
                        List<App> apps = new ArrayList<>();
                        for (PackageInfo info : infos) {
                            App app = new App(info, pm);
                            app.isFormFile = false;
                            Date date = new Date(info.lastUpdateTime);
                            app.time = mDateFormat.format(date);
                            apps.add(app);
                        }
                        setApps(apps);
                        return apps;
                    }
                });
    }

    public Disposable getAndSort(Context context, final boolean sortByTime) {
        return Flowable.just(context.getApplicationContext())
                .flatMap(new Function<Context, Publisher<List<App>>>() {
                    @Override
                    public Publisher<List<App>> apply(Context context) throws Exception {
                        List<App> apps = getApps();
                        if (apps == null) {
                            return getApps(context);
                        } else {
                            return Flowable.just(apps);
                        }
                    }
                })
                .map(new SortFun(mDateFormat, sortByTime))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<ItemArray>() {
                    @Override
                    public void onNext(ItemArray apps) {
                        mView.getAppsSuccess(apps, sortByTime);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("error", "", t);
                        mView.getAppsError(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public Disposable saveApk(App app, final String dest) {
        return Flowable.just(app)
                .map(new Function<App, String>() {
                    @Override
                    public String apply(App source) throws Exception {
                        File file = new File(source.apkPath);
                        if (!file.exists()) {
                            throw new Exception("the apk file is no exists");
                        }
                        String fileName = source.namePinyin + "_" + source.versionName + ".apk";
                        File dst = new File(dest, fileName);
                        if (dst.exists()) {
                            dst.delete();
                        }
                        long total = file.length();
                        long sum = 0;
                        InputStream in = new FileInputStream(file);
                        try {
                            OutputStream out = new FileOutputStream(dst);
                            try {
                                // Transfer bytes from in to out
                                byte[] buf = new byte[1024 * 4];
                                int len;
                                Thread thread = Thread.currentThread();
                                while ((len = in.read(buf)) > 0) {
                                    if (thread.isInterrupted()) {
                                        break;
                                    }
                                    sum += len;
                                    out.write(buf, 0, len);
                                    final long finalSum = sum;
                                    final long finalTotal = total;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mView.inProgress(finalSum * 1.0f / finalTotal);
                                        }
                                    });

                                }
                            } finally {
                                out.close();
                            }
                        } finally {
                            in.close();
                        }
                        return dst.getAbsolutePath();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<String>() {
                    @Override
                    public void onNext(String path) {
                        mView.saveSuccess(path);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("error", t.getMessage(), t);
                        mView.saveError(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void setApps(List<App> apps) {
        mLock.writeLock().lock();
        try {
            mApps = apps;
        } finally {
            mLock.writeLock().unlock();
        }
    }

    private List<App> getApps() {
        mLock.readLock().lock();
        try {
            return mApps;
        } finally {
            mLock.readLock().unlock();
        }
    }

    public Disposable getApp(Context context, String path) {
        return Flowable.just(path)
                .map(path1 -> {
                    PackageManager pm = context.getPackageManager();
                    PackageInfo info = pm.getPackageArchiveInfo(path1, 0);
                    if (info == null) {
                        throw new NullPointerException();
                    }
                    ApplicationInfo applicationInfo = info.applicationInfo;
                    applicationInfo.sourceDir = path;
                    applicationInfo.publicSourceDir = path;
                    App app = new App(info, pm);
                    app.isFormFile = true;
                    return app;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<App>() {
                    @Override
                    public void onNext(App app) {
                        mView.getAppSuccess(app);
                    }

                    @Override
                    public void onError(Throwable t) {
                        mView.getAppError(t.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    static class SortFun implements Function<List<App>, ItemArray> {

        private final SimpleDateFormat mDateFormat;
        private final boolean mSortByTime;

        public SortFun(SimpleDateFormat dateFormat, boolean sortByTime) {
            mDateFormat = dateFormat;
            mSortByTime = sortByTime;
        }

        @Override
        public ItemArray apply(List<App> apps) throws Exception {
            ItemArray itemArray = new ItemArray();
            List<Object> stickies = new ArrayList<>();
            for (App app : apps) {
                if (mSortByTime) {
                    StickyTime sticky = new StickyTime(app.time.substring(0, 7));
                    if (!stickies.contains(sticky)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(mDateFormat.parse(app.time));
                        calendar.set(Calendar.DAY_OF_MONTH, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.add(Calendar.MONTH, 1);
                        sticky.lastUpdateTime = calendar.getTimeInMillis() + 24 * 60 * 60 * 1000 - 1;
                        stickies.add(sticky);
                        itemArray.add(new ItemData(AppAdapter.TYPE_STICKY, sticky));
                    }
                } else {
                    StickyPinyin sticky = new StickyPinyin(app.namePinyin.isEmpty() ? null : app.namePinyin.substring(0, 1));
                    if (!stickies.contains(sticky)) {
                        stickies.add(sticky);
                        itemArray.add(new ItemData(AppAdapter.TYPE_STICKY, sticky));
                    }
                }
                itemArray.add(new ItemData(AppAdapter.TYPE_APP, app));
            }
            if (mSortByTime) {
                Collections.sort(itemArray, new TimeComparator());
            } else {
                Collections.sort(itemArray, new PinyinComparator());
            }
            return itemArray;
        }

    }

    public interface IView {

        void getAppsSuccess(ItemArray apps, boolean sortByTime);

        void getAppsError(String message);

        void getAppSuccess(App app);

        void getAppError(String error);

        void inProgress(float progress);

        void saveSuccess(String path);

        void saveError(String message);

    }
}
