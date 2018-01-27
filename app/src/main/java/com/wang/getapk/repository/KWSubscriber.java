package com.wang.getapk.repository;


import android.util.Log;

import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Author: wang
 * Date: 2018/1/11
 */

public abstract class KWSubscriber<T> extends DisposableSubscriber<T> {

    private static final String TAG = KWSubscriber.class.getSimpleName();

    @Override
    public void onError(Throwable e) {
        try {
            Log.e(TAG, e.getMessage(), e);
            error(-2, e.getMessage());
        } catch (NullPointerException e1) {
            Log.e(TAG, e1.getMessage(), e1);
        }

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext(T t) {
        try {
            success(t);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public abstract void success(T t);

    public abstract void error(int code, String error);

    public void inProgress(float progress){

    }

}
