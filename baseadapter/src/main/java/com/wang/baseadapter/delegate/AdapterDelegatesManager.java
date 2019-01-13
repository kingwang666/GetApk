package com.wang.baseadapter.delegate;

import android.animation.AnimatorSet;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.wang.baseadapter.BaseRecyclerViewAdapter;
import com.wang.baseadapter.animation.AlphaInAnimation;
import com.wang.baseadapter.animation.BaseAnimation;
import com.wang.baseadapter.animation.ScaleInAnimation;
import com.wang.baseadapter.animation.SlideInBottomAnimation;
import com.wang.baseadapter.animation.SlideInLeftAnimation;
import com.wang.baseadapter.animation.SlideInRightAnimation;
import com.wang.baseadapter.model.ItemArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


public class AdapterDelegatesManager {

    @IntDef({ALPHA_IN, SCALE_IN, SLIDE_IN_BOTTOM, SLIDE_IN_LEFT, SLIDE_IN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {

    }
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHA_IN = 1;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALE_IN = 2;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_BOTTOM = 3;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_LEFT = 4;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDE_IN_RIGHT = 5;

    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;

    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    private List<Integer> mNoAnimTypes;

    public AdapterDelegatesManager() {
        mNoAnimTypes = new ArrayList<>(4);
        mNoAnimTypes.add(BaseRecyclerViewAdapter.TYPE_EMPTY);
        mNoAnimTypes.add(BaseRecyclerViewAdapter.TYPE_FOOTER);
        mNoAnimTypes.add(BaseRecyclerViewAdapter.TYPE_HEADER);
        mNoAnimTypes.add(BaseRecyclerViewAdapter.TYPE_LOADING);
    }

    /**
     * Map for ViewType to AdapterDeleage
     */
    private SparseArrayCompat<AdapterDelegate> delegates = new SparseArrayCompat<>();

    /**
     * Adds an {@link AdapterDelegate}.
     * <b>This method automatically assign internally the view type integer by using the next
     * unused</b>
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, AdapterDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(int, AdapterDelegate)
     * @see #addDelegate(int, boolean, AdapterDelegate)
     */
    @Deprecated
    public AdapterDelegatesManager addDelegate(@NonNull AdapterDelegate delegate) {

        int viewType = delegates.size();
        while (delegates.get(viewType) != null) {
            viewType++;
        }
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Adds an {@link AdapterDelegate} with the specified view type.
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, AdapterDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param viewType the view type integer if you want to assign manually the view type. Otherwise
     *                 use {@link #addDelegate(AdapterDelegate)} where a viewtype will be assigned manually.
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(AdapterDelegate)
     * @see #addDelegate(int, boolean, AdapterDelegate)
     */
    public AdapterDelegatesManager addDelegate(int viewType, @NonNull AdapterDelegate delegate) {
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Adds an {@link AdapterDelegate}.
     *
     * @param viewType               The viewType id
     * @param allowReplacingDelegate if true, you allow to replacing the given delegate any previous
     *                               delegate for the same view type. if false, you disallow and a {@link IllegalArgumentException}
     *                               will be thrown if you try to replace an already registered {@link AdapterDelegate} for the
     *                               same view type.
     * @param delegate               The delegate to add
     * @throws IllegalArgumentException if <b>allowReplacingDelegate</b>  is false and an {@link
     *                                  AdapterDelegate} is already added (registered)
     *                                  with the same ViewType.
     * @see #addDelegate(AdapterDelegate)
     * @see #addDelegate(int, AdapterDelegate)
     */
    public AdapterDelegatesManager addDelegate(int viewType, boolean allowReplacingDelegate, @NonNull AdapterDelegate delegate) {

        if (delegate == null) {
            throw new NullPointerException("AdapterDelegate is null!");
        }

        if (!allowReplacingDelegate && delegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered AdapterDelegate is "
                            + delegates.get(viewType));
        }

        delegates.put(viewType, delegate);

        return this;
    }

    /**
     * Removes a previously registered delegate if and only if the passed delegate is registered
     * (checks the reference of the object). This will not remove any other delegate for the same
     * viewType (if there is any).
     *
     * @param delegate The delegate to remove
     * @return self
     */
    public AdapterDelegatesManager removeDelegate(@NonNull AdapterDelegate delegate) {

        if (delegate == null) {
            throw new NullPointerException("AdapterDelegate is null");
        }

        int indexToRemove = delegates.indexOfValue(delegate);

        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }
        return this;
    }

    /**
     * Removes the adapterDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public AdapterDelegatesManager removeDelegate(int viewType) {
        delegates.remove(viewType);
        return this;
    }


    /**
     * This method must be called
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return The new created ViewHolder
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdapterDelegate delegate = delegates.get(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }

        RecyclerView.ViewHolder vh = delegate.onCreateViewHolder(parent, viewType);
        if (vh == null) {
            throw new NullPointerException("ViewHolder returned from AdapterDelegate "
                    + delegate
                    + " for ViewType ="
                    + viewType
                    + " is null!");
        }
        return vh;
    }

    /**
     * Must be called
     *
     * @param itemArray Adapter's data source
     * @param vh    the ViewHolder to bind
     * @param position  the position in data source  @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                  viewType
     */
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ItemArray itemArray, RecyclerView.ViewHolder vh, int position) {
        int type = vh.getItemViewType();
        getDelegateForViewType(type).onBindViewHolder(itemArray, vh, position);
        addAnimation(vh, type);
    }


    /**
     * Must be called
     *
     * @param itemArray Adapter's data source
     * @param vh    the ViewHolder to bind
     * @param position  the position in data source  @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                  viewType
     */
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(ItemArray itemArray, RecyclerView.ViewHolder vh, int position, List<Object> payloads) {
        int type = vh.getItemViewType();
        getDelegateForViewType(type).onBindViewHolder(itemArray, vh, position, payloads);
        addAnimation(vh, type);
    }

    /**
     *
     * @param vh The ViewHolder for the view being recycled
     */
    @SuppressWarnings("unchecked")
    public void onViewRecycled(RecyclerView.ViewHolder vh){
        getDelegateForViewType(vh.getItemViewType()).onViewRecycled(vh);
    }

    @SuppressWarnings("unchecked")
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder vh) {
        return getDelegateForViewType(vh.getItemViewType()).onFailedToRecycleView(vh);
    }

    /**
     * @param vh Holder of the view being attached
     */
    @SuppressWarnings("unchecked")
    public void onViewAttachedToWindow(RecyclerView.ViewHolder vh) {
        getDelegateForViewType(vh.getItemViewType()).onViewAttachedToWindow(vh);
    }

    /**
     * @param vh Holder of the view being detached
     */
    @SuppressWarnings("unchecked")
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder vh) {
        getDelegateForViewType(vh.getItemViewType()).onViewDetachedFromWindow(vh);
    }


