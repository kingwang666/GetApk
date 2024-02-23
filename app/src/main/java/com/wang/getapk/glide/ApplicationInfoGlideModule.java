package com.wang.getapk.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.LibraryGlideModule;

/**
 * Created by wangxiaojie on 2024/2/22.
 */
@GlideModule
public final class ApplicationInfoGlideModule extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(ApplicationInfo.class, ApplicationInfo.class, ApplicationInfoModelLoader.Factory.getInstance());
        registry.append(ApplicationInfo.class, Bitmap.class, new ApplicationInfoDecoder(context, glide.getBitmapPool()));
    }
}
