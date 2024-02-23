package com.wang.getapk.glide;

import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

/**
 * Created by wangxiaojie on 2024/2/22.
 */
public class ApplicationInfoModelLoader implements ModelLoader<ApplicationInfo, ApplicationInfo> {

    private static final ApplicationInfoModelLoader INSTANCE = new ApplicationInfoModelLoader();

    public static ApplicationInfoModelLoader getInstance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public LoadData<ApplicationInfo> buildLoadData(@NonNull ApplicationInfo applicationInfo, int width, int height, @NonNull Options options) {
        return new ModelLoader.LoadData<>(new ObjectKey(applicationInfo.packageName), new ApplicationInfoFetcher(applicationInfo));
    }

    @Override
    public boolean handles(@NonNull ApplicationInfo applicationInfo) {
        return true;
    }

    private static class ApplicationInfoFetcher implements DataFetcher<ApplicationInfo> {

        private final ApplicationInfo resource;

        ApplicationInfoFetcher(ApplicationInfo resource) {
            this.resource = resource;
        }


        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super ApplicationInfo> callback) {
            callback.onDataReady(resource);
        }

        @Override
        public void cleanup() {
            // Do nothing.
        }

        @Override
        public void cancel() {
            // Do nothing.
        }

        @NonNull
        @Override
        public Class<ApplicationInfo> getDataClass() {
            return ApplicationInfo.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }


    public static class Factory implements ModelLoaderFactory<ApplicationInfo, ApplicationInfo> {

        private static final ApplicationInfoModelLoader.Factory FACTORY = new ApplicationInfoModelLoader.Factory();

        public static ApplicationInfoModelLoader.Factory getInstance() {
            return FACTORY;
        }

        @NonNull
        @Override
        public ModelLoader<ApplicationInfo, ApplicationInfo> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return ApplicationInfoModelLoader.getInstance();
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }
}
