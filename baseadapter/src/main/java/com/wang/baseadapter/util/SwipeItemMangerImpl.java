package com.wang.baseadapter.util;


import com.wang.baseadapter.listener.SimpleSwipeListener;
import com.wang.baseadapter.widget.SwipeItemView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * SwipeItemMangerImpl is a helper class to help all the adapters to maintain open status.
 */
public class SwipeItemMangerImpl implements SwipeItemMangerInterface {

    private SwipeItemView.Mode mode = SwipeItemView.Mode.Single;
    public final int INVALID_POSITION = -1;

    protected int mOpenPosition = INVALID_POSITION;

    protected Set<Integer> mOpenPositions = new HashSet<>();
    protected Set<SwipeItemView> mShownLayouts = new HashSet<>();

    protected SwipeAdapterInterface swipeAdapterInterface;

    public SwipeItemMangerImpl(SwipeAdapterInterface swipeAdapterInterface) {
        if (swipeAdapterInterface == null)
            throw new IllegalArgumentException("SwipeAdapterInterface can not be null");

        this.swipeAdapterInterface = swipeAdapterInterface;
    }

    public SwipeItemView.Mode getMode() {
        return mode;
    }

    public void setMode(SwipeItemView.Mode mode) {
        this.mode = mode;
        mOpenPositions.clear();
        mShownLayouts.clear();
        mOpenPosition = INVALID_POSITION;
    }

    public void bind(SwipeItemView swipeLayout, int position) {


        if (swipeLayout == null)
            throw new IllegalStateException("can not find SwipeLayout in target view");

        if (swipeLayout.getTag() == null) {
            OnLayoutListener onLayoutListener = new OnLayoutListener(position);
            SwipeMemory swipeMemory = new SwipeMemory(position);
            swipeLayout.addSwipeListener(swipeMemory);
            swipeLayout.addOnLayoutListener(onLayoutListener);
            swipeLayout.setTag(new ValueBox(position, swipeMemory, onLayoutListener));
            mShownLayouts.add(swipeLayout);
        } else {
            ValueBox valueBox = (ValueBox) swipeLayout.getTag();
            valueBox.swipeMemory.setPosition(position);
            valueBox.onLayoutListener.setPosition(position);
            valueBox.position = position;
        }
    }

    @Override
    public void openItem(int position) {
        if (mode == SwipeItemView.Mode.Multiple) {
            if (!mOpenPositions.contains(position))
                mOpenPositions.add(position);
        } else {
            mOpenPosition = position;
        }
        swipeAdapterInterface.notifyDatasetChanged();
    }

    @Override
    public void closeItem(int position) {
        if (mode == SwipeItemView.Mode.Multiple) {
            mOpenPositions.remove(position);
        } else {
            if (mOpenPosition == position)
                mOpenPosition = INVALID_POSITION;
        }
        swipeAdapterInterface.notifyDatasetChanged();
    }

    @Override
    public void closeAllExcept(SwipeItemView layout) {
        for (SwipeItemView s : mShownLayouts) {
            if (s != layout)
                s.close();
        }
    }

    @Override
    public void closeAllItems() {
        if (mode == SwipeItemView.Mode.Multiple) {
            mOpenPositions.clear();
        } else {
            mOpenPosition = INVALID_POSITION;
        }
        for (SwipeItemView s : mShownLayouts) {
            s.close();
        }
    }

    @Override
    public void removeShownLayouts(SwipeItemView layout) {
        mShownLayouts.remove(layout);
    }

    @Override
    public List<Integer> getOpenItems() {
        if (mode == SwipeItemView.Mode.Multiple) {
            return new ArrayList<>(mOpenPositions);
        } else {
            return Collections.singletonList(mOpenPosition);
        }
    }

    @Override
    public List<SwipeItemView> getOpenLayouts() {
        return new ArrayList<>(mShownLayouts);
    }

    @Override
    public boolean isOpen(int position) {
        if (mode == SwipeItemView.Mode.Multiple) {
            return mOpenPositions.contains(position);
        } else {
            return mOpenPosition == position;
        }
    }

    class ValueBox {
        OnLayoutListener onLayoutListener;
        SwipeMemory swipeMemory;
        int position;

        ValueBox(int position, SwipeMemory swipeMemory, OnLayoutListener onLayoutListener) {
            this.swipeMemory = swipeMemory;
            this.onLayoutListener = onLayoutListener;
            this.position = position;
        }
    }

    class OnLayoutListener implements SwipeItemView.OnLayout {

        private int position;

        OnLayoutListener(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onLayout(SwipeItemView v) {
            if (isOpen(position)) {
                v.open(false, false);
            } else {
                v.close(false, false);
            }
        }

    }

    class SwipeMemory extends SimpleSwipeListener {

        private int position;

        SwipeMemory(int position) {
            this.position = position;
        }

        @Override
        public void onClose(SwipeItemView layout) {
            if (mode == SwipeItemView.Mode.Multiple) {
                mOpenPositions.remove(position);
            } else if (mOpenPosition == position){
                mOpenPosition = INVALID_POSITION;

            }
        }

        @Override
        public void onStartOpen(SwipeItemView layout) {
            if (mode == SwipeItemView.Mode.Single) {
                closeAllExcept(layout);
            }
        }

        @Override
        public void onOpen(SwipeItemView layout) {
            if (mode == SwipeItemView.Mode.Multiple)
                mOpenPositions.add(position);
            else {
//                closeAllExcept(layout);
                mOpenPosition = position;
            }
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

}
