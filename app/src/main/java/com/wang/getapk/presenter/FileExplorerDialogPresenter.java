package com.wang.getapk.presenter;

import android.text.TextUtils;
import android.util.Log;


import com.wang.getapk.model.FileItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/11
 */

public class FileExplorerDialogPresenter {

    private List<FileItem> mFileItems;
    private IView mView;

    /**
     * 文件导出为图片的 弹框dialog presenter
     * @param view
     */
    public FileExplorerDialogPresenter(IView view) {
        mFileItems = new ArrayList<>();
        mView = view;
    }

    private File getCanonicalFile(String path) throws IOException {
        File parent = new File(path).getAbsoluteFile();
        parent = parent.getCanonicalFile();
        if (TextUtils.isEmpty(parent.toString())) {
            parent = new File("/");
        }
        return parent;
    }

    public FileItem get(int position) {
        return mFileItems.get(position);
    }

    public List<FileItem> getFileItems() {
        return mFileItems;
    }

    private boolean isApk(File file){
        Pattern pattern = Pattern.compile("\\.apk$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(file.getName()).find();
    }

    public Disposable getFiles(final String path) {
        final File parent;
        try {
            parent = getCanonicalFile(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return Flowable.just(parent)
                .map(new Function<File, List<FileItem>>() {
                    @Override
                    public List<FileItem> apply(File parent) throws Exception {
                        File[] files = parent.listFiles();
                        List<FileItem> fileItems = new ArrayList<>();
                        if (parent.getParent() != null) {
                            FileItem item = new FileItem(new File(parent, ".."));
                            item.isDirectory = true;
                            fileItems.add(item);
                        }
                        if (files != null) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    FileItem item = new FileItem(file);
                                    item.isDirectory = true;
                                    fileItems.add(item);
                                } else if (isApk(file)) {
                                    FileItem item = new FileItem(file);
                                    item.isDirectory = false;
                                    fileItems.add(item);
                                }
                            }
                            Collections.sort(fileItems, new Comparator<FileItem>() {
                                @Override
                                public int compare(FileItem lhs, FileItem rhs) {
                                    if (lhs.isDirectory && !rhs.isDirectory)
                                        return -1;
                                    else if (!lhs.isDirectory && rhs.isDirectory)
                                        return 1;
                                    return lhs.name.compareToIgnoreCase(rhs.name);
                                }
                            });
                        }
                        return fileItems;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<List<FileItem>>() {
                    @Override
                    public void onNext(List<FileItem> fileItems) {
                        mFileItems.clear();
                        mFileItems.addAll(fileItems);
                        mView.getFilesSuccess(parent);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("error", t.getMessage(), t);
                        mView.getFilesError(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public interface IView  {

        void getFilesSuccess(File parent);

        void getFilesError(String message);
    }
}
