package com.wang.getapk.glide;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by wangxiaojie on 2024/2/22.
 */
@GlideModule
public final class MyGlideModule extends AppGlideModule {



    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
