package com.wang.getapk.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wang.baseadapter.StickyHeaderDecoration;
import com.wang.baseadapter.listener.OnHeaderClickListener;
import com.wang.baseadapter.listener.StickyHeaderTouchListener;
import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.baseadapter.widget.WaveSideBarView;
import com.wang.getapk.R;
import com.wang.getapk.constant.Key;
import com.wang.getapk.databinding.ActivityMainBinding;
import com.wang.getapk.model.App;
import com.wang.getapk.presenter.MainActivityPresenter;
import com.wang.getapk.util.CommonPreference;
import com.wang.getapk.view.adapter.AppAdapter;
import com.wang.getapk.view.dialog.ProgressDialog;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity<ActivityMainBinding>
        implements AppAdapter.OnAppClickListener,
        Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        WaveSideBarView.OnTouchLetterChangeListener,
        OnHeaderClickListener,
        MainActivityPresenter.IView {

    private MainActivityPresenter mPresenter;
    private CompositeDisposable mDisposables;

    private ProgressDialog mDialog;

    private boolean mIsSortByTime = true;

    private final ActivityResultLauncher<String> mGetApkLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mDialog = new ProgressDialog.Builder(MainActivity.this)
                        .title(R.string.parsing)
                        .show();
                mDisposables.add(mPresenter.getApp(MainActivity.this, result));
            } else {
                MainActivityPermissionsDispatcher.getAppWithPermissionCheck(MainActivity.this, result);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityMainBinding.inflate(getLayoutInflater()));
        mIsSortByTime = CommonPreference.getBoolean(this, Key.KEY_SORT, mIsSortByTime);
        mPresenter = new MainActivityPresenter(this);
        mDisposables = new CompositeDisposable();

        viewBinding.toolbar.inflateMenu(R.menu.menu_main);
        viewBinding.toolbar.getMenu().getItem(1).setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
        viewBinding.toolbar.setOnMenuItemClickListener(this);
//        viewBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        viewBinding.refreshView.setOnRefreshListener(this);
        viewBinding.refreshView.setColorSchemeResources(R.color.blue300, R.color.red300, R.color.green300);
        viewBinding.refreshView.setRefreshing(true);

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

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

            }
        });


//        viewBinding.sideBarView.setVisibility(mIsSortByTime ? View.GONE : View.VISIBLE);
        viewBinding.sideBarView.setOnTouchLetterChangeListener(this);

        onRefresh();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(value = Manifest.permission.READ_EXTERNAL_STORAGE, maxSdkVersion = Build.VERSION_CODES.S_V2)
    public void getApp(Uri uri) {
        mDialog = new ProgressDialog.Builder(MainActivity.this)
                .title(R.string.parsing)
                .show();
        mDisposables.add(mPresenter.getApp(MainActivity.this, uri));
    }

    @SuppressLint("NoCorrespondingNeedsPermission")
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


    @Override
    public void onRefresh() {
        mPresenter.clearApps();
        viewBinding.toolbar.getMenu().getItem(1).setEnabled(false);
        mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apk:
                mGetApkLauncher.launch("application/vnd.android.package-archive");
                break;
            case R.id.sort:
                mIsSortByTime = !mIsSortByTime;
                item.setIcon(mIsSortByTime ? R.drawable.ic_a_white_24dp : R.drawable.ic_timer_white_24dp);
                viewBinding.refreshView.setRefreshing(true);
                viewBinding.toolbar.getMenu().getItem(1).setEnabled(false);
                mDisposables.add(mPresenter.getAndSort(this, mIsSortByTime));
                break;
        }

        return true;
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


    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void getAppsSuccess(ItemArray apps, boolean sortByTime) {
        viewBinding.refreshView.setRefreshing(false);
        viewBinding.toolbar.getMenu().getItem(1).setEnabled(true);
        CommonPreference.putBoolean(this, Key.KEY_SORT, sortByTime);
        viewBinding.recyclerView.setVerticalScrollBarEnabled(sortByTime);
        if (sortByTime) {
            viewBinding.sideBarView.setVisibility(View.GONE);
        } else {
            viewBinding.sideBarView.setVisibility(View.VISIBLE);
            viewBinding.sideBarView.showAfterHide();
        }
        if (viewBinding.recyclerView.getAdapter() == null) {
            viewBinding.recyclerView.setAdapter(new AppAdapter(apps, this));
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
    protected void onDestroy() {
        mDisposables.clear();
        dismissDialog();
        super.onDestroy();
    }


}
