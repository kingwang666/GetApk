package com.wang.getapk.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


/**
 * Created on 2016/1/6.
 * Author: wang
 */
public class FileUtil {

    public static boolean isHaveSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * 获取sd卡根路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    /**
     * 获取根路径下的folderName文件夹
     *
     * @param folderName
     * @return
     */
    public static File getSaveFolder(Context context, String folderName) {
        File file;
        if (isHaveSDCard()) {
            file = new File(getSDCardPath() + File.separator + folderName);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            String[] names = folderName.split(File.separator);
            file = context.getDir(names[names.length - 1], Context.MODE_PRIVATE);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static File getSaveFolder(Context context, String folderName, String childName) {
        File file = new File(getSaveFolder(context, folderName), childName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static File newCreateFolder(String parent, String child) throws IOException {
        return newCreateFolder(parent, child, false);
    }

    public static File newCreateFolder(String parent, String child, boolean cover) throws IOException {
        File file = new File(parent, child);
        if (!file.exists()){
            file.mkdirs();
            return file;
        }else if (cover){
            return file;
        }else {
            file = getUnExistsFolder(file.getAbsolutePath());
            file.mkdirs();
            return file;
        }
    }

    public static File getUnExistsFolder(String filePath) throws IOException {
        for (int i = 1; i< Integer.MAX_VALUE; i++){
            File file = new File(String.format(Locale.getDefault(), "%s(%d)", filePath, i));
            if (!file.exists()){
                return file;
            }
        }
        throw new IOException("you are so many this name file");
    }

    /**
     * 获取根路径下的folderName文件夹路径
     *
     * @param folderName
     * @return
     */
    public static String getSavePath(Context context, String folderName) {
        return getSaveFolder(context, folderName).getAbsolutePath();
    }

    public static String getSavePath(Context context, String folderName, String childName) {
        return getSaveFolder(context, folderName, childName).getAbsolutePath();
    }

}
