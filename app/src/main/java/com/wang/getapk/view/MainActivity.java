package com.wang.getapk.view;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.wang.getapk.presenter.MainActivityPresenter;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.constant.Key;
import com.wang.getapk.view.dialog.BaseDialog;
import com.wang.getapk.view.dialog.FileExplorerDialog;
import com.wang.getapk.view.dialog.NumberProgressDialog;
import com.wang.getapk.view.listener.OnPathSelectListener;
import com.wang.getapk.view.listener.OnRecyclerClickListener;
import com.wang.getapk.model.App;
import com.wang.getapk.util.CommonPreference;

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
        implements OnRecyclerClickListener,
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
    private NumberProgressDialog mDialog;

    private boolean mIsSortByTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mIsSortByTime = CommonPreference.getBoolean(this, Key.KEY_SORT, mIsSortByTime);
        mPresenter = new MainActivityPresenter(this);
        mDisposables = new CompositeDisposable();

        mToolbar.inflateMenu(R.menu.menu_sort);
        mToolbar.getMenu().getItem(0).setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
        mToolbar.setOnMenuItemClickListener(this);

        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setColorSchemeResources(R.color.blue300, R.color.red300, R.color.green300);
        mRefreshView.setRefreshing(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        StickyHeaderDecoration decoration = new StickyHeaderDecoration(AppAdapter.TYPE_STICKY);
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.addOnItemTouchListener(new StickyHeaderTouchListener(this, decoration, this));
        mRecyclerView.setVerticalScrollBarEnabled(mIsSortByTime);

        mSideBarView.setVisibility(mIsSortByTime ? View.GONE : View.VISIBLE);
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
    public void showFileExplorer(final App app) {
        new FileExplorerDialog.Builder(this)
                .pathSelectListener(new OnPathSelectListener() {
                    @Override
                    public void onSelected(String path) {
                        if (mDialog != null){
                            dismissDialog();
                        }
                        mDialog = new NumberProgressDialog.Builder(MainActivity.this)
                                .cancelable(false)
                                .canceledOnTouchOutside(false)
                                .title("正在复制")
                                .negative("取消")
                                .onNegative(new BaseDialog.OnButtonClickListener() {
                                    @Override
                                    public void onClick(@NonNull BaseDialog dialog, int which) {
                                        if (mDisposable != null && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }
                                }).show();
                        mDisposable = mPresenter.saveApk(app, path);
                        mDisposables.add(mDisposable);
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
        mToolbar.getMenu().getItem(0).setEnabled(false);
        mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mIsSortByTime = !mIsSortByTime;
        item.setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
        mRefreshView.setRefreshing(true);
        mToolbar.getMenu().getItem(0).setEnabled(false);
        mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
        return true;
    }

    @Override
    public void onLetterChange(String letter) {
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
    public void onClick(int viewType, final int position, @Nullable final Object data) {
        MainActivityPermissionsDispatcher.showFileExplorerWithPermissionCheck(this, (App) data);
    }

    private void dismissDialog(){
        if (mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void getAppsSuccess(ItemArray apps, boolean sortByTime) {
        mRefreshView.setRefreshing(false);
        mToolbar.getMenu().getItem(0).setEnabled(true);
        CommonPreference.putBoolean(this, Key.KEY_SORT, sortByTime);
        mRecyclerView.setVerticalScrollBarEnabled(sortByTime);
        mSideBarView.setVisibility(sortByTime ? View.GONE : View.VISIBLE);
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(new AppAdapter(apps, this));
        } else {
            ((AppAdapter) mRecyclerView.getAdapter()).setItems(apps);
        }
    }

    @Override
    public void getAppsError(String message) {
        mRefreshView.setRefreshing(false);
        mToolbar.getMenu().getItem(0).setEnabled(true);
        Toast.makeText(this, "error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inProgress(float progress) {
        if (mDialog != null){
            mDialog.setProgress(Math.round(progress * 100));
        }
    }

    @Override
    public void saveSuccess(String path) {
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
