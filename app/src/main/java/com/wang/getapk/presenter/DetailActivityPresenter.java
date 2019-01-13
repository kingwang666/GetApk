package com.wang.getapk.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wang.getapk.model.App;
import com.wang.getapk.model.Sign;
import com.wang.getapk.util.SignUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Author: wangxiaojie6
 * Date: 2019/1/2
 */
public class DetailActivityPresenter {

    private IView mView;
    private Handler mHandler;

    public DetailActivityPresenter(IView view) {
        mView = view;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @SuppressLint("PackageManagerGetSignatures")
    public Disposable getSignature(Context context, String packageName, boolean file) {
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
                .subscribeWith(new DisposableSubscriber<Sign>() {
                    @Override
                    public void onNext(Sign sign) {
                        mView.getSignatureSuccess(sign);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("error", "", t);
                        mView.getSignatureError(t.toString());
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
                        try (InputStream in = new FileInputStream(file); OutputStream out = new FileOutputStream(dst)) {
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
                                mHandler.post(() -> mView.inProgress(finalSum * 1.0f / finalTotal));
                            }
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

    public interface IView {

        void getSignatureSuccess(Sign sign);

        void getSignatureError(String error);

        void inProgress(float progress);

        void saveSuccess(String path);

        void saveError(String message);

    }
}
