package com.wang.getapk.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.model.App;
import com.wang.getapk.model.FileItem;
import com.wang.getapk.model.Sign;
import com.wang.getapk.util.FileUtil;
import com.wang.getapk.util.SignUtil;
import com.wang.getapk.view.listener.OnCopyListener;

import org.reactivestreams.Publisher;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Author: wang
 * Date: 2018/1/11
 */

public class LocalRepository {

    private static volatile LocalRepository sInstance;

    private List<App> mApps;

    private Handler mHandler;
    private final ReadWriteLock mLock = new ReentrantReadWriteLock();

    public static LocalRepository getInstance() {
        LocalRepository repository = sInstance;
        if (repository == null) {
            synchronized (LocalRepository.class) {
                repository = sInstance;
                if (repository == null) {
                    repository = new LocalRepository();
                    sInstance = repository;
                }
            }
        }
        return repository;
    }

    private LocalRepository() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private Publisher<List<App>> getApps(Context context, final DateFormat dateFormat) {
        return Flowable.just(new WeakReference<>(context))
                .map(new Function<WeakReference<Context>, List<App>>() {
                    @Override
                    public List<App> apply(WeakReference<Context> weakContext) throws Exception {
                        final PackageManager pm = weakContext.get().getPackageManager();
                        List<PackageInfo> infos = pm.getInstalledPackages(0);
                        List<App> apps = new ArrayList<>();
                        for (PackageInfo info : infos) {
                            App app = new App(info, pm);
                            app.isFormFile = false;
                            Date date = new Date(info.lastUpdateTime);
                            app.time = dateFormat.format(date);
                            apps.add(app);
                        }
                        setApps(apps);
                        return apps;
                    }
                });
    }

    public Disposable getAndSort(Context context, final boolean sortByTime, final DateFormat dateFormat, KWSubscriber<ItemArray> subscriber) {
        return Flowable.just(new WeakReference<>(context))
                .flatMap(new Function<WeakReference<Context>, Publisher<List<App>>>() {
                    @Override
                    public Publisher<List<App>> apply(WeakReference<Context> weakContext) throws Exception {
                        List<App> apps = getApps();
                        if (apps == null) {
                            return getApps(weakContext.get(), dateFormat);
                        } else {
                            return Flowable.just(apps);
                        }
                    }
                })
                .map(new SortFunction(dateFormat, sortByTime))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }

