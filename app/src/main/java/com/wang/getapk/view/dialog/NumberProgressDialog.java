package com.wang.getapk.view.dialog;

import android.content.Context;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.wang.getapk.R;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import butterknife.BindView;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class NumberProgressDialog extends BaseDialog<NumberProgressDialog.Builder> {

    @BindView(R.id.progress_bar)
    NumberProgressBar mProgressBar;

    private NumberProgressDialog(Builder builder) {
        super(builder);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_number_progress;
    }

    public void setProgress(int progress){
        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
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
