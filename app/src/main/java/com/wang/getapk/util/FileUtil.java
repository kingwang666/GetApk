package com.wang.getapk.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.wang.getapk.view.listener.OnCopyListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Pattern;


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
        if (!file.exists()) {
            file.mkdirs();
            return file;
        } else if (cover) {
            return file;
        } else {
            file = getUnExistsFolder(file.getAbsolutePath());
            file.mkdirs();
            return file;
        }
    }

    public static File getUnExistsFolder(String filePath) throws IOException {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            File file = new File(String.format(Locale.getDefault(), "%s(%d)", filePath, i));
            if (!file.exists()) {
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

    public static File getCanonicalFile(String path) throws IOException {
        File parent = new File(path).getAbsoluteFile();
        parent = parent.getCanonicalFile();
        if (TextUtils.isEmpty(parent.toString())) {
            parent = new File("/");
        }
        return parent;
    }

    public static boolean isApk(File file) {
        Pattern pattern = Pattern.compile("\\.apk$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(file.getName()).find();
    }

    @Deprecated
    public static File copy(String source, String dest, String name, OnCopyListener listener) throws IOException {
        File file = new File(source);
        if (!file.exists()) {
            throw new IOException("the apk file is no exists");
        }
        File dst = new File(dest, name);
        if (dst.exists()) {
            dst.delete();
        }
        long total = file.length();
        long sum = 0;
        try (InputStream in = new FileInputStream(file); OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024 * 4];
            int len;
            Thread thread = Thread.currentThread();
            while ((len = in.read(buf)) > 0) {
                if (thread.isInterrupted()) {
                    break;
                }
                sum += len;
                out.write(buf, 0, len);
                if (listener != null) {
                    listener.inProgress(sum * 1.0f / total);
                }

            }
        }
        return dst;
    }

    public static void copy(ContentResolver resolver, Uri source, Uri dest, OnCopyListener listener) throws IOException {

        FileInputStream in = null;
        OutputStream out = null;
        try{
            AssetFileDescriptor fd = resolver.openAssetFileDescriptor(source, "r");
            in =  fd != null ? fd.createInputStream() : null;

            if (in == null){
                throw new IOException("open the src file failed");
            }
            long total = fd.getLength();
            long sum = 0;

            out = resolver.openOutputStream(dest);

            if (out == null) {
                throw new IOException("open the dest file failed");
            }
            // Transfer bytes from in to out
            byte[] buf = new byte[1024 * 4];
            int len;
            Thread thread = Thread.currentThread();
            while ((len = in.read(buf)) > 0) {
                if (thread.isInterrupted()) {
                    break;
                }
                sum += len;
                out.write(buf, 0, len);
                if (listener != null) {
                    listener.inProgress(sum * 1.0f / total);
                }
            }
        }finally {
            IOUtil.closeQuiet(in);
            IOUtil.closeQuiet(out);
        }
    }

    @NonNull
    public static String getPath(@NonNull final Context context, @NonNull final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final Uri contentUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentUri = uri;
                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                }
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                if (contentUri == null) {
                    return "";
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            String path = uri.getPath();
            return path == null ? "" : path;
        } else if (!isKitKat) {
            String filename = "";
            if (uri.getScheme().compareTo("content") == 0) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        filename = cursor.getString(0);
                    }
                    cursor.close();
                }
            } else {
                if (uri.getScheme().compareTo("file") == 0) {         //file:///开头的uri{
                    filename = uri.toString().replace("file://", "");
                    //替换file://
                    if (!filename.startsWith("/mnt")) {
                        //加上"/mnt"头
                        filename += "/mnt";
                    }
                }
            }
            return filename;
        }

        return "";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.MediaColumns.RELATIVE_PATH : MediaStore.MediaColumns.DATA;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                if (!cursor.isNull(column_index)) {
                    return cursor.getString(column_index);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
