package com.wang.getapk;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Author: wangxiaojie6
 * Date: 2019/1/19
 */
public class KWApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }
}
