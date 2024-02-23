package com.wang.getapk.view.dialog;

import android.content.Context;
import android.view.View;

import com.wang.getapk.R;
import com.wang.getapk.databinding.DialogProgressBinding;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/26
 */

public class ProgressDialog extends BaseDialog<ProgressDialog.Builder, DialogProgressBinding> {

    private ProgressDialog(Builder builder) {
        super(builder);
    }

    @Override
    protected DialogProgressBinding getViewBinding() {
        return DialogProgressBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void afterView(Context context, Builder builder) {
        AppCompatTextView titleTV = viewBinding.getRoot().findViewById(R.id.title_tv);
        titleTV.setText(builder.title);
        setButton(viewBinding.negativeBtn, builder.negative);
        viewBinding.negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNegative();
            }
        });
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
