package com.wang.baseadapter.delegate;

import android.view.ViewGroup;

import com.wang.baseadapter.model.ItemArray;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public abstract class AdapterDelegate<VH extends RecyclerView.ViewHolder> {


    /**
     * Creates the  {@link VH} for the given data source item
     *
     * @param parent The ViewGroup parent of the given datasource
     * @param viewType the datasource type
     * @return The new instantiated {@link VH}
     */
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the datas source set
     *
     * @param itemArray    The data source
     * @param position The position in the datasource
     * @param vh   The {@link RecyclerView.ViewHolder} to bind
     * @param payloads A non-null list of merged payloads. Can be empty list if requires full update.
     */
    public void onBindViewHolder(ItemArray itemArray, VH vh, int position, List<Object> payloads){
        onBindViewHolder(itemArray, vh, position);
    }

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the datas source set
     *
     * @param itemArray    The data source
     * @param position The position in the datasource
     * @param vh   The {@link RecyclerView.ViewHolder} to bind
     */
    public abstract void onBindViewHolder(ItemArray itemArray, VH vh, int position);

    /**
     * Called when a view created by this adapter has been recycled.
     * <p>
     * <p>A view is recycled when a {@link RecyclerView.LayoutManager} decides that it no longer
     * needs to be attached to its parent {@link RecyclerView}. This can be because it has
     * fallen out of visibility or a set of cached views represented by views still
     * attached to the parent RecyclerView. If an item view has large or expensive data
     * bound to it such as large bitmaps, this may be a good place to release those
     * resources.</p>
     * <p>
     * RecyclerView calls this method right before clearing ViewHolder's internal data and
     * sending it to RecycledViewPool. This way, if ViewHolder was holding valid information
     * before being recycled, you can call {@link RecyclerView.ViewHolder#getAdapterPosition()} to
     * get
     * its adapter position.
     *
     * @param vh The ViewHolder for the view being recycled
     */
    protected void onViewRecycled(VH vh) {
    }

    /**
     * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
     * due to its transient state. Upon receiving this callback, Adapter can clear the
     * animation(s) that effect the View's transient state and return <code>true</code> so that
     * the View can be recycled. Keep in mind that the View in question is already removed from
     * the RecyclerView.
     * <p>
     * In some cases, it is acceptable to recycle a View although it has transient state. Most
     * of the time, this is a case where the transient state will be cleared in
     * {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)} call when View is
     * rebound to a new position.
     * For this reason, RecyclerView leaves the decision to the Adapter and uses the return
     * value of this method to decide whether the View should be recycled or not.
     * <p>
     * Note that when all animations are created by {@link RecyclerView.ItemAnimator}, you
     * should never receive this callback because RecyclerView keeps those Views as children
     * until their animations are complete. This callback is useful when children of the item
     * views create animations which may not be easy to implement using an {@link
     * RecyclerView.ItemAnimator}.
     * <p>
     * You should <em>never</em> fix this issue by calling
     * <code>holder.itemView.setHasTransientState(false);</code> unless you've previously called
     * <code>holder.itemView.setHasTransientState(true);</code>. Each
     * <code>View.setHasTransientState(true)</code> call must be matched by a
     * <code>View.setHasTransientState(false)</code> call, otherwise, the state of the View
     * may become inconsistent. You should always prefer to end or cancel animations that are
     * triggering the transient state instead of handling it manually.
     *
     * @param vh The ViewHolder containing the View that could not be recycled due to its
     *               transient state.
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
     * the View and recycle it regardless. If this method returns <code>false</code>,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    protected boolean onFailedToRecycleView(VH vh) {
        return false;
    }

    /**
     * Called when a view created by this adapter has been attached to a window.
     * <p>
     * <p>This can be used as a reasonable signal that the view is about to be seen
     * by the user. If the adapter previously freed any resources in
     * {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)
     * onViewDetachedFromWindow}
     * those resources should be restored here.</p>
     *
     * @param vh Holder of the view being attached
     */
    protected void onViewAttachedToWindow(VH vh) {
    }

    /**
     * Called when a view created by this adapter has been detached from its window.
     * <p>
     * <p>Becoming detached from the window is not necessarily a permanent condition;
     * the consumer of an Adapter's views may choose to cache views offscreen while they
     * are not visible, attaching an detaching them as appropriate.</p>
     *
     * @param vh Holder of the view being detached
     */
    protected void onViewDetachedFromWindow(VH vh) {
    }
}
