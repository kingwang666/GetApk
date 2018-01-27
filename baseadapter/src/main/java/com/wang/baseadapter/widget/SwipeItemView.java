package com.wang.baseadapter.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/1/5.
 * Author: wang
 */
public class SwipeItemView extends LinearLayout {
    private ViewDragHelper viewDragHelper;
    private View contentView;
    private View actionView;


    private int dragDistance;
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int draggedX;
    private int mTouchSlop;

    private List<SwipeListener> mSwipeListeners = new ArrayList<>();
    private DoubleClickListener mDoubleClickListener;
    private SingleClickListener mSingleClickListener;
    private GestureDetector gestureDetector = new GestureDetector(getContext(), new SwipeDetector());
    private boolean isLock = false;

    public enum Status {
        Middle,
        Open,
        Close
    }

    public enum Mode {
        Single, Multiple
    }

    public SwipeItemView(Context context) {
        super(context);
        init(context);
    }

    public SwipeItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        viewDragHelper = ViewDragHelper.create(this, new DragHelperCallback());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        contentView = getChildAt(0);
        actionView = getChildAt(1);
//        actionView.setVisibility(GONE);
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        dragDistance = actionView.getMeasuredWidth();
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            return view == contentView || view == actionView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            draggedX = left;
            if (changedView == contentView) {
                actionView.offsetLeftAndRight(dx);
            } else {
                contentView.offsetLeftAndRight(dx);
            }
            if (actionView.getVisibility() == View.GONE) {
                actionView.setVisibility(View.VISIBLE);
            }
            dispatchSwipeEvent(dx <= 0);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == contentView) {
                final int leftBound = getPaddingLeft();
                final int minLeftBound = -leftBound - dragDistance;
                final int newLeft = Math.min(Math.max(minLeftBound, left), 0);
                return newLeft;
            } else {
                final int minLeftBound = getPaddingLeft() + contentView.getMeasuredWidth() - dragDistance;
                final int maxLeftBound = getPaddingLeft() + contentView.getMeasuredWidth() + getPaddingRight();
                final int newLeft = Math.min(Math.max(left, minLeftBound), maxLeftBound);
                return newLeft;
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return dragDistance;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            boolean settleToOpen = false;
            if (xvel > AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false;
            } else if (xvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = true;
            } else if (draggedX <= -dragDistance / 2) {
                settleToOpen = true;
            } else if (draggedX > -dragDistance / 2) {
                settleToOpen = false;
            }

            final int settleDestX = settleToOpen ? -dragDistance : 0;
            viewDragHelper.smoothSlideViewTo(contentView, settleDestX, 0);
            ViewCompat.postInvalidateOnAnimation(SwipeItemView.this);
        }
    }

    public void addSwipeListener(SwipeListener l) {
        mSwipeListeners.add(l);
    }

    public void removeSwipeListener(SwipeListener l) {
        mSwipeListeners.remove(l);
    }

    public void removeAllSwipeListener() {
        mSwipeListeners.clear();
    }

    public void open() {
        open(true, true);
    }

    public void open(boolean smooth) {
        open(smooth, true);
    }

    public void open(boolean smooth, boolean notify) {
        draggedX = dragDistance;
        if (contentView == null || mIsBeingDragged) {
            return;
        }
        if (smooth) {
            viewDragHelper.smoothSlideViewTo(contentView, -dragDistance, 0);
        } else {
            contentView.layout(-dragDistance, 0, contentView.getMeasuredWidth() - dragDistance, contentView.getMeasuredHeight());
            actionView.layout(contentView.getMeasuredWidth() - dragDistance, 0, contentView.getMeasuredWidth(), actionView.getMeasuredHeight());


            if (notify) {
                dispatchSwipeEvent(true);
            }
        }
        if (actionView.getVisibility() == GONE) {
            actionView.setVisibility(VISIBLE);
        }
        invalidate();
    }

    /**
     * smoothly close surface.
     */
    public void close() {
        close(true, true);
    }

    public void close(boolean smooth) {
        close(smooth, true);
    }

    /**
     * close surface
     *
     * @param smooth smoothly or not.
     * @param notify if notify all the listeners.
     */
    public void close(boolean smooth, boolean notify) {

        if (contentView == null || mIsBeingDragged) {
            return;
        }
        draggedX = 0;
        if (smooth)
            viewDragHelper.smoothSlideViewTo(contentView, 0, 0);
        else {
            contentView.layout(0, 0, contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
            actionView.layout(contentView.getMeasuredWidth(), 0, contentView.getMeasuredWidth() + dragDistance, actionView.getMeasuredHeight());
            if (notify) {
                dispatchSwipeEvent(false);
            }
        }

        invalidate();
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean smooth) {
        if (getOpenStatus() == Status.Open)
            close(smooth);
        else if (getOpenStatus() == Status.Close) open(smooth);
    }

    private Rect computeContentLayoutArea(boolean open) {
        int l = getPaddingLeft(), t = getPaddingTop();
        if (open) {
            l = getPaddingLeft() - dragDistance;
        }
        return new Rect(l, t, l + getMeasuredWidth(), t + getMeasuredHeight());
    }


    private int mEventCounter = 0;

    protected void dispatchSwipeEvent(boolean open) {
        Status status = getOpenStatus();

        if (!mSwipeListeners.isEmpty()) {
            mEventCounter++;
            for (SwipeListener l : mSwipeListeners) {
                if (mEventCounter == 1) {
                    if (open) {
                        l.onStartOpen(this);
                    } else {
                        l.onStartClose(this);
                    }
                }
//                l.onUpdate(SwipeItemView.this, surfaceLeft - getPaddingLeft(), surfaceTop - getPaddingTop());
            }

            if (status == Status.Close) {
//                if (actionView.getVisibility() != GONE){
//                    actionView.setVisibility(GONE);
//                }
                for (SwipeListener l : mSwipeListeners) {
                    l.onClose(SwipeItemView.this);
                }
                mEventCounter = 0;
            }

            if (status == Status.Open) {
                for (SwipeListener l : mSwipeListeners) {
                    l.onOpen(SwipeItemView.this);
                }
                mEventCounter = 0;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isLock) {
            return super.onInterceptTouchEvent(ev);
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewDragHelper.shouldInterceptTouchEvent(ev);
                    mIsBeingDragged = false;
                    sX = ev.getRawX();
                    sY = ev.getRawY();
                    //if the swipe is in middle state(scrolling), should intercept the touch
                    if (getOpenStatus() == Status.Middle) {
                        mIsBeingDragged = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    boolean beforeCheck = mIsBeingDragged;
                    checkCanDrag(ev);
                    if (mIsBeingDragged) {
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
//                    viewDragHelper.shouldInterceptTouchEvent(ev);
                    }
                    if (!beforeCheck && mIsBeingDragged) {
                        //let children has one chance to catch the touch, and request the swipe not intercept
                        //useful when swipeLayout wrap a swipeLayout or other gestural layout
                        return false;
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mIsBeingDragged = false;
                    viewDragHelper.shouldInterceptTouchEvent(ev);
                    break;
                default://handle other action, such as ACTION_POINTER_DOWN/UP
                    viewDragHelper.shouldInterceptTouchEvent(ev);
            }
            return mIsBeingDragged;
        }

    }

    /**
     * get the open status.
     *
     * @return {} Open , Close or
     * Middle.
     */
    public Status getOpenStatus() {
        if (contentView == null) {
            return Status.Close;
        }
        int surfaceLeft = contentView.getLeft();
        int surfaceTop = contentView.getTop();
        if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop()) return Status.Close;

        if (surfaceLeft == (getPaddingLeft() - dragDistance) || surfaceLeft == (getPaddingLeft() + dragDistance)
                || surfaceTop == (getPaddingTop() - dragDistance) || surfaceTop == (getPaddingTop() + dragDistance))
            return Status.Open;

        return Status.Middle;
    }

    private boolean mIsBeingDragged;

    private void checkCanDrag(MotionEvent ev) {
        if (mIsBeingDragged) return;
        if (getOpenStatus() == Status.Middle) {
            mIsBeingDragged = true;
            return;
        }
        Status status = getOpenStatus();
        float distanceX = ev.getRawX() - sX;
        float distanceY = ev.getRawY() - sY;
        float angle = Math.abs(distanceY / distanceX);
        angle = (float) Math.toDegrees(Math.atan(angle));


        boolean doNothing = false;

        boolean suitable = (status == Status.Open && distanceX > mTouchSlop)
                || (status == Status.Close && distanceX < -mTouchSlop);
        suitable = suitable || (status == Status.Middle);

        if (angle > 30 || !suitable) {
            doNothing = true;
        }


        mIsBeingDragged = !doNothing;
    }

    private float sX = -1, sY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLock) {
            gestureDetector.onTouchEvent(event);
            return super.onTouchEvent(event);
        } else {
            int action = event.getActionMasked();
            gestureDetector.onTouchEvent(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    viewDragHelper.processTouchEvent(event);
                    sX = event.getRawX();
                    sY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE: {
                    //the drag state and the direction are already judged at onInterceptTouchEvent
                    checkCanDrag(event);
                    if (mIsBeingDragged) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        viewDragHelper.processTouchEvent(event);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsBeingDragged = false;
                    viewDragHelper.processTouchEvent(event);
                    break;

                default://handle other action, such as ACTION_POINTER_DOWN/UP
                    viewDragHelper.processTouchEvent(event);
            }

            return super.onTouchEvent(event) || mIsBeingDragged || action == MotionEvent.ACTION_DOWN;
        }


    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private Rect hitSurfaceRect;

    private boolean isTouchOnSurface(MotionEvent ev) {
        if (contentView == null) {
            return false;
        }
        if (hitSurfaceRect == null) {
            hitSurfaceRect = new Rect();
        }
        contentView.getHitRect(hitSurfaceRect);
        return hitSurfaceRect.contains((int) ev.getX(), (int) ev.getY());
    }

    public void setOnDoubleClickListener(DoubleClickListener doubleClickListener) {
        mDoubleClickListener = doubleClickListener;
    }

    public void setOnSingleClickListener(SingleClickListener clickListener) {
//        为了产生点击效果
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mSingleClickListener = clickListener;
    }

    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    public boolean isLock() {
        return isLock;
    }

    public interface OnLayout {
        void onLayout(SwipeItemView v);
    }

    private List<OnLayout> mOnLayoutListeners;

    public void addOnLayoutListener(OnLayout l) {
        if (mOnLayoutListeners == null) mOnLayoutListeners = new ArrayList<>();
        mOnLayoutListeners.add(l);
    }

    public void removeOnLayoutListener(OnLayout l) {
        if (mOnLayoutListeners != null) mOnLayoutListeners.remove(l);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mOnLayoutListeners != null) for (int i = 0; i < mOnLayoutListeners.size(); i++) {
            mOnLayoutListeners.get(i).onLayout(this);
        }
    }

    public interface DoubleClickListener {
        void onDoubleClick(SwipeItemView itemView, boolean surface);
    }


    class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isEnabled() && mSingleClickListener != null && getOpenStatus() == Status.Close) {
                mSingleClickListener.onClick(SwipeItemView.this);
            } else if (isEnabled() && mSingleClickListener != null && isLock) {
                mSingleClickListener.onClick(SwipeItemView.this);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isEnabled() && mDoubleClickListener != null) {
                View target;
                View bottom = actionView;
                View surface = contentView;
                if (bottom != null && e.getX() > bottom.getLeft() && e.getX() < bottom.getRight()
                        && e.getY() > bottom.getTop() && e.getY() < bottom.getBottom()) {
                    target = bottom;
                } else {
                    target = surface;
                }
                mDoubleClickListener.onDoubleClick(SwipeItemView.this, target == surface);
            }
            return true;
        }
    }

    public interface SingleClickListener {
        void onClick(SwipeItemView view);
    }

    public interface SwipeListener {
        void onStartOpen(SwipeItemView layout);

        void onOpen(SwipeItemView layout);

        void onStartClose(SwipeItemView layout);

        void onClose(SwipeItemView layout);

    }
}
