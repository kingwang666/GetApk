package com.wang.getapk.view.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.wang.getapk.R;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2017/12/28
 */

public abstract class BaseDialog<Builder extends BaseBuilder> extends AppCompatDialog {

    public static final int POSITIVE = 1;
    public static final int NEUTRAL = 2;
    public static final int NEGATIVE = 3;

    @IntDef({POSITIVE, NEUTRAL, NEGATIVE})
    public @interface DialogAction {
    }

    @Nullable
    @BindView(R.id.title_tv)
    AppCompatTextView mTitleTV;
    @Nullable
    @BindView(R.id.neutral_btn)
    AppCompatButton mNeutralBtn;
    @Nullable
    @BindView(R.id.negative_btn)
    AppCompatButton mNegativeBtn;
    @Nullable
    @BindView(R.id.positive_btn)
    AppCompatButton mPositiveBtn;


    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable;
    Builder mBuilder;

    BaseDialog(Builder builder) {
        super(builder.context);
        mBuilder = builder;
        mCompositeDisposable = new CompositeDisposable();
        setCancelable(builder.cancelable);
        setCanceledOnTouchOutside(builder.canceledOnTouchOutside);
        setOnShowListener(builder.showListener);
        setOnDismissListener(builder.dismissListener);
        setOnCancelListener(builder.cancelListener);
        setOnKeyListener(builder.keyListener);
        getWindow().setSoftInputMode(mBuilder.softInputMode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
    }

    @Override
    public void show() {
        super.show();
        mUnbinder = ButterKnife.bind(this);
        initCommonView();
        initViewListener();
        afterView(mBuilder.context, mBuilder);
    }

    private void initCommonView() {
        if (mTitleTV != null) {
            if (mBuilder.titleColorSet) {
                mTitleTV.setTextColor(mBuilder.titleColor);
            }
            mTitleTV.setText(mBuilder.title);
        }
        setButton(mPositiveBtn, mBuilder.positive);
        setButton(mNegativeBtn, mBuilder.negative);
        setButton(mNeutralBtn, mBuilder.neutral);
    }

    private void setButton(Button button, CharSequence name) {
        if (button != null) {
            if (TextUtils.isEmpty(name)) {
                button.setVisibility(View.GONE);
            } else {
                button.setVisibility(View.VISIBLE);
                button.setText(name);
            }
        }
    }

    protected abstract int getLayoutId();

    protected void initViewListener() {

    }

    protected abstract void afterView(Context context, Builder builder);

    @Optional
    @OnClick(R.id.neutral_btn)
    public void onNeutral() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onNeutralListener != null) {
            mBuilder.onNeutralListener.onClick(this, NEUTRAL);
        }
    }

    @Optional
    @OnClick(R.id.negative_btn)
    public void onNegative() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onNegativeListener != null) {
            mBuilder.onNegativeListener.onClick(this, NEGATIVE);
        }
    }

    @Optional
    @OnClick(R.id.positive_btn)
    public void onPositive() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onPositiveListener != null) {
            mBuilder.onPositiveListener.onClick(this, POSITIVE);
        }
    }

    public Builder getBuilder() {
        return mBuilder;
    }



    /**
     * 插入到观察者集合
     *
     * @param disposable
     */
    public void addDisposable(Disposable disposable) {
        if (disposable != null) {
            if (mCompositeDisposable != null) {
                mCompositeDisposable.add(disposable);
            } else {
                disposable.dispose();
            }
        }
    }

    public void removeDisposable(Disposable disposable) {
        if (disposable != null) {
            if (mCompositeDisposable != null) {
                mCompositeDisposable.remove(disposable);
            } else if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    /**
     * 获取观察者集合
     *
     * @return
     */
    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
        mUnbinder.unbind();
    }

    public interface OnButtonClickListener {

        void onClick(@NonNull BaseDialog dialog, @DialogAction int which);

    }
}
