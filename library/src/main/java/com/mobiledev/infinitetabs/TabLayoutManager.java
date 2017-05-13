package com.mobiledev.infinitetabs;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mobiledev.infinitetabs.InfiniteTabAdapter.LOOPS_COUNT;


/**
 * Created by mdev3 on 4/11/17.
 */

public class TabLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    final static int LAYOUT_START = -1;
    final static int LAYOUT_END = 1;
    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    private int lengthofTAbs = 0;
    private int mFirstVisiblePosition = 0;
    private int mLastVisiblePos = 0;
    private int mInitialSelectedPosition = -1;
    int mCurSelectedPosition = -1;
    View mCurSelectedView;
    /**
     * Scroll state
     */
    private State mState;
    private LinearSnapHelper mSnapHelper = new LinearSnapHelper();
    private InnerScrollListener mInnerScrollListener = new InnerScrollListener();
    private boolean mCallbackInFling = false;
    private int mOrientation = HORIZONTAL;
    private OrientationHelper mHorizontalHelper;

    public int getOrientation() {
        return mOrientation;
    }

    public int getCurSelectedPosition() {
        return mCurSelectedPosition;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {

        return new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }


    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (getItemCount() == 0) {
            reset();
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (state.isPreLayout()) {
            return;
        }
        if (state.getItemCount() != 0 && !state.didStructureChange()) {

            return;
        }
        if (getChildCount() == 0 || state.didStructureChange()) {
            reset();
        }
        detachAndScrapAttachedViews(recycler);
        firstFillCover(recycler, state, 0);
    }


    private void reset() {

        if (mState != null) {
            mState.mItemsFrames.clear();
        }
        mCurSelectedPosition = -1;
        mFirstVisiblePosition = 0;
        if (mCurSelectedView != null) {
            mCurSelectedView.setSelected(false);
            mCurSelectedView = null;
        }
        mLastVisiblePos = 0;
    }


    private void firstFillCover(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDelta) {
        if (mInitialSelectedPosition < 0 || mInitialSelectedPosition >= getItemCount()) {
            fillCover(recycler, state, scrollDelta);
            return;
        }
        if (mOrientation == HORIZONTAL) {
            firstFillWithHorizontal(recycler, state);
        }



        if (mItemSelectionHighlightListener != null) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                Log.e("fddffdfdfdf===>", " "+i);
                mItemSelectionHighlightListener.highlightItem(this, child, calculateToCenterFraction(child, scrollDelta));
            }
        }
        mInnerScrollListener.onScrolled(mRecyclerView, 0, 0);
    }


    private void firstFillWithHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        int leftEdge = getOrientationHelper().getStartAfterPadding();
        int rightEdge = getOrientationHelper().getEndAfterPadding();
        int startPosition = mInitialSelectedPosition;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        int topOffset;
        //layout the init position view
        View scrap = recycler.getViewForPosition(mInitialSelectedPosition);
        addView(scrap, 0);
        measureChildWithMargins(scrap, 0, 0);
        scrapWidth = getDecoratedMeasuredWidth(scrap);
        scrapHeight = getDecoratedMeasuredHeight(scrap);
        topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
        int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
        scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight);
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
        if (getState().mItemsFrames.get(startPosition) == null) {
            getState().mItemsFrames.put(startPosition, scrapRect);
        } else {
            getState().mItemsFrames.get(startPosition).set(scrapRect);
        }
        mFirstVisiblePosition = mLastVisiblePos = startPosition;
        int leftStartOffset = getDecoratedLeft(scrap);
        int rightStartOffset = getDecoratedRight(scrap);
        //fill left of center
        fillLeft(recycler, mInitialSelectedPosition - 1, leftStartOffset, leftEdge);
        //fill right of center
        fillRight(recycler, mInitialSelectedPosition + 1, rightStartOffset, rightEdge);
    }

    private void fillLeft(RecyclerView.Recycler recycler, int startPosition, int startOffset, int leftEdge) {
        View scrap;
        int topOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        for (int i = startPosition; i > 0 && startOffset > leftEdge; i--) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap, 0);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
            scrapRect.set(startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.left;
            mFirstVisiblePosition = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
    }

    /**
     * Fill right of the center view
     *
     * @param recycler
     * @param startPosition start position to fill right
     * @param startOffset   layout start offset
     * @param rightEdge
     */
    private void fillRight(RecyclerView.Recycler recycler, int startPosition, int startOffset, int rightEdge) {
        View scrap;
        int topOffset;
        int scrapWidth, scrapHeight;
        Rect scrapRect = new Rect();
        int height = getVerticalSpace();
        for (int i = startPosition; i < getItemCount() && startOffset < rightEdge; i++) {
            scrap = recycler.getViewForPosition(i);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
            scrapRect.set(startOffset, topOffset, startOffset + scrapWidth, topOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            startOffset = scrapRect.right;
            mLastVisiblePos = i;
            if (getState().mItemsFrames.get(i) == null) {
                getState().mItemsFrames.put(i, scrapRect);
            } else {
                getState().mItemsFrames.get(i).set(scrapRect);
            }
        }
    }


    private void fillCover(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDelta) {
        if (getItemCount() == 0) {
            return;
        }


        fillWithHorizontal(recycler, state, scrollDelta);


        if (mItemSelectionHighlightListener != null) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                Log.e("fferererer===>", " "+i);
                mItemSelectionHighlightListener.highlightItem(this, child, calculateToCenterFraction(child, scrollDelta));
            }
        }
    }

    private float calculateToCenterFraction(View child, float pendingOffset) {
        int distance = calculateDistanceCenter(child, pendingOffset);
        int childLength = mOrientation == TabLayoutManager.HORIZONTAL ? child.getWidth() : child.getHeight();


        return Math.max(-1.f, Math.min(1.f, distance * 1.f / childLength));
    }

    /**
     * @param child
     * @param pendingOffset child view will scroll by
     * @return
     */
    private int calculateDistanceCenter(View child, float pendingOffset) {
        OrientationHelper orientationHelper = getOrientationHelper();
        int parentCenter = (orientationHelper.getEndAfterPadding() - orientationHelper.getStartAfterPadding()) / 2 + orientationHelper.getStartAfterPadding();

        return (int) (child.getWidth() / 2 - pendingOffset + child.getLeft() - parentCenter);


    }


    /**
     * @param recycler
     * @param state
     */
    private void fillWithHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        int leftEdge = getOrientationHelper().getStartAfterPadding();
        int rightEdge = getOrientationHelper().getEndAfterPadding();

        //1.remove and recycle the view that disappear in screen
        View child;
        if (getChildCount() > 0) {
            if (dx >= 0) {
                //remove and recycle the left off screen view
                int fixIndex = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    child = getChildAt(i + fixIndex);
                    if (getDecoratedRight(child) - dx < leftEdge) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                        fixIndex--;

                    } else {
                        break;
                    }
                }
            } else { //dx<0
                //remove and recycle the right off screen view
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (getDecoratedLeft(child) - dx > rightEdge) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;

                    }
                }
            }

        }
        //2.Add or reattach item view to fill screen
        int startPosition = mFirstVisiblePosition;
        int startOffset = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect;
        int height = getVerticalSpace();
        int topOffset;
        View scrap;
        if (dx >= 0) {
            if (getChildCount() != 0) {
                View lastView = getChildAt(getChildCount() - 1);
                startPosition = getPosition(lastView) + 1; //start layout from next position item
                startOffset = getDecoratedRight(lastView);

            }
            for (int i = startPosition; i < getItemCount() && startOffset < rightEdge + dx; i++) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                }
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = getDecoratedMeasuredWidth(scrap);
                scrapHeight = getDecoratedMeasuredHeight(scrap);
                topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
                if (startOffset == -1 && startPosition == 0) {
                    // layout the first position item in center
                    int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
                    scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight);
                } else {
                    scrapRect.set(startOffset, topOffset, startOffset + scrapWidth, topOffset + scrapHeight);
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.right;
                mLastVisiblePos = i;

            }
        } else {
            //dx<0
            if (getChildCount() > 0) {
                View firstView = getChildAt(0);
                startPosition = getPosition(firstView) - 1; //start layout from previous position item
                startOffset = getDecoratedLeft(firstView);

            }
            for (int i = startPosition; i >= 0 && startOffset > leftEdge + dx; i--) {
                scrapRect = getState().mItemsFrames.get(i);
                scrap = recycler.getViewForPosition(i);
                addView(scrap, 0);
                if (scrapRect == null) {
                    scrapRect = new Rect();
                    getState().mItemsFrames.put(i, scrapRect);
                }
                measureChildWithMargins(scrap, 0, 0);
                scrapWidth = getDecoratedMeasuredWidth(scrap);
                scrapHeight = getDecoratedMeasuredHeight(scrap);
                topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.0f);
                scrapRect.set(startOffset - scrapWidth, topOffset, startOffset, topOffset + scrapHeight);
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
                startOffset = scrapRect.left;
                mFirstVisiblePosition = i;
            }
        }
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    public State getState() {
        if (mState == null) {
            mState = new State();
        }
        return mState;
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return LAYOUT_START;
        }
        final int firstChildPos = mFirstVisiblePosition;
        return position < firstChildPos ? LAYOUT_START : LAYOUT_END;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (mOrientation == HORIZONTAL) {
            outVector.x = direction;
            outVector.y = 0;
        }
        return outVector;
    }


    class State {
        /**
         * Record all item view 's last position after last layout
         */
        SparseArray<Rect> mItemsFrames;

        /**
         * RecycleView 's current scroll distance since first layout
         */
        int mScrollDelta;

        public State() {
            mItemsFrames = new SparseArray<Rect>();
            mScrollDelta = 0;
        }
    }


    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }



    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // When dx is positive，finger fling from right to left(←)，scrollX+
        if (getChildCount() == 0 || dx == 0) {
            return 0;
        }
        int delta = -dx;
        int parentCenter = (getOrientationHelper().getEndAfterPadding() - getOrientationHelper().getStartAfterPadding()) / 2 + getOrientationHelper().getStartAfterPadding();
        View child;
        if (dx > 0) {
            //If we've reached the last item, enforce limits
            if (getPosition(getChildAt(getChildCount() - 1)) == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dx, (child.getRight() - child.getLeft()) / 2 + child.getLeft() - parentCenter));
            }
        } else {
            //If we've reached the first item, enforce limits
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dx, ((child.getRight() - child.getLeft()) / 2 + child.getLeft()) - parentCenter));
            }
        }

        getState().mScrollDelta = -delta;
        fillCover(recycler, state, -delta);
        offsetChildrenHorizontal(delta);
        return -delta;
    }


    public OrientationHelper getOrientationHelper() {

        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(this);
        }
        return mHorizontalHelper;

    }


    public static class LayoutParams extends RecyclerView.LayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }


    private ItemSelectionHighlightListener mItemSelectionHighlightListener = new HighlightedItem();




    public interface ItemSelectionHighlightListener {

        void highlightItem(TabLayoutManager layoutManager, View item, float fraction);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(RecyclerView recyclerView, View item, int position);
    }

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public void attach(RecyclerView recyclerView, List<String> TabList) {
        lengthofTAbs = TabList.size();
        int selectedPosition = TabList.size() * LOOPS_COUNT / 2;
        if (recyclerView == null) {
            throw new IllegalArgumentException("The attach RecycleView must not null!!");
        }
        mRecyclerView = recyclerView;
        mInitialSelectedPosition = selectedPosition;
        recyclerView.setLayoutManager(this);
        mSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mInnerScrollListener);
    }

    RecyclerView mRecyclerView;




    private class InnerScrollListener extends RecyclerView.OnScrollListener {
        int mState;
        boolean mCallbackOnIdle;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            View snap = mSnapHelper.findSnapView(recyclerView.getLayoutManager());
            if (snap != null) {
                int selectedPosition = recyclerView.getLayoutManager().getPosition(snap);
                if (selectedPosition != mCurSelectedPosition) {
                    if (mCurSelectedView != null) {
                        mCurSelectedView.setSelected(false);
                    }
                    mCurSelectedView = snap;
                    mCurSelectedView.setSelected(true);
                    mCurSelectedPosition = selectedPosition;
                    if (!mCallbackInFling && mState != SCROLL_STATE_IDLE) {

                        mCallbackOnIdle = true;
                        return;
                    }
                    if (mOnItemSelectedListener != null) {
                        if (mCurSelectedPosition >= lengthofTAbs) {
                            mCurSelectedPosition = mCurSelectedPosition % lengthofTAbs;
                        }
                        mOnItemSelectedListener.onItemSelected(recyclerView, snap, mCurSelectedPosition);
                    }
                }
            }

        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            mState = newState;

            if (mState == SCROLL_STATE_IDLE) {
                View snap = mSnapHelper.findSnapView(recyclerView.getLayoutManager());
                if (snap != null) {
                    int selectedPosition = recyclerView.getLayoutManager().getPosition(snap);
                    if (selectedPosition != mCurSelectedPosition) {
                        if (mCurSelectedView != null) {
                            mCurSelectedView.setSelected(false);
                        }
                        mCurSelectedView = snap;
                        mCurSelectedView.setSelected(true);
                        mCurSelectedPosition = selectedPosition;
                        if (mOnItemSelectedListener != null) {
                            if (mCurSelectedPosition >= lengthofTAbs) {
                                mCurSelectedPosition = mCurSelectedPosition % lengthofTAbs;
                            }
                            mOnItemSelectedListener.onItemSelected(recyclerView, snap, mCurSelectedPosition);
                        }
                    } else if (!mCallbackInFling && mOnItemSelectedListener != null && mCallbackOnIdle) {
                        mCallbackOnIdle = false;
                        if (mCurSelectedPosition >= lengthofTAbs) {
                            mCurSelectedPosition = mCurSelectedPosition % lengthofTAbs;
                        }
                        mOnItemSelectedListener.onItemSelected(recyclerView, snap, mCurSelectedPosition);
                    }
                }
            }
        }
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        GallerySmoothScroller linearSmoothScroller = new GallerySmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }


    private class GallerySmoothScroller extends LinearSmoothScroller {

        public GallerySmoothScroller(Context context) {
            super(context);
        }


        public int calculateDxToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
            final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
            final int start = layoutManager.getPaddingLeft();
            final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
            final int childCenter = left + (int) ((right - left) / 2.0f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }


        public int calculateDyToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollVertically()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                    view.getLayoutParams();
            final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
            final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
            final int start = layoutManager.getPaddingTop();
            final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
            final int childCenter = top + (int) ((bottom - top) / 2.0f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }


        @Override
        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
            final int dx = calculateDxToMakeCentral(targetView);
            final int dy = calculateDyToMakeCentral(targetView);
            final int distance = (int) Math.sqrt(dx * dx + dy * dy);
            final int time = calculateTimeForDeceleration(distance);
            if (time > 0) {
                action.update(-dx, -dy, time, mDecelerateInterpolator);
            }
        }
    }
}
