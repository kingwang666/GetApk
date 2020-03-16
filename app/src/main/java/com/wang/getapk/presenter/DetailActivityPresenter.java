package com.wang.getapk.presenter;

import android.content.Context;

import com.wang.getapk.model.App;
import com.wang.getapk.model.Sign;
import com.wang.getapk.repository.KWSubscriber;
import com.wang.getapk.repository.LocalRepository;

import io.reactivex.rxjava3.disposables.Disposable;


/**
 * Author: wangxiaojie6
 * Date: 2019/1/2
 */
public class DetailActivityPresenter {

    private IView mView;
    private LocalRepository mRepository;

    public DetailActivityPresenter(IView view) {
        mView = view;
        mRepository = LocalRepository.getInstance();
    }

    public Disposable getSignature(Context context, String packageName, boolean file) {
        return mRepository.getSignature(context.getApplicationContext(), packageName, file, new KWSubscriber<Sign>() {
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

    public Disposable saveApk(App app, final String dest) {
        return mRepository.saveApk(app, dest, new KWSubscriber<String>() {
            @Override
            public void success(String path) {
                mView.saveSuccess(path);
            }

            @Override
            public void error(int code, String error) {
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
