package com.wang.getapk.repository;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.ArrayMap;

import androidx.documentfile.provider.DocumentFile;

import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.constant.MimeType;
import com.wang.getapk.model.App;
import com.wang.getapk.model.FileItem;
import com.wang.getapk.model.Sign;
import com.wang.getapk.util.FileUtil;
import com.wang.getapk.util.SignUtil;
import com.wang.getapk.view.listener.OnCopyListener;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * Author: wang
 * Date: 2018/1/11
 */

public class LocalRepository {

    private static volatile LocalRepository sInstance;

    private List<App> mApps = Collections.emptyList();

    private final Handler mHandler;
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
        return Flowable.just(context)
                .map(new Function<Context, List<App>>() {
                    @Override
                    public List<App> apply(Context context) throws Exception {
                        final PackageManager pm = context.getPackageManager();
                        List<PackageInfo> infos = pm.getInstalledPackages(0);
                        mLock.writeLock().lock();
                        List<App> apps = new ArrayList<>(infos.size());
                        try {
                            for (PackageInfo info : infos) {
                                App app = new App(info, pm);
                                app.isFormFile = false;
                                Date date = new Date(info.lastUpdateTime);
                                app.time = dateFormat.format(date);
                                apps.add(app);
                            }
                            mApps = Collections.unmodifiableList(apps);
                        } finally {
                            mLock.writeLock().unlock();
                        }
                        return apps;
                    }
                });
    }

    public Disposable getAndSort(Context context, final boolean sortByTime, final DateFormat dateFormat, KWSubscriber<ItemArray> subscriber) {
        return Flowable.just(context)
                .flatMap(new Function<Context, Publisher<List<App>>>() {
                    @Override
                    public Publisher<List<App>> apply(Context context) throws Exception {
                        List<App> apps = getApps();
                        if (apps == null || apps.isEmpty()) {
                            return getApps(context, dateFormat);
                        } else {
                            return Flowable.just(apps);
                        }
                    }
                })
                .map(new SortFunction(sortByTime))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }

    public Disposable getApp(Context context, final Uri path, KWSubscriber<App> subscriber) {
        return Flowable.just(context)
                .map(it -> {
                    PackageManager pm = it.getPackageManager();
                    String realPath;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        File file = new File(it.getExternalCacheDir(), "temp.apk");
                        file.deleteOnExit();
                        realPath = file.getAbsolutePath();
                        FileUtil.copy(it.getContentResolver(), path, Uri.fromFile(new File(realPath)), null, null);
                    } else {
                        realPath = FileUtil.getPath(it, path);
                    }
                    PackageInfo info;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        info = pm.getPackageArchiveInfo(realPath, PackageManager.PackageInfoFlags.of(0));
                    } else {
                        info = pm.getPackageArchiveInfo(realPath, 0);
                    }
                    if (info == null) {
                        throw new NullPointerException();
                    }
                    ApplicationInfo applicationInfo = info.applicationInfo;
                    applicationInfo.sourceDir = realPath;
                    applicationInfo.publicSourceDir = realPath;
                    App app = new App(info, pm);
                    app.isFormFile = true;
                    return app;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }

    @SuppressLint("PackageManagerGetSignatures")
    public Disposable getSignature(PackageManager pm, final String packageName, final boolean file, KWSubscriber<Sign> subscriber) {
        return Flowable.just(pm)
                .map(new Function<PackageManager, Sign>() {
                    @Override
                    public Sign apply(PackageManager pm) throws Exception {

                        Sign sign = new Sign();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            PackageInfo info;
                            if (file) {
                                info = pm.getPackageArchiveInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                                if (info.signingInfo == null) {
                                    info = pm.getPackageArchiveInfo(packageName, PackageManager.GET_SIGNATURES);
                                }
                            } else {
                                info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                            }
                            if (info == null) {
                                throw new NullPointerException();
                            }
                            SigningInfo signingInfo = info.signingInfo;
                            Signature[] signatures = signingInfo == null ? info.signatures : signingInfo.getApkContentsSigners();
                            if (signatures != null) {
                                sign.md5 = new String[signatures.length][2];
                                sign.sha1 = new String[signatures.length][2];
                                sign.sha256 = new String[signatures.length][2];
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
                                    sign.historyMD5 = new String[signatures.length][2];
                                    sign.historySHA1 = new String[signatures.length][2];
                                    sign.historySHA256 = new String[signatures.length][2];
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
                                info = pm.getPackageArchiveInfo(packageName, PackageManager.GET_SIGNATURES);
                            } else {
                                info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                            }
                            Signature[] signatures = info.signatures;
                            if (signatures != null) {
                                sign.md5 = new String[signatures.length][2];
                                sign.sha1 = new String[signatures.length][2];
                                sign.sha256 = new String[signatures.length][2];
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

    public Disposable saveApk(ContentResolver resolver, App app, final Uri dest, final KWSubscriber<Uri> subscriber) {
        return Flowable.just(app)
                .map(new Function<App, Uri>() {
                    @Override
                    public Uri apply(App source) throws Exception {
                        FileUtil.copy(resolver, Uri.fromFile(new File(source.apkPath)), dest, subscriber, new OnCopyListener() {
                            @Override
                            public void inProgress(final float progress) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        subscriber.inProgress(progress);
                                    }
                                });
                            }
                        });
                        return dest;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);

    }

    public Disposable saveApks(Context context, List<App> apps, final Uri dest, final KWSubscriber<Uri> subscriber) {
        final DocumentFile dir = DocumentFile.fromTreeUri(context, dest);
        if (dir == null || !dir.canWrite()) {
            subscriber.onError(new IllegalStateException("can not write"));
            return subscriber;
        }
        return Flowable.just(apps)
                .map(new Function<List<App>, Uri>() {
                    @Override
                    public Uri apply(List<App> apps) throws Throwable {
                        ContentResolver resolver = context.getContentResolver();
                        for (App app : apps) {
                            if (subscriber.isDisposed()) {
                                break;
                            }
                            DocumentFile newFile = dir.createFile(MimeType.APK, app.getSaveName());
                            try {
                                if (newFile == null) {
                                    throw new IllegalStateException("can not create file");
                                }
                                FileUtil.copy(resolver, Uri.fromFile(new File(app.apkPath)), newFile.getUri(), subscriber, null);
                            } catch (Throwable e) {
                                newFile.delete();
                                throw e;
                            }
                        }
                        return dir.getUri();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(subscriber);
    }


    public Uri getDownloadUri(ContentResolver resolver, String fileName, String mimeType, String relativePath) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + relativePath);
            Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            return resolver.insert(external, values);
        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), relativePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            return Uri.fromFile(new File(file, fileName));
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

    public int appSize() {
        mLock.readLock().lock();
        try {
            return mApps.size();
        } finally {
            mLock.readLock().unlock();
        }
    }

    public void clearApps() {
        mLock.writeLock().lock();
        try {
            mApps = Collections.emptyList();
        } finally {
            mLock.writeLock().unlock();
        }
    }

}
