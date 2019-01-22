package com.wang.getapk.presenter;


import com.wang.getapk.model.FileItem;
import com.wang.getapk.repository.KWSubscriber;
import com.wang.getapk.repository.LocalRepository;
import com.wang.getapk.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/11
 */

public class FileExplorerDialogPresenter {

    private List<FileItem> mFileItems;
    private IView mView;
    private LocalRepository mRepository;


    public FileExplorerDialogPresenter(IView view) {
        mFileItems = new ArrayList<>();
        mView = view;
        mRepository = LocalRepository.getInstance();
    }

    public FileItem get(int position) {
        return mFileItems.get(position);
    }

    public List<FileItem> getFileItems() {
        return mFileItems;
    }

    public Disposable getFiles(final String path) {
        final File parent;
        try {
            parent = FileUtil.getCanonicalFile(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return mRepository.getFiles(parent, new KWSubscriber<List<FileItem>>() {
            @Override
            public void success(List<FileItem> fileItems) {
                mFileItems.clear();
                mFileItems.addAll(fileItems);
                mView.getFilesSuccess(parent);
            }

            @Override
            public void error(int code, String error) {
                mView.getFilesError(error);
            }
        });

    }

    public interface IView {

        void getFilesSuccess(File parent);

        void getFilesError(String message);
    }
}
