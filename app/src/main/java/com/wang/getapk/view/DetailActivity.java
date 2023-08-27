package com.wang.getapk.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.palette.graphics.Palette;

import com.wang.getapk.R;
import com.wang.getapk.databinding.ActivityDetailBinding;
import com.wang.getapk.model.App;
import com.wang.getapk.model.Sign;
import com.wang.getapk.presenter.DetailActivityPresenter;
import com.wang.getapk.util.AndroidVersionHelper;
import com.wang.getapk.util.DrawableHelper;
import com.wang.getapk.util.SizeUtil;
import com.wang.getapk.view.dialog.BaseDialog;
import com.wang.getapk.view.dialog.NumberProgressDialog;
import com.wang.getapk.view.widget.InfoItemView;

import java.io.File;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/12/29
 */
public class DetailActivity extends BaseActivity<ActivityDetailBinding> implements
        DetailActivityPresenter.IView {

    private App mApp;
    private DetailActivityPresenter mPresenter;
    private Disposable mDisposable;
    private Disposable mSaveDisposable;
    private NumberProgressDialog mDialog;
    private boolean mCollapsed;

    private final ActivityResultLauncher<String> mCreateApkLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/vnd.android.package-archive"), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result == null) {
                return;
            }
            if (mDialog != null) {
                dismissDialog();
            }
            mDialog = new NumberProgressDialog.Builder(DetailActivity.this)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .title(R.string.copying)
                    .negative(R.string.cancel)
                    .onNegative(new BaseDialog.OnButtonClickListener() {
                        @Override
                        public void onClick(@NonNull BaseDialog<?, ?> dialog, int which) {
                            if (mSaveDisposable != null && !mSaveDisposable.isDisposed()) {
                                mSaveDisposable.dispose();
                            }
                        }
                    }).show();
            mSaveDisposable = mPresenter.saveApk(DetailActivity.this, mApp, result);
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityDetailBinding.inflate(getLayoutInflater()));
        initView();

        mApp = getIntent().getParcelableExtra("app");

        mPresenter = new DetailActivityPresenter(this);
        mDisposable = mPresenter.getSignature(this, mApp.isFormFile ? mApp.apkPath : mApp.packageName, mApp.isFormFile);

        final Drawable drawable = mApp.applicationInfo.loadIcon(getPackageManager());
        viewBinding.logoImg.setImageDrawable(drawable);
        viewBinding.toolbar.setTitle(mApp.name);
        viewBinding.toolbar.inflateMenu(R.menu.menu_detail);
        if (drawable instanceof BitmapDrawable) {
            setColors(((BitmapDrawable) drawable).getBitmap(), false);
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            canvas.setBitmap(null);
            setColors(bitmap, true);
        }
        viewBinding.toolbar.setOnMenuItemClickListener(item -> {
            if (!mApp.isFormFile) {
                createApk(mApp.name + "_" + mApp.versionName + ".apk");
            }
            return true;
        });
        viewBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (-verticalOffset >= appBarLayout.getTotalScrollRange()) {
                if (!mCollapsed) {
                    viewBinding.toolbar.getMenu().getItem(0).setVisible(true);
                    mCollapsed = true;
                }

            } else {
                if (mCollapsed) {
                    viewBinding.toolbar.getMenu().getItem(0).setVisible(false);
                    mCollapsed = false;
                }
            }

        });
        viewBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        viewBinding.packageItemView.setText(mApp.packageName);
        ComponentName name = null;
        if (mApp.launch != null) {
            name = mApp.launch.getComponent();
        }
        viewBinding.launchItemView.setText(name == null ? "null" : name.getClassName());
        viewBinding.compileSdkItemView.setText(String.format(Locale.getDefault(), "%d (%s)", mApp.compileSdkVersion, AndroidVersionHelper.getName(mApp.compileSdkVersion)));
        viewBinding.targetSdkItemView.setText(String.format(Locale.getDefault(), "%d (%s)", mApp.targetSdkVersion, AndroidVersionHelper.getName(mApp.targetSdkVersion)));
        viewBinding.minSdkItemView.setText(String.format(Locale.getDefault(), "%d (%s)", mApp.minSdkVersion, AndroidVersionHelper.getName(mApp.minSdkVersion)));
        viewBinding.versionItemView.setText(String.valueOf(mApp.versionCode));
        viewBinding.versionNameItemView.setText(mApp.versionName);
        viewBinding.timeItemView.setText(mApp.time);

        viewBinding.releaseItemView.setText(String.valueOf(mApp.isDebug));
        viewBinding.systemItemView.setText(String.valueOf(mApp.isSystem));

        viewBinding.pathItemView.setText(mApp.apkPath);
        viewBinding.sizeItemView.setText(Formatter.formatFileSize(this, new File(mApp.apkPath).length()));
    }

    private void initView() {
        viewBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mApp.isFormFile) {
                    createApk(mApp.name + "_" + mApp.versionName + ".apk");
                }
            }
        });

        viewBinding.logoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApp.launch != null && !mApp.isFormFile) {
                    startActivity(mApp.launch);
                }
            }
        });

        viewBinding.logoImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mApp.isFormFile) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", mApp.packageName, null));
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    private void setColors(Bitmap bitmap, boolean recycle) {
        Palette.from(bitmap).generate(palette -> {
            if (recycle) {
                bitmap.recycle();
            }
            if (palette != null) {
                Palette.Swatch swatch = palette.getDominantSwatch();
                if (swatch != null) {
                    int color = swatch.getRgb();
                    viewBinding.toolbarLayout.setBackgroundColor(color);
                    viewBinding.toolbarLayout.setContentScrimColor(color);
                    viewBinding.toolbarLayout.setStatusBarScrimColor(color);
                    int titleColor = swatch.getTitleTextColor();
                    viewBinding.toolbar.setTitleTextColor(titleColor);
                    viewBinding.toolbar.setNavigationIcon(DrawableHelper.tintDrawable(this, R.drawable.ic_arrow_back_white_24dp, ColorStateList.valueOf(titleColor), null));
                    viewBinding.toolbarLayout.setExpandedTitleColor(titleColor);
                    viewBinding.toolbarLayout.setCollapsedTitleTextColor(titleColor);
                    viewBinding.toolbar.getMenu().getItem(0).setIcon(DrawableHelper.tintDrawable(this, R.drawable.ic_export, ColorStateList.valueOf(titleColor), null));
                }
                swatch = palette.getLightVibrantSwatch();
                if (swatch == null) {
                    swatch = palette.getVibrantSwatch();
                }
                if (swatch == null) {
                    swatch = palette.getDarkVibrantSwatch();
                }
                if (swatch != null) {
                    ViewCompat.setBackgroundTintList(viewBinding.fab, ColorStateList.valueOf(swatch.getRgb()));
                    ImageViewCompat.setImageTintList(viewBinding.fab, ColorStateList.valueOf(swatch.getTitleTextColor()));
                }
            }
        });
    }

    private void createApk(String fileName) {
        mCreateApkLauncher.launch(fileName);
    }

    private void addChildView(String title, String value, boolean isModuleFirst) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(SizeUtil.dp2pxSize(this, 6));
        params.topMargin = SizeUtil.dp2pxSize(this, isModuleFirst ? 12 : 4);
        InfoItemView itemView = new InfoItemView(this);
        itemView.setTextIsSelectable(true);
        itemView.setTitle(title);
        itemView.setText(value);
        viewBinding.infoParent.addView(itemView, params);
    }

    private void addParentView(String str) {
        AppCompatTextView textView = new AppCompatTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = SizeUtil.dp2pxSize(this, 8);
        textView.setTextSize(15);
        textView.setText(str);
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        viewBinding.infoParent.addView(textView, params);
    }

    private void addParent2View(String str) {
        AppCompatTextView textView = new AppCompatTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = SizeUtil.dp2pxSize(this, 4);
        params.setMarginStart(SizeUtil.dp2pxSize(this, 6));
        textView.setTextSize(15);
        textView.setText(str);
        textView.setTextColor(ContextCompat.getColor(this, R.color.blue500));
        viewBinding.infoParent.addView(textView, params);
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void finishAfterTransition() {
        viewBinding.fab.setVisibility(View.GONE);
        super.finishAfterTransition();
    }

    @Override
    public void getSignatureSuccess(Sign sign) {
        if (sign.md5 != null) {
            for (int i = 0; i < sign.md5.length; i++) {
                addParent2View(getString(R.string.signature, i + 1));
                addChildView("MD5:", sign.md5[i][0], false);
                addChildView("MD5 Base64:", sign.md5[i][1], false);
                addChildView("SHA1:", sign.sha1[i][0], true);
                addChildView("SHA1 Base64:", sign.sha1[i][1], false);
                addChildView("SHA256:", sign.sha256[i][0], true);
                addChildView("SHA256 Base64:", sign.sha256[i][1], false);
            }
        }
        if (sign.hasHistory) {
            addParentView(getString(R.string.history_signing));
            for (int i = 0; i < sign.historyMD5.length; i++) {
                addParent2View(getString(R.string.signature, i + 1));
                addChildView("MD5:", sign.historyMD5[i][0], false);
                addChildView("MD5 Base64:", sign.historyMD5[i][1], false);
                addChildView("SHA1:", sign.historySHA1[i][0], true);
                addChildView("SHA1 Base64:", sign.historySHA1[i][1], false);
                addChildView("SHA256:", sign.historySHA256[i][0], true);
                addChildView("SHA256 Base64:", sign.historySHA256[i][1], false);
            }
        }
    }

    @Override
    public void getSignatureError(String error) {
        Toast.makeText(this, "error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inProgress(float progress) {
        if (mDialog != null) {
            mDialog.setProgress(Math.round(progress * 100));
        }
    }

    @Override
    public void saveSuccess(String path) {
        dismissDialog();
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "export apk file success", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "success: " + path, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void saveError(String message) {
        dismissDialog();
        Toast.makeText(this, "error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mSaveDisposable != null) {
            mSaveDisposable.dispose();
        }
        dismissDialog();
        super.onDestroy();
    }
}
