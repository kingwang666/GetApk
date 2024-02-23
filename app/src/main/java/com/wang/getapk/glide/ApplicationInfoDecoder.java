package com.wang.getapk.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.drawable.DrawableDecoderCompat;

import java.io.IOException;

/**
 * Created by wangxiaojie on 2024/2/22.
 */
public class ApplicationInfoDecoder implements ResourceDecoder<ApplicationInfo, Bitmap> {

    private final Context context;
    private final BitmapPool bitmapPool;

    public ApplicationInfoDecoder(Context context, BitmapPool bitmapPool) {
        this.context = context.getApplicationContext();
        this.bitmapPool = bitmapPool;
    }

    @Override
    public boolean handles(@NonNull ApplicationInfo source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<Bitmap> decode(@NonNull ApplicationInfo source, int width, int height, @NonNull Options options) throws IOException {
        Drawable drawable = null;
        if (source.icon > 0) {
            Context targetContext = findContextForPackage(source.packageName);
            drawable = DrawableDecoderCompat.getDrawable(context, targetContext, source.icon);
        }
        if (drawable == null) {
            drawable = source.loadIcon(context.getPackageManager());
        }
        if (drawable == null){
            return null;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        Bitmap bitmap = bitmapPool.get(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawableWidth, drawableHeight);
        drawable.draw(canvas);
        canvas.setBitmap(null);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    @NonNull
    private Context findContextForPackage(@NonNull String packageName) {
        // Fast path
        if (packageName.equals(context.getPackageName())) {
            return context;
        }

        try {
            return context.createPackageContext(packageName, /* flags= */ 0);
        } catch (PackageManager.NameNotFoundException e) {
            // The parent APK holds the correct context if the resource is located in a split
            if (packageName.contains(context.getPackageName())) {
                return context;
            }

            throw new IllegalArgumentException(
                    "Failed to obtain context  for: " + packageName, e);
        }
    }
}
