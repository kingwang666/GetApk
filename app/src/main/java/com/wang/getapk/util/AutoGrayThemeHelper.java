package com.wang.getapk.util;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

/**
 * Created on 3/22/21
 * Author: wangxiaojie
 * Description:
 */
public class AutoGrayThemeHelper extends GrayThemeHelper implements DateChangedHelper.OnDateChangedListener {

    public static final IGrayChecker DEFAULT_CHECKER = new IGrayChecker() {
        @Override
        public boolean shouldGrayTheme() {
            return false;
        }
    };

    public static final IGrayChecker QINGMING_CHECKER = new IGrayChecker() {
        @Override
        public boolean shouldGrayTheme() {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 5;
        }
    };

    @NonNull
    View mView;
    @Nullable
    Context mContext;
    @NonNull
    private final IGrayChecker mChecker;

    public AutoGrayThemeHelper(@NonNull View view) {
        this(view, null);
    }

    public AutoGrayThemeHelper(@NonNull View view, @Nullable Context context) {
        this(view, context, DEFAULT_CHECKER);
    }


    public AutoGrayThemeHelper(@NonNull View view, @Nullable Context context, @NonNull IGrayChecker checker) {
        mView = view;
        mContext = context;
        mChecker = checker;
    }

    public void bindDateChangedReceiver() {
        if (mContext == null) {
            return;
        }
        DateChangedHelper.getInstance().addOnDateChangedListener(mContext.getApplicationContext(), this);
    }

    @Override
    public void onDateChanged() {
        applyOrRemoveGrayTheme();
    }

    public void applyOrRemoveGrayTheme(){
        if (mChecker.shouldGrayTheme()) {
            applyGrayTheme(mView);
        } else {
            removeGrayTheme(mView);
        }
    }

    public void unbindDateChangedReceiver() {
        if (mContext == null) {
            return;
        }
        DateChangedHelper.getInstance().removeOnDateChangedListener(mContext.getApplicationContext(), this);
    }

}
