package com.wang.getapk.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wang.getapk.R;
import com.wang.getapk.model.App;
import com.wang.getapk.model.Sign;
import com.wang.getapk.presenter.DetailActivityPresenter;
import com.wang.getapk.util.DrawableHelper;
import com.wang.getapk.util.SizeUtil;
import com.wang.getapk.view.dialog.BaseDialog;
import com.wang.getapk.view.dialog.FileExplorerDialog;
import com.wang.getapk.view.dialog.NumberProgressDialog;
import com.wang.getapk.view.listener.OnPathSelectListener;

import java.io.File;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Author: wangxiaojie6
 * Date: 2018/12/29
 */
@RuntimePermissions
public class DetailActivity extends AppCompatActivity implements DetailActivityPresenter.IView {

    @BindView(R.id.logo_img)
    AppCompatImageView mLogoImg;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBar;
    @BindView(R.id.package_tv)
    AppCompatTextView mPackageTV;
    @BindView(R.id.launch_tv)
    AppCompatTextView mLaunchTV;
    @BindView(R.id.version_tv)
    AppCompatTextView mVersionTV;
    @BindView(R.id.version_name_tv)
    AppCompatTextView mVersionNameTV;
    @BindView(R.id.time_tv)
    AppCompatTextView mTimeTV;
    @BindView(R.id.release_tv)
    AppCompatTextView mReleaseTV;
    @BindView(R.id.system_tv)
    AppCompatTextView mSystemTV;
    @BindView(R.id.path_tv)
    AppCompatTextView mPathTV;
    @BindView(R.id.size_tv)
    AppCompatTextView mSizeTV;
    @BindView(R.id.info_parent)
    LinearLayout mInfoParent;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private App mApp;
    private DetailActivityPresenter mPresenter;
    private Disposable mDisposable;
    private Disposable mSaveDisposable;
    private NumberProgressDialog mDialog;
    private boolean mCollapsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mApp = getIntent().getParcelableExtra("app");

        mPresenter = new DetailActivityPresenter(this);
        mDisposable = mPresenter.getSignature(this, mApp.isFormFile ? mApp.apkPath : mApp.packageName, mApp.isFormFile);

