package com.wang.getapk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created on 2020/4/9
 * Author: bigwang
 * Description:
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {

//        try {
//            if ("FrameLayout".equals(name)) {
//                int count = attrs.getAttributeCount();
//                for (int i = 0; i < count; i++) {
//                    String attributeName = attrs.getAttributeName(i);
//                    String attributeValue = attrs.getAttributeValue(i);
//                    if (attributeName.equals("id")) {
//                        int id = Integer.parseInt(attributeValue.substring(1));
//                        String idVal = getResources().getResourceName(id);
//                        if ("android:id/content".equals(idVal)) {
//                            GrayFrameLayout grayFrameLayout = new GrayFrameLayout(context, attrs);
////                            grayFrameLayout.setWindow(getWindow());
//                            return grayFrameLayout;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return super.onCreateView(name, context, attrs);
    }
}