    public Disposable getApp(Context context, final String path, KWSubscriber<App> subscriber) {
        return Flowable.just(new WeakReference<>(context))
                .map(weakContext -> {
                    PackageManager pm = weakContext.get().getPackageManager();
                    PackageInfo info = pm.getPackageArchiveInfo(path, 0);
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
                .subscribeWith(subscriber);
    }

    @SuppressLint("PackageManagerGetSignatures")
    public Disposable getSignature(Context context, final String packageName, final boolean file, KWSubscriber<Sign> subscriber) {
        return Flowable.just(new WeakReference<>(context))
                .map(new Function<WeakReference<Context>, Sign>() {
                    @Override
                    public Sign apply(WeakReference<Context> weakContext) throws Exception {

                        Sign sign = new Sign();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            PackageInfo info;
                            if (file) {
                                info = weakContext.get().getPackageManager().getPackageArchiveInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                                if (info.signingInfo == null) {
                                    info = weakContext.get().getPackageManager().getPackageArchiveInfo(packageName, PackageManager.GET_SIGNATURES);
                                }
                            } else {
                                info = weakContext.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                            }
                            if (info == null) {
                                throw new NullPointerException();
                            }
                            SigningInfo signingInfo = info.signingInfo;
                            Signature[] signatures = signingInfo == null ? info.signatures : signingInfo.getApkContentsSigners();
                            if (signatures != null) {
                                sign.md5 = new String[signatures.length];
                                sign.sha1 = new String[signatures.length];
                                sign.sha256 = new String[signatures.length];
                                for (int i = 0; i < signatures.length; i++) {
                                    byte[] data = signatures[i].toByteArray();
                                    sign.md5[i] = SignUtil.getMD5(data);
                                    sign.sha1[i] = SignUtil.getSHA1(data);
                                    sign.sha256[i] = SignUtil.getSHA256(data);
                                }
                            }
                            if (signingInfo != null && !signingInfo.hasMultipleSigners() && signingInfo.hasPastSigningCertificates()) {
                                sign.hasHistory = true;
                                signatures = signingInfo.getSigningCertificateHistory();
                                if (signatures != null) {
                                    sign.historyMD5 = new String[signatures.length];
                                    sign.historySHA1 = new String[signatures.length];
                                    sign.historySHA256 = new String[signatures.length];
                                    for (int i = 0; i < signatures.length; i++) {
                                        byte[] data = signatures[i].toByteArray();
                                        sign.historyMD5[i] = SignUtil.getMD5(data);
                                        sign.historySHA1[i] = SignUtil.getSHA1(data);
                                        sign.historySHA256[i] = SignUtil.getSHA256(data);
                                    }
                                }
                            }
                        } else {
                            PackageInfo info;
                            if (file) {
                                info = weakContext.get().getPackageManager().getPackageArchiveInfo(packageName, PackageManager.GET_SIGNATURES);
                            } else {
                                info = weakContext.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                            }
                            Signature[] signatures = info.signatures;
                            if (signatures != null) {
                                sign.md5 = new String[signatures.length];
                                sign.sha1 = new String[signatures.length];
                                sign.sha256 = new String[signatures.length];
                                for (int i = 0; i < signatures.length; i++) {
                                    byte[] data = signatures[i].toByteArray();
                                    sign.md5[i] = SignUtil.getMD5(data);
                                    sign.sha1[i] = SignUtil.getSHA1(data);
                                    sign.sha256[i] = SignUtil.getSHA256(data);
                                }
                            }
                        }
                        return sign;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }


    public Disposable getFiles(File parent, KWSubscriber<List<FileItem>> subscriber) {
        return Flowable.just(parent)
                .map(new Function<File, List<FileItem>>() {
                    @Override
                    public List<FileItem> apply(File parent) throws Exception {
                        File[] files = parent.listFiles();
                        List<FileItem> fileItems = new ArrayList<>();
                        if (parent.getParent() != null /*&& !parent.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())*/) {
                            FileItem item = new FileItem(new File(parent, ".."));
                            item.isDirectory = true;
                            fileItems.add(item);
                        } else if (files == null || files.length == 0) {
                            throw new NullPointerException("the files is null or 0");
                        }
                        if (files != null) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    FileItem item = new FileItem(file);
                                    item.isDirectory = true;
                                    fileItems.add(item);
                                } else if (FileUtil.isApk(file)) {
                                    FileItem item = new FileItem(file);
                                    item.isDirectory = false;
                                    fileItems.add(item);
                                }
                            }
                            Collections.sort(fileItems, new Comparator<FileItem>() {
                                @Override
                                public int compare(FileItem lhs, FileItem rhs) {
                                    if (lhs.isDirectory && !rhs.isDirectory)
                                        return -1;
                                    else if (!lhs.isDirectory && rhs.isDirectory)
                                        return 1;
                                    return lhs.name.compareToIgnoreCase(rhs.name);
                                }
                            });
                        }
                        return fileItems;
                    }

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }

    public Disposable saveApk(App app, final String dest, final KWSubscriber<String> subscriber) {
        return Flowable.just(app)
                .map(new Function<App, String>() {
                    @Override
                    public String apply(App source) throws Exception {
                        String fileName = source.name + "_" + source.versionName + ".apk";
                        return FileUtil.copy(source.apkPath, dest, fileName, new OnCopyListener() {
                            @Override
                            public void inProgress(final float progress) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        subscriber.inProgress(progress);
                                    }
                                });
                            }
                        }).getAbsolutePath();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
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
}
