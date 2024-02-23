package com.wang.getapk.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.TransitionManager;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.wang.baseadapter.StickyHeaderDecoration;
import com.wang.baseadapter.listener.OnHeaderClickListener;
import com.wang.baseadapter.listener.StickyHeaderTouchListener;
import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.baseadapter.widget.WaveSideBarView;
import com.wang.getapk.R;
import com.wang.getapk.constant.Key;
import com.wang.getapk.constant.MimeType;
import com.wang.getapk.databinding.ActivityMainBinding;
import com.wang.getapk.model.App;
import com.wang.getapk.presenter.MainActivityPresenter;
import com.wang.getapk.util.CommonPreference;
import com.wang.getapk.util.FileUtil;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.view.dialog.BaseDialog;
import com.wang.getapk.view.dialog.NumberProgressDialog;
import com.wang.getapk.view.dialog.ProgressDialog;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity<ActivityMainBinding>
        implements AppAdapter.OnAppClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        WaveSideBarView.OnTouchLetterChangeListener,
        OnHeaderClickListener,
        MainActivityPresenter.IView {

    private MainActivityPresenter mPresenter;
    private CompositeDisposable mDisposables;

    private ProgressDialog mDialog;

    private boolean mIsSortByTime = true;
    private Animation mShowAnim;
    private Animation mHideAnim;

    private List<App> mTempApps;
    private Disposable mSaveDisposable;


    private final ActivityResultLauncher<String> mGetApkLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result == null) {
                return;
            }
            MainActivityPermissionsDispatcher.getAppWithPermissionCheck(MainActivity.this, result);
        }
    });

    private final ActivityResultLauncher<Uri> mSaveFileLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result == null) {
                mTempApps = null;
                return;
            }
            if (mTempApps == null){
                return;
            }
            if (mDialog != null) {
                mDialog.dismiss();
            }
            mDialog = new ProgressDialog.Builder(MainActivity.this)
                    .title(R.string.saveing)
                    .cancelable(false)
                    .negative(R.string.cancel)
                    .onNegative(new BaseDialog.OnButtonClickListener() {
                        @Override
                        public void onClick(@NonNull BaseDialog<?, ?> dialog, int which) {
                            if (mSaveDisposable != null) {
                                mSaveDisposable.dispose();
                            }
                        }
                    })
                    .show();
            if (mSaveDisposable != null){
                mSaveDisposable.dispose();
            }
            mSaveDisposable = mPresenter.saveApps(getApplicationContext(), mTempApps, result);
            mTempApps = null;
        }
    });

    private final OnBackPressedCallback mOnBackPressedCallback =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    AppAdapter adapter = (AppAdapter) viewBinding.recyclerView.getAdapter();
                    if (adapter != null && adapter.isSelectMode()) {
                        onMultiChoice();
                    } else {
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityMainBinding.inflate(getLayoutInflater()));
        mIsSortByTime = CommonPreference.getBoolean(this, Key.KEY_SORT, mIsSortByTime);
        mPresenter = new MainActivityPresenter(this);
        mDisposables = new CompositeDisposable();
        setSupportActionBar(viewBinding.toolbar);
        getOnBackPressedDispatcher().addCallback(this, mOnBackPressedCallback);

        viewBinding.refreshView.setOnRefreshListener(this);
        viewBinding.refreshView.setColorSchemeResources(R.color.blue300, R.color.red300, R.color.green300);

        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        StickyHeaderDecoration decoration = new StickyHeaderDecoration(AppAdapter.TYPE_STICKY);
        viewBinding.recyclerView.addItemDecoration(decoration);
        viewBinding.recyclerView.addOnItemTouchListener(new StickyHeaderTouchListener(this, decoration, this));
        viewBinding.recyclerView.setVerticalScrollBarEnabled(mIsSortByTime);
        viewBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    if (!mIsSortByTime) {
                        viewBinding.sideBarView.hide();
                    }
                }
            }
        });

        viewBinding.selectView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                viewBinding.recyclerView.setPadding(0, 0, 0, viewBinding.selectView.getHeight());
            }
        });
        viewBinding.allCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getTag() instanceof Boolean && (Boolean) buttonView.getTag()) {
                    return;
                }
                AppAdapter adapter = (AppAdapter) viewBinding.recyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                if (isChecked) {
                    mPresenter.selectAll(adapter);
                } else {
                    mPresenter.clearSelected(adapter);
                }
            }
        });
        viewBinding.exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.saveAppsWithPermissionCheck(MainActivity.this, mPresenter.peekSelectedApps());
            }
        });

        viewBinding.sideBarView.setOnTouchLetterChangeListener(this);

        viewBinding.refreshView.setRefreshing(true);
        onRefresh();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(value = Manifest.permission.READ_EXTERNAL_STORAGE, maxSdkVersion = Build.VERSION_CODES.S_V2)
    public void getApp(Uri uri) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new ProgressDialog.Builder(MainActivity.this)
                .cancelable(false)
                .title(R.string.parsing)
                .show();
        mDisposables.add(mPresenter.getApp(getApplicationContext(), uri));
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
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

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void storageDenied() {
        Toast.makeText(this, getString(R.string.error_storage), Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE, maxSdkVersion = Build.VERSION_CODES.S_V2)
    public void saveApps(List<App> apps) {
        mSaveFileLauncher.launch(null);
        mTempApps = apps;
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void showWriteStorageRationale(final PermissionRequest request) {
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

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void writeStorageDenied() {
        Toast.makeText(this, getString(R.string.error_storage), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRefresh() {
        onRefresh(true);
    }

    private void onRefresh(boolean clearApps) {
        if (clearApps) {
            mPresenter.clearApps();
        }
        MenuItem menuItem = viewBinding.toolbar.getMenu().findItem(R.id.sort);
        if (menuItem != null) {
            menuItem.setEnabled(false);
        }
        mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.sort);
        menuItem.setEnabled(!viewBinding.refreshView.isRefreshing());
        menuItem.setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.apk) {
            mGetApkLauncher.launch(MimeType.APK);
            return true;
        }
        if (id == R.id.multi_choice) {
            onMultiChoice();
            return true;
        }
        if (id == R.id.sort) {
            mIsSortByTime = !mIsSortByTime;
            item.setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
            viewBinding.refreshView.setRefreshing(true);
            onRefresh(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMultiChoice() {
        AppAdapter adapter = (AppAdapter) viewBinding.recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setSelectMode(!adapter.isSelectMode());
            if (adapter.isSelectMode()) {
                mOnBackPressedCallback.setEnabled(true);
                if (mShowAnim == null) {
                    mShowAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
                    mShowAnim.setInterpolator(new AccelerateInterpolator());
                }
                viewBinding.selectView.startAnimation(mShowAnim);
                viewBinding.selectView.setVisibility(View.VISIBLE);
            } else {
                mOnBackPressedCallback.setEnabled(false);
                mPresenter.clearSelected();
                if (mHideAnim == null) {
                    mHideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
                    mHideAnim.setInterpolator(new AccelerateInterpolator());
                }
                viewBinding.selectView.startAnimation(mHideAnim);
                viewBinding.selectView.setVisibility(View.GONE);
                viewBinding.recyclerView.setPadding(0, 0, 0, 0);
            }
        }
    }


    @Override
    public void onLetterChange(String letter) {
        if (viewBinding.recyclerView.getAdapter() == null) {
            return;
        }
        ItemArray itemArray = ((AppAdapter) viewBinding.recyclerView.getAdapter()).getItems();
        int size = itemArray.size();
        for (int i = 0; i < size; i++) {
            ItemData data = itemArray.get(i);
            if (data.getDataType() == AppAdapter.TYPE_STICKY) {
                App app = data.getData();
                if (app.namePinyin.startsWith(letter)) {
                    LinearLayoutManager mLayoutManager =
                            (LinearLayoutManager) viewBinding.recyclerView.getLayoutManager();
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                    return;
                }
            }
        }
    }

    @Override
    public void onHeader(int viewType, int position) {

    }

    @Override
    public void onDetail(App app, View iconImg) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("app", app);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, iconImg, "logo_img").toBundle());
    }

    @Override
    public void onSettings(App app) {
        if (!app.isFormFile) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", app.packageName, null));
            startActivity(intent);
        }
    }

    @Override
    public void onSelectChanged(App app, boolean isChecked, int position) {
        if (isChecked) {
            mPresenter.addSelected(position, app);
        } else {
            mPresenter.removeSelected(app);
        }
    }


    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void getAppsSuccess(ItemArray apps, boolean sortByTime) {
        viewBinding.refreshView.setRefreshing(false);
        viewBinding.toolbar.getMenu().findItem(R.id.sort).setEnabled(true);
        CommonPreference.putBoolean(this, Key.KEY_SORT, sortByTime);
        viewBinding.recyclerView.setVerticalScrollBarEnabled(sortByTime);
        if (sortByTime) {
            viewBinding.sideBarView.setVisibility(View.GONE);
        } else {
            viewBinding.sideBarView.setVisibility(View.VISIBLE);
            viewBinding.sideBarView.showAfterHide();
        }
        if (viewBinding.recyclerView.getAdapter() == null) {
            AppAdapter adapter = new AppAdapter(apps, this);
            viewBinding.recyclerView.setAdapter(adapter);
        } else {
            ((AppAdapter) viewBinding.recyclerView.getAdapter()).setItems(apps);
        }
    }

    @Override
    public void getAppsError(String message) {
        viewBinding.refreshView.setRefreshing(false);
        viewBinding.toolbar.getMenu().getItem(1).setEnabled(true);
        Toast.makeText(this, "error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getAppSuccess(App app) {
        dismissDialog();
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("app", app);
        startActivity(intent);
    }

    @Override
    public void getAppError(String error) {
        dismissDialog();
        Toast.makeText(this, "error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectedChanged(int selectedCount, int allCount) {
        viewBinding.allCheckbox.setText(getString(R.string.select_all, selectedCount, allCount));
        viewBinding.allCheckbox.setTag(true);
        if (selectedCount == 0) {
            viewBinding.exportBtn.setEnabled(false);
            viewBinding.allCheckbox.setCheckedState(MaterialCheckBox.STATE_UNCHECKED);
        } else if (selectedCount == allCount) {
            viewBinding.exportBtn.setEnabled(true);
            viewBinding.allCheckbox.setCheckedState(MaterialCheckBox.STATE_CHECKED);
        } else {
            viewBinding.exportBtn.setEnabled(true);
            viewBinding.allCheckbox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);
        }
        viewBinding.allCheckbox.setTag(null);
    }

    @Override
    public void saveSuccess(Uri uri) {
        dismissDialog();
        String path = FileUtil.getPath(this, uri);
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
        mDisposables.clear();
        dismissDialog();
        super.onDestroy();
    }


}
