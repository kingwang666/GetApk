package com.wang.getapk.model;

import java.io.File;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/11
 */

public class FileItem {

    public String path;

    public String name;

    public boolean isDirectory;

    public FileItem(File file) {
        path = file.getAbsolutePath();
        name = file.getName();
    }
}
