package com.wang.getapk.view.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatTextView;

import com.wang.getapk.R;
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