    /**
     * Get the {@link AdapterDelegate} associated with the given view type integer
     *
     * @param viewType The view type integer we want to retrieve the associated
     *                 delegate for.
     * @return The {@link AdapterDelegate} associated with the view type param if it exists,
     * the fallback delegate otherwise if it is set.
     * @throws NullPointerException if no delegates are associated with this view type
     *                              and if no fallback delegate is set.
     */
    @NonNull
    public AdapterDelegate getDelegateForViewType(int viewType) {
        AdapterDelegate delegate = delegates.get(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }
        return delegate;
    }

    public void addNoAnimType(int type){
        mNoAnimTypes.add(type);
    }

    public void resetAnimPosition(){
        mLastPosition = -1;
    }

    /**
     * 加入并开始动画
     *
     * @param holder 对应的viewHolder
     */
    private void addAnimation(RecyclerView.ViewHolder holder, int type) {
        if (mOpenAnimationEnable && mSelectAnimation != null && !mNoAnimTypes.contains(type)) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                AnimatorSet set = mSelectAnimation.getAnimators(holder.itemView);
                set.setDuration(mDuration);
                set.setInterpolator(mInterpolator);
                set.start();
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHA_IN}, {@link #SCALE_IN}, {@link #SLIDE_IN_BOTTOM}, {@link #SLIDE_IN_LEFT}, {@link #SLIDE_IN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        switch (animationType) {
            case ALPHA_IN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALE_IN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDE_IN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDE_IN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDE_IN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mSelectAnimation = animation;
    }

    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * 设置动画是否只有第一次有效果
     *
     * @param firstOnly true动画只显示一次
     */
    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }
}
