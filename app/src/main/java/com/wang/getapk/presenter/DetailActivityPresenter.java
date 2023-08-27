package com.wang.getapk.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.wang.getapk.model.App;
import com.wang.getapk.model.Sign;
import com.wang.getapk.repository.KWSubscriber;
import com.wang.getapk.repository.LocalRepository;
import com.wang.getapk.util.FileUtil;

import java.io.FileNotFoundException;

import io.reactivex.rxjava3.disposables.Disposable;


/**
 * Author: wangxiaojie6
 * Date: 2019/1/2
 */
public class DetailActivityPresenter {

    private final IView mView;
    private final LocalRepository mRepository;

    public DetailActivityPresenter(IView view) {
        mView = view;
        mRepository = LocalRepository.getInstance();
    }

    public Disposable getSignature(Context context, String packageName, boolean file) {
        return mRepository.getSignature(context.getPackageManager(), packageName, file, new KWSubscriber<Sign>() {
            @Override
            public void success(Sign sign) {
                mView.getSignatureSuccess(sign);
            }

            @Override
            public void error(int code, String error) {
                mView.getSignatureError(error);
            }
        });
    }

    public Disposable saveApk(Context context, App app, Uri dest) {
        ContentResolver resolver = context.getContentResolver();
        return mRepository.saveApk(resolver, app, dest, new KWSubscriber<Uri>() {

            @Override
            public void success(Uri uri) {
                mView.saveSuccess(FileUtil.getPath(context, uri));
            }

            @Override
            public void error(int code, String error) {
                try {
                    DocumentsContract.deleteDocument(resolver, dest);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mView.saveError(error);
            }

            @Override
            public void inProgress(float progress) {
                mView.inProgress(progress);
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
