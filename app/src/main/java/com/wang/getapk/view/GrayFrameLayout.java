package com.wang.getapk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


/**
 * Created on 2020/4/9
 * Author: bigwang
 * Description:
 */
public class GrayFrameLayout extends FrameLayout {

    private Paint mPaint = new Paint();

    private boolean mIsSaved = false;


    public GrayFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        mPaint.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!mIsSaved) {
            mIsSaved = true;
            canvas.saveLayer(null, mPaint, Canvas.ALL_SAVE_FLAG);
            super.dispatchDraw(canvas);
            mIsSaved = false;
            canvas.restore();
        } else {
            super.draw(canvas);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mIsSaved) {
            mIsSaved = true;
            canvas.saveLayer(null, mPaint, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            mIsSaved = false;
            canvas.restore();
        } else {
            super.draw(canvas);
        }

    }

}
