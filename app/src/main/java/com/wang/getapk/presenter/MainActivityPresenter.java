package com.wang.getapk.presenter;

import android.content.Context;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.BatchingListUpdateCallback;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.baseadapter.model.ItemArray;
import com.wang.baseadapter.model.ItemData;
import com.wang.getapk.model.App;
import com.wang.getapk.model.SelectEvent;
import com.wang.getapk.repository.KWSubscriber;
import com.wang.getapk.repository.LocalRepository;
import com.wang.getapk.view.adapter.AppAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/24
 */

public class MainActivityPresenter {

    private final IView mView;
    private final LocalRepository mRepository;

    private final ArrayMap<String, Pair<Integer, App>> mSelected = new ArrayMap<>();

    public MainActivityPresenter(IView view) {
        mView = view;
        mRepository = LocalRepository.getInstance();
    }


    public Disposable getAndSort(Context context, final boolean sortByTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mRepository.getAndSort(context.getApplicationContext(), sortByTime, dateFormat, new KWSubscriber<ItemArray>() {
            @Override
            public void success(ItemArray apps) {
                doItemArraySelected(apps);
                mView.getAppsSuccess(apps, sortByTime);
            }

            @Override
            public void error(int code, String error) {
                mView.getAppsError(error);
            }
        });
    }

    public Disposable getApp(Context context, Uri path) {
        return mRepository.getApp(context, path, new KWSubscriber<App>() {
            @Override
            public void success(App app) {
                mView.getAppSuccess(app);
            }

            @Override
            public void error(int code, String error) {
                mView.getAppError(error);
            }
        });
    }

    public Disposable saveApps(Context context, List<App> apps, Uri uri) {
        return mRepository.saveApks(context, apps, uri, new KWSubscriber<Uri>() {
            @Override
            public void success(Uri uri) {
                mView.saveSuccess(uri);
            }

            @Override
            public void error(int code, String error) {
                mView.saveError(error);
            }
        });
    }

    private void doItemArraySelected(ItemArray array) {
        for (int i = 0; i < array.size(); i++) {
            ItemData data = array.get(i);
            if (data.getDataType() == AppAdapter.TYPE_APP) {
                App app = data.getData();
                int index = mSelected.indexOfKey(app.packageName);
                if (index >= 0) {
                    app.isSelected = true;
                    mSelected.setValueAt(index, Pair.create(i, app));
                }
            }
        }
        mView.onSelectedChanged(mSelected.size(), mRepository.appSize());
    }

    public void clearApps() {
        mRepository.clearApps();
    }

    public void clearSelected() {
        clearSelected((ListUpdateCallback) null);
    }

    public void clearSelected(RecyclerView.Adapter<?> adapter) {
        clearSelected(adapter == null ? null : new AdapterListUpdateCallback(adapter));
    }

    public void clearSelected(ListUpdateCallback callback) {
        int size = mSelected.size();
        if (size == 0) {
            return;
        }
        if (callback == null) {
            for (int i = 0; i < size; i++) {
                Pair<Integer, App> pair = mSelected.valueAt(i);
                pair.second.isSelected = false;
            }
            mSelected.clear();
            mView.onSelectedChanged(0, mRepository.appSize());
            return;
        }

        BatchingListUpdateCallback batchCallback;
        if (callback instanceof BatchingListUpdateCallback) {
            batchCallback = (BatchingListUpdateCallback) callback;
        } else {
            batchCallback = new BatchingListUpdateCallback(callback);
        }
        for (int i = 0; i < size; i++) {
            Pair<Integer, App> pair = mSelected.valueAt(i);
            pair.second.isSelected = false;
            batchCallback.onChanged(pair.first, 1, SelectEvent.UNSELECTED);
        }
        batchCallback.dispatchLastEvent();
        mSelected.clear();
        mView.onSelectedChanged(0, mRepository.appSize());
    }

    public void selectAll(AppAdapter adapter) {
        if (adapter == null) {
            return;
        }
        selectAll(adapter.getItems(), new AdapterListUpdateCallback(adapter));
    }

    private void selectAll(ItemArray itemArray, ListUpdateCallback callback) {
        if (itemArray == null || itemArray.isEmpty()) {
            return;
        }
        int allSize = mRepository.appSize();
        int size = allSize - mSelected.size();
        if (size <= 0) {
            return;
        }
        BatchingListUpdateCallback batchCallback = null;
        if (callback instanceof BatchingListUpdateCallback) {
            batchCallback = (BatchingListUpdateCallback) callback;
        } else if (callback != null) {
            batchCallback = new BatchingListUpdateCallback(callback);
        }
        boolean changed = false;
        for (int i = 0; i < itemArray.size(); i++) {
            ItemData data = itemArray.get(i);
            if (data.getDataType() != AppAdapter.TYPE_APP) {
                continue;
            }
            App app = data.getData();
            if (!app.isSelected) {
                mSelected.put(app.packageName, Pair.create(i, app));
                changed = true;
                app.isSelected = true;
                size--;
                if (batchCallback != null) {
                    batchCallback.onChanged(i, 1, SelectEvent.SELECTED);
                }
            }
            if (size <= 0) {
                break;
            }
        }
        if (batchCallback != null) {
            batchCallback.dispatchLastEvent();
        }
        if (changed) {
            mView.onSelectedChanged(mSelected.size(), allSize);
        }
    }

    public void addSelected(int position, @NonNull App app) {
        mSelected.put(app.packageName, Pair.create(position, app));
        app.isSelected = true;
        mView.onSelectedChanged(mSelected.size(), mRepository.appSize());
    }


    public void removeSelected(App app) {
        int index = mSelected.indexOfKey(app.packageName);
        if (index >= 0) {
            mSelected.removeAt(index);
            mView.onSelectedChanged(mSelected.size(), mRepository.appSize());
        }
        app.isSelected = false;
    }

    public ArrayList<App> peekSelectedApps() {
        int size = mSelected.size();
        if (size == 0) {
            return null;
        }
        ArrayList<App> apps = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Pair<Integer, App> appWithPosition = mSelected.valueAt(i);
            apps.add(appWithPosition.second);
        }
        return apps;
    }



    public interface IView {

        void getAppsSuccess(ItemArray apps, boolean sortByTime);

        void getAppsError(String message);

        void getAppSuccess(App app);

        void getAppError(String error);

        void onSelectedChanged(int selectedCount, int allCount);

        void saveSuccess(Uri uri);

        void saveError(String error);

    }
}
