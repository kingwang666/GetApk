package com.wang.getapk.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wang.baseadapter.StickyHeaderDecoration;
import com.wang.baseadapter.listener.OnHeaderClickListener;
import com.wang.baseadapter.listener.StickyHeaderTouchListener;
import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.baseadapter.widget.WaveSideBarView;
import com.wang.getapk.R;
import com.wang.getapk.constant.Key;
import com.wang.getapk.model.App;
import com.wang.getapk.presenter.MainActivityPresenter;
import com.wang.getapk.util.CommonPreference;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.view.dialog.BaseDialog;
import com.wang.getapk.view.dialog.FileExplorerDialog;
import com.wang.getapk.view.dialog.NumberProgressDialog;
import com.wang.getapk.view.dialog.ProgressDialog;
import com.wang.getapk.view.listener.OnPathSelectListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
        implements AppAdapter.OnAppClickListener,
        Toolbar.OnMenuItemClickListener,

        SwipeRefreshLayout.OnRefreshListener,
        WaveSideBarView.OnTouchLetterChangeListener,
        OnHeaderClickListener,
        MainActivityPresenter.IView {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout mRefreshView;
    @BindView(R.id.side_bar_view)
    WaveSideBarView mSideBarView;

    private MainActivityPresenter mPresenter;
    private CompositeDisposable mDisposables;
    private Disposable mDisposable;
    private NumberProgressDialog mNumDialog;
    private ProgressDialog mDialog;

    private boolean mIsSortByTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mIsSortByTime = CommonPreference.getBoolean(this, Key.KEY_SORT, mIsSortByTime);
        mPresenter = new MainActivityPresenter(this);
        mDisposables = new CompositeDisposable();

        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.getMenu().getItem(1).setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
        mToolbar.setOnMenuItemClickListener(this);
//        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setColorSchemeResources(R.color.blue300, R.color.red300, R.color.green300);
        mRefreshView.setRefreshing(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        StickyHeaderDecoration decoration = new StickyHeaderDecoration(AppAdapter.TYPE_STICKY);
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.addOnItemTouchListener(new StickyHeaderTouchListener(this, decoration, this));
        mRecyclerView.setVerticalScrollBarEnabled(mIsSortByTime);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE){
                    if (!mIsSortByTime){
                        mSideBarView.hide();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

            }
        });


//        mSideBarView.setVisibility(mIsSortByTime ? View.GONE : View.VISIBLE);
        mSideBarView.setOnTouchLetterChangeListener(this);

        onRefresh();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    })
    public void showFileExplorer(final App app, boolean selectFile) {
        new FileExplorerDialog.Builder(this)
                .selectFile(selectFile)
                .title(selectFile ? R.string.choose_apk : R.string.choose_save_path)
                .pathSelectListener(new OnPathSelectListener() {
                    @Override
                    public void onSelected(String path) {
                        dismissDialog();
                        if (!selectFile) {
                            mNumDialog = new NumberProgressDialog.Builder(MainActivity.this)
                                    .cancelable(false)
                                    .canceledOnTouchOutside(false)
                                    .title(R.string.copying)
                                    .negative(R.string.cancel)
                                    .onNegative(new BaseDialog.OnButtonClickListener() {
                                        @Override
                                        public void onClick(@NonNull BaseDialog dialog, int which) {
                                            if (mDisposable != null && !mDisposable.isDisposed()) {
                                                mDisposable.dispose();
                                            }
                                        }
                                    }).show();
                            mDisposable = mPresenter.saveApk(app, path);
                            mDisposables.add(mDisposable);
                        } else {
                            mDialog = new ProgressDialog.Builder(MainActivity.this)
                                    .title(R.string.parsing)
                                    .show();
                            mDisposables.add(mPresenter.getApp(MainActivity.this, path));
                        }
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

    @Override
    public void onRefresh() {
        mPresenter.clearApps();
        mToolbar.getMenu().getItem(1).setEnabled(false);
        mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apk:
                MainActivityPermissionsDispatcher.showFileExplorerWithPermissionCheck(this, null, true);
                break;
            case R.id.sort:
                mIsSortByTime = !mIsSortByTime;
                item.setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
                mRefreshView.setRefreshing(true);
                mToolbar.getMenu().getItem(1).setEnabled(false);
                mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
                break;
        }

        return true;
    }

    @Override
    public void onLetterChange(String letter) {
        if (mRecyclerView.getAdapter() == null){
            return;
        }
        ItemArray itemArray = ((AppAdapter) mRecyclerView.getAdapter()).getItems();
        int size = itemArray.size();
        for (int i = 0; i < size; i++) {
            ItemData data = itemArray.get(i);
            if (data.getDataType() == AppAdapter.TYPE_STICKY) {
                App app = data.getData();
                if (app.namePinyin.startsWith(letter)) {
                    LinearLayoutManager mLayoutManager =
                            (LinearLayoutManager) mRecyclerView.getLayoutManager();
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
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                Pair.create(iconImg, "logo_img")
        ).toBundle());
    }

    @Override
    public void onSave(App app) {
        MainActivityPermissionsDispatcher.showFileExplorerWithPermissionCheck(this, app, false);
    }

    private void dismissDialog() {
        if (mNumDialog != null) {
            mNumDialog.dismiss();
            mNumDialog = null;
        }
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void getAppsSuccess(ItemArray apps, boolean sortByTime) {
        mRefreshView.setRefreshing(false);
        mToolbar.getMenu().getItem(1).setEnabled(true);
        CommonPreference.putBoolean(this, Key.KEY_SORT, sortByTime);
        mRecyclerView.setVerticalScrollBarEnabled(sortByTime);
        if (sortByTime) {
            mSideBarView.setVisibility(View.GONE);
        }else {
            mSideBarView.setVisibility(View.VISIBLE);
            mSideBarView.showAfterHide();
        }
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(new AppAdapter(apps, this));
        } else {
            ((AppAdapter) mRecyclerView.getAdapter()).setItems(apps);
        }
    }

    @Override
    public void getAppsError(String message) {
        mRefreshView.setRefreshing(false);
        mToolbar.getMenu().getItem(1).setEnabled(true);
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
    public void inProgress(float progress) {
        if (mNumDialog != null) {
            mNumDialog.setProgress(Math.round(progress * 100));
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
        mDisposables.clear();
        dismissDialog();
        super.onDestroy();
    }


}
