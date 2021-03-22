package com.wang.getapk.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wang.getapk.util.AutoGrayThemeHelper;
import com.wang.getapk.util.DateChangedHelper;
import com.wang.getapk.util.GrayThemeHelper;
import com.wang.getapk.util.IGrayChecker;

import java.util.Calendar;

/**
 * Created on 2020/4/9
 * Author: bigwang
 * Description:
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    protected AutoGrayThemeHelper mGrayThemeHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGrayThemeHelper = createGrayThemeHelper(getWindow().getDecorView(), this);
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.bindDateChangedReceiver();
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

    @Nullable
    protected AutoGrayThemeHelper createGrayThemeHelper(View view, Context context) {
        return new AutoGrayThemeHelper(view, context, AutoGrayThemeHelper.QINGMING_CHECKER);
    }

    protected final void applyOrRemoveGrayTheme() {
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

//    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//
////        try {
////            if ("FrameLayout".equals(name)) {
////                int count = attrs.getAttributeCount();
////                for (int i = 0; i < count; i++) {
////                    String attributeName = attrs.getAttributeName(i);
////                    String attributeValue = attrs.getAttributeValue(i);
////                    if (attributeName.equals("id")) {
////                        int id = Integer.parseInt(attributeValue.substring(1));
////                        String idVal = getResources().getResourceName(id);
////                        if ("android:id/content".equals(idVal)) {
////                            GrayFrameLayout grayFrameLayout = new GrayFrameLayout(context, attrs);
//////                            grayFrameLayout.setWindow(getWindow());
////                            return grayFrameLayout;
////                        }
////                    }
////                }
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//        return super.onCreateView(name, context, attrs);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.unbindDateChangedReceiver();
        }
    }

}
