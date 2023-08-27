package com.wang.getapk.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.wang.getapk.util.AutoGrayThemeHelper;
import com.wang.getapk.util.DateChangedHelper;
import com.wang.getapk.util.GrayThemeHelper;
import com.wang.getapk.util.IGrayChecker;

import java.util.Calendar;

/**
 * Created on 2020/4/9
 * Author: bigwang
 * Description:
 */
public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    @Nullable
    protected AutoGrayThemeHelper mGrayThemeHelper;

    protected T viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGrayThemeHelper = createGrayThemeHelper(getWindow().getDecorView(), this);
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.bindDateChangedReceiver();
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

    public void setContentView(T viewBinding) {
        this.viewBinding = viewBinding;
        super.setContentView(viewBinding.getRoot());
    }

    @Nullable
    protected AutoGrayThemeHelper createGrayThemeHelper(View view, Context context) {
        return new AutoGrayThemeHelper(view, context, AutoGrayThemeHelper.QINGMING_CHECKER);
    }

    protected final void applyOrRemoveGrayTheme() {
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.unbindDateChangedReceiver();
        }
    }

}
