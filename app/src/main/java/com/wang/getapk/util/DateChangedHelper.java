package com.wang.getapk.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.wang.getapk.model.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/19/21
 * Author: wangxiaojie
 * Description:
 */
public final class DateChangedHelper {

    private static volatile DateChangedHelper sInstance;

    private DateChangedReceiver mReceiver;

    private List<OnDateChangedListener> mListeners;

    public static DateChangedHelper getInstance() {
        DateChangedHelper helper = sInstance;
        if (helper == null) {
            synchronized (DateChangedHelper.class) {
                helper = sInstance;
                if (helper == null) {
                    helper = new DateChangedHelper();
                    sInstance = helper;
                }
            }
        }
        return helper;
    }

    private DateChangedHelper() {

    }

    public void addOnDateChangedListener(Context context, OnDateChangedListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
        if (mReceiver == null) {
            mReceiver = new DateChangedReceiver();
        }
        mReceiver.registerReceiver(context);
    }

    public void removeOnDateChangedListener(Context context, OnDateChangedListener listener) {
        if (mListeners == null) {
            return;
        }
        if (mListeners.remove(listener) && mReceiver != null && mListeners.isEmpty()) {
            mReceiver.unregisterReceiver(context);
            mReceiver = null;
        }
    }

    public void clearOnDateChangedListeners(Context context) {
        if (mListeners == null) {
            return;
        }
        mListeners.clear();
        if (mReceiver != null) {
            mReceiver.unregisterReceiver(context);
            mReceiver = null;
        }
    }

    interface OnDateChangedListener {

        void onDateChanged();

    }

    public class DateChangedReceiver extends BroadcastReceiver {

        private boolean mIsRegisted = false;

        public void registerReceiver(Context context) {
            if (mIsRegisted) {
                return;
            }
            try {
                IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                context.registerReceiver(this, filter);
                mIsRegisted = true;
            } catch (IllegalArgumentException | IllegalStateException e) {
                e.printStackTrace();
                mIsRegisted = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListeners == null) {
                return;
            }
            for (OnDateChangedListener listener : mListeners) {
                listener.onDateChanged();
            }
        }

        public void unregisterReceiver(Context context) {
            if (!mIsRegisted) {
                return;
            }
            try {
                context.unregisterReceiver(this);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mIsRegisted = false;

        }
    }
}
