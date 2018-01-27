package com.wang.getapk.presenter;

import android.content.Context;

import com.wang.baseadapter.model.ItemArray;
import com.wang.getapk.model.App;
import com.wang.getapk.repository.KWSubscriber;
import com.wang.getapk.repository.LocalRepository;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class MainActivityPresenter {

    private IView mView;
    private LocalRepository mRepository;


    public MainActivityPresenter(IView view) {
        mView = view;
        mRepository = LocalRepository.getInstance();
    }



    public Disposable getAndSort(Context context, final boolean sortByTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mRepository.getAndSort(context.getApplicationContext(), sortByTime, dateFormat, new KWSubscriber<ItemArray>() {
            @Override
            public void success(ItemArray apps) {
                mView.getAppsSuccess(apps, sortByTime);
            }

            @Override
            public void error(int code, String error) {
                mView.getAppsError(error);
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

    public void clearApps() {
        mRepository.setApps(null);
    }


    public interface IView {

    void getAppsSuccess(ItemArray apps, boolean sortByTime);

    void getAppsError(String message);

    void inProgress(float progress);

    void saveSuccess(String path);

    void saveError(String message);

}
}
