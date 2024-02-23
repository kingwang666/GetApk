package com.wang.getapk.model;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.recyclerview.widget.RecyclerView;

import com.wang.getapk.util.HanziToPinyin;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/25
 */

public class App implements Parcelable {

    public String name;

    public String namePinyin;

    public ApplicationInfo applicationInfo;

    public String packageName;

    public int compileSdkVersion;

    public int minSdkVersion;

    public int targetSdkVersion;

    public String apkPath;

    public String versionName;

    public int versionCode;

    public boolean isSystem;

    public boolean isDebug;

    public boolean isFormFile;

    public long lastUpdateTime;

    public String time;

    public Intent launch;

    public transient boolean isSelected;


    public App(PackageInfo info, PackageManager pm) {
        name = info.applicationInfo.loadLabel(pm).toString();
        namePinyin = HanziToPinyin.getInstance().get(name, true);
//        防止崩溃
        info.applicationInfo.loadIcon(pm);
        applicationInfo = info.applicationInfo;
        lastUpdateTime = info.lastUpdateTime;
        packageName = info.packageName;
        apkPath = applicationInfo.sourceDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            compileSdkVersion = applicationInfo.compileSdkVersion;
        }
        targetSdkVersion = applicationInfo.targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            minSdkVersion = applicationInfo.minSdkVersion;
        }
        versionName = info.versionName;
        versionCode = info.versionCode;
        launch = pm.getLaunchIntentForPackage(packageName);
        isSystem = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        isDebug = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public App() {

    }


    protected App(Parcel in) {
        name = in.readString();
        namePinyin = in.readString();
        applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        packageName = in.readString();
        compileSdkVersion = in.readInt();
        minSdkVersion = in.readInt();
        targetSdkVersion = in.readInt();
        apkPath = in.readString();
        versionName = in.readString();
        versionCode = in.readInt();
        isSystem = in.readByte() != 0;
        isDebug = in.readByte() != 0;
        isFormFile = in.readByte() != 0;
        lastUpdateTime = in.readLong();
        time = in.readString();
        launch = in.readParcelable(Intent.class.getClassLoader());
    }

    public String getSaveName(){
        return name + "_" + versionName + ".apk";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(namePinyin);
        dest.writeParcelable(applicationInfo, flags);
        dest.writeString(packageName);
        dest.writeInt(compileSdkVersion);
        dest.writeInt(minSdkVersion);
        dest.writeInt(targetSdkVersion);
        dest.writeString(apkPath);
        dest.writeString(versionName);
        dest.writeInt(versionCode);
        dest.writeByte((byte) (isSystem ? 1 : 0));
        dest.writeByte((byte) (isDebug ? 1 : 0));
        dest.writeByte((byte) (isFormFile ? 1 : 0));
        dest.writeLong(lastUpdateTime);
        dest.writeString(time);
        dest.writeParcelable(launch, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
