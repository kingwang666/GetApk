package com.wang.getapk.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MenuItem;

import com.wang.getapk.R;
import com.wang.getapk.constant.Key;
import com.wang.getapk.constant.Path;
import com.wang.getapk.model.FileItem;
import com.wang.getapk.presenter.FileExplorerDialogPresenter;
import com.wang.getapk.util.CommonPreference;
import com.wang.getapk.util.FileUtil;
import com.wang.getapk.view.adapter.FileAdapter;
import com.wang.getapk.view.listener.OnPathSelectListener;
import com.wang.getapk.view.listener.OnRecyclerClickListener;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import io.reactivex.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/11
 */

public class FileExplorerDialog extends BaseDialog<FileExplorerDialog.Builder>
        implements FileExplorerDialogPresenter.IView,
        OnRecyclerClickListener,
        Toolbar.OnMenuItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.path_tv)
    AppCompatTextView mPathTV;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private FileExplorerDialogPresenter mPresenter;
    private Disposable mDisposable;

    private FileExplorerDialog(Builder builder) {
        super(builder);
        mPresenter = new FileExplorerDialogPresenter(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_file_explorer;
    }

    @Override
    protected void afterView(Context context, Builder builder) {
        if (!builder.isSelectFile) {
            mToolbar.inflateMenu(R.menu.menu_dialog_file_explorer);
            mToolbar.setOnMenuItemClickListener(this);
        }
        mToolbar.setTitle(builder.title);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new FileAdapter(mPresenter.getFileItems(), this));
        mRecyclerView.setHasFixedSize(true);
        String lastPath;
        if (builder.isSelectFile) {
            lastPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else {
            lastPath = CommonPreference.getString(context, Key.KEY_LAST_DIR, FileUtil.getSavePath(mBuilder.context, Path.BASH_PATH));
        }
        mDisposable = mPresenter.getFiles(lastPath);
        addDisposable(mDisposable);
    }


    @Override
    public void onClick(int viewType, int position, @Nullable Object data) {
        FileItem item = mPresenter.get(position);
        if (item.isDirectory) {
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
            mDisposable = mPresenter.getFiles(item.path);
            addDisposable(mDisposable);
        }else if (mBuilder.isSelectFile){
            mBuilder.pathSelectListener.onSelected(item.path);
            dismiss();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.confirm:
                mBuilder.pathSelectListener.onSelected((String) mPathTV.getText());
                dismiss();
                break;
            case R.id.create:
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.new_create_folder)
                        .setView(R.layout.item_edit)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.new_create, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppCompatEditText editText = ((AppCompatDialog) dialog).findViewById(R.id.name_et);
                                String name = editText.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    name = getContext().getString(R.string.new_create_folder);
                                }
                                try {
                                    String path = mPathTV.getText().toString();
                                    FileUtil.newCreateFolder(path, name);
                                    if (mDisposable != null && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                    mDisposable = mPresenter.getFiles(path);
                                    addDisposable(mDisposable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        })
                        .show().setCanceledOnTouchOutside(false);
                break;
        }

        return true;
    }


    @Override
    public void getFilesSuccess(File parent) {
        mRecyclerView.getAdapter().notifyDataSetChanged();
        String path = parent.getAbsolutePath();
        if (!mBuilder.isSelectFile) {
            CommonPreference.putString(getContext(), Key.KEY_LAST_DIR, path);
        }
        mPathTV.setText(path);
    }

    @Override
    public void getFilesError(String message) {

    }

    public static class Builder extends BaseBuilder<Builder> {

        private OnPathSelectListener pathSelectListener;
        private boolean isSelectFile;

        public Builder(@NonNull Context context) {
            super(context);
        }

        public Builder pathSelectListener(OnPathSelectListener pathSelectListener) {
            this.pathSelectListener = pathSelectListener;
            return this;
        }

        public Builder selectFile(boolean file){
            isSelectFile = file;
            return this;
        }

        @UiThread
        public FileExplorerDialog build() {
            return new FileExplorerDialog(this);
        }

        @UiThread
        public FileExplorerDialog show() {
            FileExplorerDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }

}
