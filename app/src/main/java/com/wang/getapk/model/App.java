package com.wang.getapk.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.wang.getapk.util.HanziToPinyin;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class App {

    public String name;

    public String namePinyin;

    public ApplicationInfo applicationInfo;

    public String packageName;

    public String apkPath;

    public String versionName;

    public int versionCode;

    public boolean isSystem;

    public boolean isDebug;

    public long lastUpdateTime;

    public String time;


    public App(PackageInfo info, PackageManager pm) {
        name = info.applicationInfo.loadLabel(pm).toString();
        namePinyin = HanziToPinyin.getInstance().get(name, true);
        info.applicationInfo.loadIcon(pm);
        applicationInfo = info.applicationInfo;
        lastUpdateTime = info.lastUpdateTime;
        packageName = info.packageName;
        apkPath = applicationInfo.sourceDir;
        versionName = info.versionName;
        versionCode = info.versionCode;
        isSystem = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        isDebug = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public App() {

    }
}
