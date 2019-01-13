package com.wang.baseadapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private int mHeaderPosition;
    private int mCurrentItemType;
    private int mStickyHeaderTop;

    private boolean mIsAdapterDataChanged;

    private Rect mClipBounds;
    private RecyclerView.Adapter mAdapter;
    private View mStickyView;

    private Drawable mBackground;


    private final SparseArray<StickyHeaderCreator> mTypeStickyHeaderFactories = new SparseArray<>();
    private final RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mIsAdapterDataChanged = true;
        }
    };

    public StickyHeaderDecoration() {
        this.mHeaderPosition = -1;
        this.mCurrentItemType = -1;
    }


    public StickyHeaderDecoration(int viewType) {
        this.mHeaderPosition = -1;
        this.mCurrentItemType = -1;
        registerTypePinnedHeader(viewType);
    }

    public StickyHeaderDecoration(int viewType, Drawable background) {
        this.mHeaderPosition = -1;
        this.mCurrentItemType = -1;
        registerTypePinnedHeader(viewType);
        mBackground = background;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        createPinnedHeader(parent);

        if (mStickyView != null) {
            View v = parent.findChildViewUnder(c.getWidth() / 2, mStickyView.getHeight() + 0.5f);
//            View firstVisibleItemView = parent.getLayoutManager().getChildAt(0);
//            int firstVisiblePosition = ((RecyclerView.LayoutParams) firstVisibleItemView.getLayoutParams()).getViewAdapterPosition();
            if (isStickyView(parent, v)) {
                mStickyHeaderTop = v.getTop() - mStickyView.getHeight();
            } else {
                mStickyHeaderTop = 0;
            }
            mClipBounds = c.getClipBounds();
            mClipBounds.top = mStickyHeaderTop;
            mClipBounds.bottom = mStickyHeaderTop + mStickyView.getHeight();
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mStickyView != null) {
            c.save();

            mClipBounds.top = 0;
            c.clipRect(mClipBounds);
            c.translate(0, mStickyHeaderTop);
            if (mBackground != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mStickyView.setBackground(mBackground);
                } else {
                    mStickyView.setBackgroundDrawable(mBackground);
                }
            }
            mStickyView.draw(c);
            c.restore();
        }
    }

    /**
     * Gets the position of the header under the specified (x, y) coordinates.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return position of header, or -1 if not found
     */
    public int findHeaderPositionUnder(int x, int y) {
        if (mStickyView != null) {
            if (x >= 0 && x < mStickyView.getRight() && y >= 0 && y < mStickyView.getBottom() + mStickyHeaderTop) {
                return mHeaderPosition;
            }
        }
        return -1;
    }

    public int findCurrentHeaderViewType() {
        return mCurrentItemType;
    }

    protected void createPinnedHeader(RecyclerView parent) {
        updateStickyHeader(parent);

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null || layoutManager.getChildCount() <= 0) {
            return;
        }
        int firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        int firstCompletelyVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        int headerPosition = findStickyHeaderPosition(parent, firstVisiblePosition);
        if (headerPosition == -1 || (headerPosition == firstCompletelyVisiblePosition)) {
            resetPinnedHeader();
            return;
        }
        if (headerPosition >= 0 && mHeaderPosition != headerPosition) {
            mHeaderPosition = headerPosition;
            int viewType = mAdapter.getItemViewType(headerPosition);
            mCurrentItemType = viewType;

            RecyclerView.ViewHolder stickyViewHolder = mAdapter.createViewHolder(parent, viewType);
            if (mAdapter instanceof SwipeStickyAdapter) {
                ((SwipeStickyAdapter) mAdapter).onBindSwipeViewHolder(stickyViewHolder, headerPosition);
            } else {
                mAdapter.onBindViewHolder(stickyViewHolder, headerPosition);
            }

            mStickyView = stickyViewHolder.itemView;


            // read layout parameters
            ViewGroup.LayoutParams layoutParams = mStickyView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mStickyView.setLayoutParams(layoutParams);
            }

            int heightMode = View.MeasureSpec.getMode(layoutParams.height);
            int heightSize = View.MeasureSpec.getSize(layoutParams.height);

            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                heightMode = View.MeasureSpec.EXACTLY;
            }

            int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
            if (heightSize > maxHeight) {
                heightSize = maxHeight;
            }

            // measure & layout
            int ws = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
            int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
            mStickyView.measure(ws, hs);
            mStickyView.layout(0, 0, mStickyView.getMeasuredWidth(), mStickyView.getMeasuredHeight());
        }
    }

    private int findStickyHeaderPosition(RecyclerView parent, int fromPosition) {
        if (fromPosition > mAdapter.getItemCount() || fromPosition < 0) {
            return -1;
        }

        for (int position = fromPosition; position >= 0; position--) {
            final int viewType = mAdapter.getItemViewType(position);
            if (isStickyViewType(parent, position, viewType)) {
                return position;
            }
        }

        return -1;
    }

    private boolean isStickyViewType(RecyclerView parent, int adapterPosition, int viewType) {
        StickyHeaderCreator stickyHeaderCreator = mTypeStickyHeaderFactories.get(viewType);

        return stickyHeaderCreator != null && stickyHeaderCreator.create(parent, adapterPosition);
    }

    private boolean isStickyView(RecyclerView parent, View v) {
        if (v == null) {
            return false;
        }
        int position = parent.getChildAdapterPosition(v);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }

        return isStickyViewType(parent, position, mAdapter.getItemViewType(position));
    }

    private void updateStickyHeader(RecyclerView parent) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter || mIsAdapterDataChanged) {
            resetPinnedHeader();
            if (mAdapter != null) {
                mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
            }

            mAdapter = adapter;
            if (mAdapter != null) {
                mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
            }
        }
    }

    private void resetPinnedHeader() {
        mHeaderPosition = -1;
        mCurrentItemType = -1;
        mStickyView = null;
    }

    public View getStickyView() {
        return mStickyView;
    }

    public void registerTypePinnedHeader(int itemType, StickyHeaderCreator stickyHeaderCreator) {
        mTypeStickyHeaderFactories.put(itemType, stickyHeaderCreator);
    }

    public void registerTypePinnedHeader(int itemType) {
        mTypeStickyHeaderFactories.put(itemType, new StickyHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
    }


    public interface StickyHeaderCreator {
        boolean create(RecyclerView parent, int adapterPosition);
    }
}
