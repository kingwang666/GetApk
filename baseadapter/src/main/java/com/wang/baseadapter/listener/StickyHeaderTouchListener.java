package com.wang.baseadapter.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.wang.baseadapter.StickyHeaderDecoration;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by wang
 * on 2016/11/8
 */

public class StickyHeaderTouchListener implements RecyclerView.OnItemTouchListener {

    private final GestureDetector mTapDetector;
    private final StickyHeaderDecoration mDecor;
    private OnHeaderClickListener mOnHeaderClickListener;


    public StickyHeaderTouchListener(Context context, final StickyHeaderDecoration decor, OnHeaderClickListener listener) {
        mTapDetector = new GestureDetector(context, new SingleTapDetector());
        mDecor = decor;
        mOnHeaderClickListener = listener;
    }


    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        return this.mOnHeaderClickListener != null && mTapDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // do nothing
    }

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = mDecor.findHeaderPositionUnder((int)e.getX(), (int)e.getY());
            if (position != -1 ){
                if (mOnHeaderClickListener != null){
                    mOnHeaderClickListener.onHeader(mDecor.findCurrentHeaderViewType(), position);
                }
            }
            return position != -1;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int position = mDecor.findHeaderPositionUnder((int)e1.getX(), (int)e1.getY());
            return position != -1;
        }
    }
}
