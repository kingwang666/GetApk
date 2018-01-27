package com.wang.baseadapter.util;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by wang
 * on 2017/4/6
 */

public class SnappingLinearLayoutManager extends LinearLayoutManager {

    /**
     * scroll position
     */
    public int mSnap;

    public SnappingLinearLayoutManager(Context context) {
        this(context, LinearSmoothScroller.SNAP_TO_START);
    }

    public SnappingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        this(context, LinearSmoothScroller.SNAP_TO_START, orientation, reverseLayout);
    }

    public SnappingLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, LinearSmoothScroller.SNAP_TO_START, attrs, defStyleAttr, defStyleRes);
    }

    public SnappingLinearLayoutManager(Context context, int snap) {
        super(context);
        mSnap = snap;
    }

    public SnappingLinearLayoutManager(Context context, int snap, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mSnap = snap;
    }

    public SnappingLinearLayoutManager(Context context, int snap, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mSnap = snap;
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public int getSnap() {
        return mSnap;
    }

    public void setSnap(int snap) {
        mSnap = snap;
    }

    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        public TopSnappedSmoothScroller(Context context) {
            super(context);

        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return SnappingLinearLayoutManager.this
                    .computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return mSnap;
        }
    }
}
