package com.wang.getapk.view.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.wang.getapk.databinding.DialogNumberProgressBinding;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class NumberProgressDialog extends BaseDialog<NumberProgressDialog.Builder, DialogNumberProgressBinding> {

    private NumberProgressDialog(Builder builder) {
        super(builder);
    }

    @Override
    protected DialogNumberProgressBinding getViewBinding() {
        return DialogNumberProgressBinding.inflate(getLayoutInflater());
    }


    public void setProgress(int progress){
        viewBinding.progressBar.setProgress(progress);
    }

    @Override
    protected void afterView(Context context, Builder builder) {

    }

    public static class Builder extends BaseBuilder<Builder> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        @UiThread
        public NumberProgressDialog build() {
            return new NumberProgressDialog(this);
        }

        @UiThread
        public NumberProgressDialog show() {
            NumberProgressDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }
}