        final Drawable drawable = mApp.applicationInfo.loadIcon(getPackageManager());
        mLogoImg.setImageDrawable(drawable);
        mToolbar.setTitle(mApp.name);
        mToolbar.inflateMenu(R.menu.menu_detail);
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
        mToolbar.setOnMenuItemClickListener(item -> {
            onFab();
            return true;
        });
        mAppBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (-verticalOffset >= appBarLayout.getTotalScrollRange()) {
                if (!mCollapsed) {
                    mToolbar.getMenu().getItem(0).setVisible(true);
                    mCollapsed = true;
                }

            } else {
                if (mCollapsed) {
                    mToolbar.getMenu().getItem(0).setVisible(false);
                    mCollapsed = false;
                }
            }

        });
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mPackageTV.setText(String.format("Package Name: %s", mApp.packageName));
        ComponentName name = null;
        if (mApp.launch != null) {
            name = mApp.launch.getComponent();
        }
        mLaunchTV.setText(String.format("Launch: %s", name == null ? "null" : name.getClassName()));
        mVersionTV.setText(String.format(Locale.getDefault(), "Version Code: %d", mApp.versionCode));
        mVersionNameTV.setText(String.format("Version Name: %s", mApp.versionName));
        mTimeTV.setText(String.format("Time: %s", mApp.time));

        mReleaseTV.setText(String.format("Release: %s", (mApp.isDebug ? "false" : "true")));
        mSystemTV.setText(String.format("System: %s", (mApp.isSystem ? "true" : "false")));

        mPathTV.setText(String.format("APK Path: %s", mApp.apkPath));
        mSizeTV.setText(String.format("APK Size: %s", Formatter.formatFileSize(this, new File(mApp.apkPath).length())));
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
                    mToolbarLayout.setBackgroundColor(color);
                    mToolbarLayout.setContentScrimColor(color);
                    mToolbarLayout.setStatusBarScrimColor(color);
                    int titleColor = swatch.getBodyTextColor();
                    mToolbar.setTitleTextColor(titleColor);
                    mToolbar.setNavigationIcon(DrawableHelper.tintDrawable(this, R.drawable.ic_arrow_back_white_24dp, ColorStateList.valueOf(titleColor), null));
                    mToolbarLayout.setExpandedTitleColor(titleColor);
                    mToolbarLayout.setCollapsedTitleTextColor(titleColor);
                    mToolbar.getMenu().getItem(0).setIcon(DrawableHelper.tintDrawable(this, R.drawable.ic_export, ColorStateList.valueOf(titleColor), null));
                }
                swatch = palette.getDarkVibrantSwatch();
                if (swatch != null) {
                    mFab.setSupportBackgroundTintList(ColorStateList.valueOf(swatch.getRgb()));
                    mFab.setSupportImageTintList(ColorStateList.valueOf(swatch.getBodyTextColor()));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick(R.id.fab)
    public void onFab() {
        DetailActivityPermissionsDispatcher.showFileExplorerWithPermissionCheck(this, mApp);
    }

    @OnClick(R.id.logo_img)
    public void onLogo() {
        if (mApp.launch != null) {
            startActivity(mApp.launch);
        }
    }

    @NeedsPermission({
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    })
    public void showFileExplorer(final App app) {
        new FileExplorerDialog.Builder(this)
                .title(R.string.choose_save_path)
                .pathSelectListener(new OnPathSelectListener() {
                    @Override
                    public void onSelected(String path) {
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
                                    public void onClick(@NonNull BaseDialog dialog, int which) {
                                        if (mSaveDisposable != null && !mSaveDisposable.isDisposed()) {
                                            mSaveDisposable.dispose();
                                        }
                                    }
                                }).show();
                        mSaveDisposable = mPresenter.saveApk(app, path);
                    }
                })
                .show();
    }

    @OnShowRationale({
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    })
    public void showStorageRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.rationale_storage)
                .setTitle(R.string.warning)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();

    }

    @OnPermissionDenied({
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    })
    public void storageDenied() {
        Toast.makeText(this, getString(R.string.error_storage), Toast.LENGTH_SHORT).show();
    }

    private void addChildView(String str) {
        AppCompatTextView textView = new AppCompatTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(SizeUtil.dp2pxSize(this, 6));
        params.topMargin = SizeUtil.dp2pxSize(this, 4);
        textView.setTextIsSelectable(true);
        textView.setText(str);
        mInfoParent.addView(textView, params);
    }

    private void addParentView(String str) {
        AppCompatTextView textView = new AppCompatTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = SizeUtil.dp2pxSize(this, 8);
        textView.setTextSize(15);
        textView.setText(str);
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mInfoParent.addView(textView, params);
    }

    private void addParent2View(String str) {
        AppCompatTextView textView = new AppCompatTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = SizeUtil.dp2pxSize(this, 4);
        params.setMarginStart(SizeUtil.dp2pxSize(this, 6));
        textView.setTextSize(15);
        textView.setText(str);
        textView.setTextColor(ContextCompat.getColor(this, R.color.blue500));
        mInfoParent.addView(textView, params);
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void getSignatureSuccess(Sign sign) {
        if (sign.md5 != null) {
            for (int i = 0; i < sign.md5.length; i++) {
                addParent2View(getString(R.string.signature, i + 1));
                addChildView("MD5: " + sign.md5[i]);
                addChildView("SHA1: " + sign.sha1[i]);
                addChildView("SHA256: " + sign.sha256[i]);
            }
        }
        if (sign.hasHistory) {
            addParentView(getString(R.string.history_signing));
            for (int i = 0; i < sign.historyMD5.length; i++) {
                addParent2View(getString(R.string.signature, i + 1));
                addChildView("MD5: " + sign.historyMD5[i]);
                addChildView("SHA1: " + sign.historySHA1[i]);
                addChildView("SHA256: " + sign.historySHA256[i]);
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
        MediaScannerConnection.scanFile(this,
                new String[]{path},
                new String[]{"application/vnd.android.package-archive"},
                null);
        dismissDialog();
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
