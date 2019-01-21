package com.wang.getapk.view.dialog;

import android.content.Context;

import com.wang.getapk.R;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class ProgressDialog extends BaseDialog<ProgressDialog.Builder> {

    private ProgressDialog(Builder builder) {
        super(builder);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_progress;
    }

    @Override
    protected void afterView(Context context, Builder builder) {

    }

    public static class Builder extends BaseBuilder<Builder> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        @UiThread
        public ProgressDialog build() {
            return new ProgressDialog(this);
        }

        @UiThread
        public ProgressDialog show() {
            ProgressDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }
}
