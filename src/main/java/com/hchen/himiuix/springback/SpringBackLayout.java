/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * HiMiuiX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 HiMiuiX Contributions
 */
package com.hchen.himiuix.springback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Looper;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ListViewCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

import java.util.ArrayList;
import java.util.List;

/** @noinspection NullableProblems*/
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3, NestedCurrentFling, ScrollStateDispatcher {
    private final NestedScrollingChildHelper mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
    private final NestedScrollingParentHelper mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    private final List<ViewCompatOnScrollChangeListener> mOnScrollChangeListeners = new ArrayList<>();
    private final SpringScroller mSpringScroller = new SpringScroller();
    private final int[] mNestedScrollingV2ConsumedCompat = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private final int[] mParentScrollConsumed = new int[2];
    private final SpringBackLayoutHelper mHelper;
    private int consumeNestFlingCounter = 0;
    private int mActivePointerId = -1;
    private int mFakeScrollX;
    private int mFakeScrollY;
    private int mInitPaddingTop;
    private float mInitialDownX;
    private float mInitialDownY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private boolean mNestedFlingInProgress;
    private int mNestedScrollAxes;
    private boolean mNestedScrollInProgress;
    private int mOriginScrollOrientation;
    protected int mScreenHeight;
    protected int mScreenWidth;
    private boolean mScrollByFling;
    private int mScrollOrientation;
    private int mScrollState = 0;
    private boolean mSpringBackEnable = true;
    private final int mSpringBackMode;
    private View mTarget;
    private final int mTargetId;
    private float mTotalFlingUnconsumed;
    private float mTotalScrollBottomUnconsumed;
    private float mTotalScrollTopUnconsumed;
    private final int mTouchSlop;
    private float mVelocityX;
    private float mVelocityY;

    public SpringBackLayout(Context context) {
        this(context, null);
    }

    public SpringBackLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SpringBackLayout);
        mTargetId = obtainStyledAttributes.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        mOriginScrollOrientation = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
        mSpringBackMode = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        obtainStyledAttributes.recycle();

        mHelper = new SpringBackLayoutHelper(this, mOriginScrollOrientation);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setNestedScrollingEnabled(true);

        Point screenSize = MiuiXUtils.getScreenSize(context);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInitPaddingTop = getPaddingTop();
    }

    public void setSpringBackEnable(boolean z) {
        mSpringBackEnable = z;
    }

    public void setScrollOrientation(int i) {
        mOriginScrollOrientation = i;
        mHelper.mTargetScrollOrientation = i;
    }

    private int getFakeScrollX() {
        return mFakeScrollX;
    }

    private int getFakeScrollY() {
        return mFakeScrollY;
    }

    public int getSpringScrollX() {
        if (mSpringBackEnable) {
            return getScrollX();
        }
        return getFakeScrollX();
    }

    public int getSpringScrollY() {
        if (mSpringBackEnable) {
            return getScrollY();
        }
        return getFakeScrollY();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!(mTarget instanceof NestedScrollingChild3) || enabled == mTarget.isNestedScrollingEnabled()) {
            return;
        }
        mTarget.setNestedScrollingEnabled(enabled);
    }

    private boolean supportTopSpringBackMode() {
        return (mSpringBackMode & 1) != 0;
    }

    private boolean supportBottomSpringBackMode() {
        return (mSpringBackMode & 2) != 0;
    }

    public void setTarget(View view) {
        mTarget = view;
        if ((view instanceof NestedScrollingChild3) && !view.isNestedScrollingEnabled()) {
            mTarget.setNestedScrollingEnabled(true);
        }
        if (mTarget.getOverScrollMode() == 2 || !mSpringBackEnable) {
            return;
        }
        mTarget.setOverScrollMode(2);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (mTargetId == -1)
                throw new IllegalArgumentException("invalid target Id");

            mTarget = findViewById(mTargetId);
        }
        if (mTarget == null) {
            throw new IllegalArgumentException("fail to get target");
        }
        if (isEnabled()) {
            if ((mTarget instanceof NestedScrollingChild3) && !mTarget.isNestedScrollingEnabled()) {
                mTarget.setNestedScrollingEnabled(true);
            }
        }
        if (mTarget.getOverScrollMode() == 2 || !mSpringBackEnable) {
            return;
        }
        mTarget.setOverScrollMode(2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTarget.getVisibility() != View.GONE) {
            int measuredWidth = mTarget.getMeasuredWidth();
            int measuredHeight = mTarget.getMeasuredHeight();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            mTarget.layout(paddingLeft, paddingTop, measuredWidth + paddingLeft, measuredHeight + paddingTop);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int min;
        int min2;
        ensureTarget();
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        int mode2 = View.MeasureSpec.getMode(heightMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);
        if (mode == 0) {
            min = mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        } else if (mode == 1073741824) {
            min = View.MeasureSpec.getSize(widthMeasureSpec);
        } else {
            min = Math.min(View.MeasureSpec.getSize(widthMeasureSpec), mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
        }

        if (mode2 == 0) {
            min2 = mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        } else if (mode2 == 1073741824) {
            min2 = View.MeasureSpec.getSize(heightMeasureSpec);
        } else {
            min2 = Math.min(View.MeasureSpec.getSize(heightMeasureSpec), mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        }

        setMeasuredDimension(min, min2);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mSpringScroller.computeScrollOffset()) {
            scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
            if (!mSpringScroller.isFinished()) {
                AnimationHelper.postInvalidateOnAnimation(this);
                return;
            }
            if (getSpringScrollX() != 0 || getSpringScrollY() != 0) {
                if (mScrollState != 2) {
                    Log.d("SpringBackLayout", "Scroll stop but state is not correct.");
                    springBack(mNestedScrollAxes != 2 ? 1 : 2);
                    return;
                }
            }
            dispatchScrollState(0);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (mSpringBackEnable) {
            super.scrollTo(x, y);
            return;
        }
        if (mFakeScrollX == x && mFakeScrollY == y) {
            return;
        }

        onScrollChanged(x, y, mFakeScrollX, mFakeScrollY);
        mFakeScrollX = x;
        mFakeScrollY = y;
        if (!awakenScrollBars()) {
            postInvalidateOnAnimation();
        }
        requestLayout();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (ViewCompatOnScrollChangeListener mOnScrollChangeListener : mOnScrollChangeListeners) {
            mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    private boolean isTargetScrollOrientation(int ori) {
        return mScrollOrientation == ori;
    }

    private boolean isTargetScrollToTop(int i) {
        if (i == 2) {
            if (mTarget instanceof ListView listView) {
                return !ListViewCompat.canScrollList(listView, -1);
            }
            return !mTarget.canScrollVertically(-1);
        }
        return !mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollToBottom(int i) {
        if (i == 2) {
            if (mTarget instanceof ListView listView) {
                return !ListViewCompat.canScrollList(listView, 1);
            }
            return !mTarget.canScrollVertically(1);
        }
        return !mTarget.canScrollHorizontally(1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && mScrollState == 2 && mHelper.isTouchInTarget(motionEvent)) {
            dispatchScrollState(1);
        }
        boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
        if (motionEvent.getActionMasked() == 1 && mScrollState != 2) {
            dispatchScrollState(0);
        }
        return dispatchTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!mSpringBackEnable || !isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || mTarget.isNestedScrollingEnabled()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (!mSpringScroller.isFinished() && actionMasked == 0) {
            mSpringScroller.forceStop();
        }
        if (!supportTopSpringBackMode() && !supportBottomSpringBackMode()) {
            return false;
        }

        if ((mOriginScrollOrientation & 4) != 0) {
            checkOrientation(motionEvent);
            if (isTargetScrollOrientation(2) && (mOriginScrollOrientation & 1) != 0 && getScrollX() == 0.0f) {
                return false;
            }
            if (isTargetScrollOrientation(1) && (mOriginScrollOrientation & 2) != 0 && getScrollY() == 0.0f) {
                return false;
            }
            if (isTargetScrollOrientation(2) || isTargetScrollOrientation(1)) {
                disallowParentInterceptTouchEvent(true);
            }
        } else {
            mScrollOrientation = mOriginScrollOrientation;
        }
        if (isTargetScrollOrientation(2)) {
            return onVerticalInterceptTouchEvent(motionEvent);
        }
        if (isTargetScrollOrientation(1)) {
            return onHorizontalInterceptTouchEvent(motionEvent);
        }
        return false;
    }

    private void disallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void checkOrientation(MotionEvent motionEvent) {
        mHelper.checkOrientation(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            SpringBackLayoutHelper springBackLayoutHelper = mHelper;
            mInitialDownY = springBackLayoutHelper.mInitialDownY;
            mInitialDownX = springBackLayoutHelper.mInitialDownX;
            mActivePointerId = springBackLayoutHelper.mActivePointerId;
            if (getScrollY() != 0) {
                mScrollOrientation = 2;
                requestDisallowParentInterceptTouchEvent(true);
            } else if (getScrollX() != 0) {
                mScrollOrientation = 1;
                requestDisallowParentInterceptTouchEvent(true);
            } else {
                mScrollOrientation = 0;
            }
            if ((mOriginScrollOrientation & 2) != 0) {
                checkScrollStart(2);
            } else {
                checkScrollStart(1);
            }
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (mScrollOrientation != 0 || mHelper.mScrollOrientation == 0) {
                    return;
                }
                mScrollOrientation = mHelper.mScrollOrientation;
                return;
            }
            if (actionMasked != 3) {
                if (actionMasked != 6) {
                    return;
                }
                onSecondaryPointerUp(motionEvent);
                return;
            }
        }
        disallowParentInterceptTouchEvent(false);
        if ((mOriginScrollOrientation & 2) != 0) {
            springBack(2);
        } else {
            springBack(1);
        }
    }

    private boolean onVerticalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2)) {
            return false;
        }
        if (isTargetScrollToTop(2) && !supportTopSpringBackMode()) {
            return false;
        }
        if (isTargetScrollToBottom(2) && !supportBottomSpringBackMode()) {
            return false;
        }

        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            int pointerId = motionEvent.getPointerId(0);
            mActivePointerId = pointerId;
            int findPointerIndex = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex < 0) {
                return false;
            }
            mInitialDownY = motionEvent.getY(findPointerIndex);
            if (getScrollY() != 0) {
                mIsBeingDragged = true;
                mInitialMotionY = mInitialDownY;
            } else {
                mIsBeingDragged = false;
            }
        } else {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (mActivePointerId == -1) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                        return false;
                    }
                    int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    float y = motionEvent.getY(findPointerIndex2);
                    if (isTargetScrollToBottom(2) && isTargetScrollToTop(2)) {
                        z = true;
                    }
                    if ((z || !isTargetScrollToTop(2)) && (!z || y <= mInitialDownY)) {
                        if (mInitialDownY - y > mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionY = y;
                        }
                    } else if (y - mInitialDownY > mTouchSlop && !mIsBeingDragged) {
                        mIsBeingDragged = true;
                        dispatchScrollState(1);
                        mInitialMotionY = y;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            mIsBeingDragged = false;
            mActivePointerId = -1;
        }
        return mIsBeingDragged;
    }

    private boolean onHorizontalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1)) {
            return false;
        }
        if (isTargetScrollToTop(1) && !supportTopSpringBackMode()) {
            return false;
        }
        if (isTargetScrollToBottom(1) && !supportBottomSpringBackMode()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            int pointerId = motionEvent.getPointerId(0);
            mActivePointerId = pointerId;
            int findPointerIndex = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex < 0) {
                return false;
            }
            mInitialDownX = motionEvent.getX(findPointerIndex);
            if (getScrollX() != 0) {
                mIsBeingDragged = true;
                mInitialMotionX = mInitialDownX;
            } else {
                mIsBeingDragged = false;
            }
        } else {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (mActivePointerId == -1) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                        return false;
                    }
                    int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    float x = motionEvent.getX(findPointerIndex2);
                    if (isTargetScrollToBottom(1) && isTargetScrollToTop(1)) {
                        z = true;
                    }
                    if ((z || !isTargetScrollToTop(1)) && (!z || x <= mInitialDownX)) {
                        if (mInitialDownX - x > mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionX = x;
                        }
                    } else if (x - mInitialDownX > mTouchSlop && !mIsBeingDragged) {
                        mIsBeingDragged = true;
                        dispatchScrollState(1);
                        mInitialMotionX = x;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            mIsBeingDragged = false;
            mActivePointerId = -1;
        }
        return mIsBeingDragged;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (isEnabled() && mSpringBackEnable) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void internalRequestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
    }

    public void requestDisallowParentInterceptTouchEvent(boolean z) {
        ViewParent parent = getParent();
        parent.requestDisallowInterceptTouchEvent(z);
        while (parent != null) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(z);
            }
            parent = parent.getParent();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || mTarget.isNestedScrollingEnabled()) {
            return false;
        }
        if (!mSpringScroller.isFinished() && actionMasked == 0) {
            mSpringScroller.forceStop();
        }
        if (isTargetScrollOrientation(2)) {
            return onVerticalTouchEvent(motionEvent);
        }
        if (isTargetScrollOrientation(1)) {
            return onHorizontalTouchEvent(motionEvent);
        }
        return false;
    }

    private boolean onHorizontalTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1)) {
            return onScrollEvent(motionEvent, actionMasked, 1);
        }
        if (isTargetScrollToBottom(1)) {
            return onScrollUpEvent(motionEvent, actionMasked, 1);
        }
        return onScrollDownEvent(motionEvent, actionMasked, 1);
    }

    private boolean onVerticalTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2)) {
            return onScrollEvent(motionEvent, actionMasked, 2);
        }
        if (isTargetScrollToBottom(2)) {
            return onScrollUpEvent(motionEvent, actionMasked, 2);
        }
        return onScrollDownEvent(motionEvent, actionMasked, 2);
    }

    private boolean onScrollEvent(MotionEvent motionEvent, int actionMasked, int i) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (actionMasked == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i);
        } else {
            if (actionMasked == 1) {
                if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(i);
                }
                mActivePointerId = -1;
                return false;
            }
            if (actionMasked == 2) {
                int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    if (i == 2) {
                        float y = motionEvent.getY(findPointerIndex);
                        signum = Math.signum(y - mInitialMotionY);
                        obtainSpringBackDistance = obtainSpringBackDistance(y - mInitialMotionY, i);
                    } else {
                        float x = motionEvent.getX(findPointerIndex);
                        signum = Math.signum(x - mInitialMotionX);
                        obtainSpringBackDistance = obtainSpringBackDistance(x - mInitialMotionX, i);
                    }
                    requestDisallowParentInterceptTouchEvent(true);
                    moveTarget(signum * obtainSpringBackDistance, i);
                }
            } else {
                if (actionMasked == 3) {
                    return false;
                }
                if (actionMasked == 5) {
                    int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                        return false;
                    }
                    if (i == 2) {
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                        float y3 = motionEvent.getY(actionIndex) - y2;
                        mInitialDownY = y3;
                        mInitialMotionY = y3;
                    } else {
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                        float x3 = motionEvent.getX(actionIndex) - x2;
                        mInitialDownX = x3;
                        mInitialMotionX = x3;
                    }
                    mActivePointerId = motionEvent.getPointerId(actionIndex);
                } else if (actionMasked == 6) {
                    onSecondaryPointerUp(motionEvent);
                }
            }
        }
        return true;
    }

    private void checkVerticalScrollStart(int i) {
        if (getScrollY() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollY()), Math.abs(obtainMaxSpringBackDistance(i)), 2);
            if (getScrollY() < 0) {
                mInitialDownY -= obtainTouchDistance;
            } else {
                mInitialDownY += obtainTouchDistance;
            }
            mInitialMotionY = mInitialDownY;
            return;
        }
        mIsBeingDragged = false;
    }

    private void checkScrollStart(int i) {
        if (i == 2) {
            checkVerticalScrollStart(i);
        } else {
            checkHorizontalScrollStart(i);
        }
    }

    private void checkHorizontalScrollStart(int i) {
        if (getScrollX() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollX()), Math.abs(obtainMaxSpringBackDistance(i)), 2);
            if (getScrollX() < 0) {
                mInitialDownX -= obtainTouchDistance;
            } else {
                mInitialDownX += obtainTouchDistance;
            }
            mInitialMotionX = mInitialDownX;
            return;
        }
        mIsBeingDragged = false;
    }

    private boolean onScrollDownEvent(MotionEvent motionEvent, int actionMasked, int i) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (actionMasked == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i);
        } else {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        if (i == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(y - mInitialMotionY);
                            obtainSpringBackDistance = obtainSpringBackDistance(y - mInitialMotionY, i);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(x - mInitialMotionX);
                            obtainSpringBackDistance = obtainSpringBackDistance(x - mInitialMotionX, i);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(f, i);
                        } else {
                            moveTarget(0.0f, i);
                            return false;
                        }
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i == 2) {
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            mInitialDownY = y3;
                            mInitialMotionY = y3;
                        } else {
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            mInitialDownX = x3;
                            mInitialMotionX = x3;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (mIsBeingDragged) {
                mIsBeingDragged = false;
                springBack(i);
            }
            mActivePointerId = -1;
            return false;
        }
        return true;
    }

    private void moveTarget(float f, int i) {
        if (i == 2) {
            scrollTo(0, (int) (-f));
        } else {
            scrollTo((int) (-f), 0);
        }
    }

    private void springBack(int i) {
        springBack(0.0f, i, true);
    }

    private void springBack(float f, int i, boolean z) {
        mSpringScroller.forceStop();
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        mSpringScroller.scrollByFling(scrollX, 0.0f, scrollY, 0.0f, f, i, false);
        if (scrollX == 0 && scrollY == 0 && f == 0.0f) {
            dispatchScrollState(0);
        } else {
            dispatchScrollState(2);
        }
        if (z) {
            AnimationHelper.postInvalidateOnAnimation(this);
        }
    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int actionMasked, int i) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (actionMasked == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i);
        } else {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        if (i == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(mInitialMotionY - y);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionY - y, i);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(mInitialMotionX - x);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionX - x, i);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(-f, i);
                        } else {
                            moveTarget(0.0f, i);
                            return false;
                        }
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i == 2) {
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            mInitialDownY = y3;
                            mInitialMotionY = y3;
                        } else {
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            mInitialDownX = x3;
                            mInitialMotionX = x3;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (mIsBeingDragged) {
                mIsBeingDragged = false;
                springBack(i);
            }
            mActivePointerId = -1;
            return false;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == mActivePointerId) {
            mActivePointerId = motionEvent.getPointerId(actionIndex == 0 ? 1 : 0);
        }
    }

    protected int getSpringBackRange(int i) {
        return i == 2 ? mScreenHeight : mScreenWidth;
    }

    protected float obtainSpringBackDistance(float f, int i) {
        int springBackRange = getSpringBackRange(i);
        return obtainDampingDistance(Math.min(Math.abs(f) / springBackRange, 1.0f), springBackRange);
    }

    protected float obtainMaxSpringBackDistance(int i) {
        return obtainDampingDistance(1.0f, getSpringBackRange(i));
    }

    protected float obtainDampingDistance(float f, int i) {
        double min = Math.min(f, 1.0f);
        return ((float) (((Math.pow(min, 3.0d) / 3.0d) - Math.pow(min, 2.0d)) + min)) * i;
    }

    protected float obtainTouchDistance(float f, float f2, int i) {
        int springBackRange = getSpringBackRange(i);
        if (Math.abs(f) >= Math.abs(f2)) {
            f = f2;
        }
        return (float) (springBackRange - (Math.pow(springBackRange, 0.6666666666666666d) * Math.pow(springBackRange - (f * 3.0f), 0.3333333333333333d)));
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        boolean z = mNestedScrollAxes == 2;
        int i6 = z ? dyConsumed : dxConsumed;
        int i7 = z ? consumed[1] : consumed[0];
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type, consumed);
        if (mSpringBackEnable) {
            int i8 = (z ? consumed[1] : consumed[0]) - i7;
            int i9 = z ? dyUnconsumed - i8 : dxUnconsumed - i8;
            int i11 = z ? 2 : 1;
            if (i9 < 0 && isTargetScrollToTop(i11) && supportTopSpringBackMode()) {
                if (type != 0) {
                    float obtainMaxSpringBackDistance = obtainMaxSpringBackDistance(i11);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (i6 != 0 && (-i9) <= obtainMaxSpringBackDistance) {
                            mSpringScroller.setFirstStep(i9);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollTopUnconsumed != 0.0f) {
                        return;
                    }
                    float f = obtainMaxSpringBackDistance - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f <= Math.abs(i9)) {
                            mTotalFlingUnconsumed += f;
                            consumed[1] = (int) (consumed[1] + f);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(i9);
                            consumed[1] = consumed[1] + i9;
                        }
                        dispatchScrollState(2);
                        moveTarget(obtainSpringBackDistance(mTotalFlingUnconsumed, i11), i11);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollTopUnconsumed += Math.abs(i9);
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i11), i11);
                    consumed[1] = consumed[1] + i9;
                    return;
                }
                return;
            }
            if (i9 > 0 && isTargetScrollToBottom(i11) && supportBottomSpringBackMode()) {
                if (type != 0) {
                    float obtainMaxSpringBackDistance2 = obtainMaxSpringBackDistance(i11);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (i6 != 0 && i9 <= obtainMaxSpringBackDistance2) {
                            mSpringScroller.setFirstStep(i9);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollBottomUnconsumed != 0.0f) {
                        return;
                    }
                    float f2 = obtainMaxSpringBackDistance2 - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f2 <= Math.abs(i9)) {
                            mTotalFlingUnconsumed += f2;
                            consumed[1] = (int) (consumed[1] + f2);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(i9);
                            consumed[1] = consumed[1] + i9;
                        }
                        dispatchScrollState(2);
                        moveTarget(-obtainSpringBackDistance(mTotalFlingUnconsumed, i11), i11);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollBottomUnconsumed += Math.abs(i9);
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i11), i11);
                    consumed[1] = consumed[1] + i9;
                }
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View view, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(view, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollAxes = axes;
        boolean z = axes == 2;
        if (((z ? 2 : 1) & mOriginScrollOrientation) == 0) {
            return false;
        }
        if (mSpringBackEnable) {
            if (!onStartNestedScroll(child, child, axes)) {
                return false;
            }
            float scrollY = z ? getScrollY() : getScrollX();
            if (type != 0 && scrollY != 0.0f && (mTarget instanceof NestedScrollView)) {
                return false;
            }
        }
        mNestedScrollingChildHelper.startNestedScroll(axes, type);
        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (mSpringBackEnable) {
            boolean z = mNestedScrollAxes == 2;
            int i3 = z ? 2 : 1;
            float scrollY = z ? getScrollY() : getScrollX();
            if (type != 0) {
                if (scrollY == 0.0f) {
                    mTotalFlingUnconsumed = 0.0f;
                } else {
                    mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }
                mNestedFlingInProgress = true;
                consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0f) {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else if (scrollY < 0.0f) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }
                mNestedScrollInProgress = true;
            }
            mVelocityY = 0.0f;
            mVelocityX = 0.0f;
            mScrollByFling = false;
            mSpringScroller.forceStop();
        }
        onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (mSpringBackEnable) {
            if (mNestedScrollAxes == 2) {
                onNestedPreScroll(dy, consumed, type);
            } else {
                onNestedPreScroll(dx, consumed, type);
            }
        }
        int[] iArr2 = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], iArr2, null, type)) {
            consumed[0] = consumed[0] + iArr2[0];
            consumed[1] = consumed[1] + iArr2[1];
        }
    }

    private void onNestedPreScroll(int dy, int[] consumed, int type) {
        boolean z = mNestedScrollAxes == 2;
        int i3 = z ? 2 : 1;
        int abs = Math.abs(z ? getScrollY() : getScrollX());
        float f = 0.0f;
        if (type == 0) {
            if (dy > 0) {
                float f2 = mTotalScrollTopUnconsumed;
                if (f2 > 0.0f) {
                    if ((float) dy > f2) {
                        consumeDelta((int) f2, consumed, i3);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        mTotalScrollTopUnconsumed = f2 - (float) dy;
                        consumeDelta(dy, consumed, i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i3), i3);
                    return;
                }
            }
            if (dy < 0) {
                float f4 = mTotalScrollBottomUnconsumed;
                if ((-f4) < 0.0f) {
                    if ((float) dy < (-f4)) {
                        consumeDelta((int) f4, consumed, i3);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        mTotalScrollBottomUnconsumed = f4 + (float) dy;
                        consumeDelta(dy, consumed, i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i3), i3);
                    return;
                }
                return;
            }
            return;
        }
        float f6 = i3 == 2 ? mVelocityY : mVelocityX;
        if (dy > 0) {
            float f7 = mTotalScrollTopUnconsumed;
            if (f7 > 0.0f) {
                if (f6 > 2000.0f) {
                    float obtainSpringBackDistance = obtainSpringBackDistance(f7, i3);
                    if ((float) dy > obtainSpringBackDistance) {
                        consumeDelta((int) obtainSpringBackDistance, consumed, i3);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        consumeDelta(dy, consumed, i3);
                        f = obtainSpringBackDistance - (float) dy;
                        mTotalScrollTopUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    }
                    moveTarget(f, i3);
                    dispatchScrollState(1);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(f6, i3, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollTopUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                }
                consumeDelta(dy, consumed, i3);
                return;
            }
        }
        if (dy < 0) {
            float f9 = mTotalScrollBottomUnconsumed;
            if ((-f9) < 0.0f) {
                if (f6 < -2000.0f) {
                    float obtainSpringBackDistance2 = obtainSpringBackDistance(f9, i3);
                    if ((float) dy < (-obtainSpringBackDistance2)) {
                        consumeDelta((int) obtainSpringBackDistance2, consumed, i3);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        consumeDelta(dy, consumed, i3);
                        f = obtainSpringBackDistance2 + (float) dy;
                        mTotalScrollBottomUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(-f, i3);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(f6, i3, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                } else {
                    mTotalScrollBottomUnconsumed = 0.0f;
                }
                consumeDelta(dy, consumed, i3);
                return;
            }
        }
        if (dy != 0) {
            if ((mTotalScrollBottomUnconsumed == 0.0f || mTotalScrollTopUnconsumed == 0.0f) && mScrollByFling && getScrollY() == 0) {
                consumeDelta(dy, consumed, i3);
            }
        }
    }

    private void consumeDelta(int i, int[] iArr, int i2) {
        if (i2 == 2) {
            iArr[1] = i;
        } else {
            iArr[0] = i;
        }
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
        if (mSpringBackEnable) {
            boolean z = mNestedScrollAxes == 2;
            int i2 = z ? 2 : 1;
            if (mNestedScrollInProgress) {
                mNestedScrollInProgress = false;
                float scrollY = z ? getScrollY() : getScrollX();
                if (!mNestedFlingInProgress && scrollY != 0.0f) {
                    springBack(i2);
                } else {
                    if (scrollY != 0.0f) {
                        stopNestedFlingScroll(i2);
                        return;
                    }
                }
                return;
            }
            if (mNestedFlingInProgress) {
                stopNestedFlingScroll(i2);
            }
        }
    }

    private void stopNestedFlingScroll(int i) {
        mNestedFlingInProgress = false;
        if (mScrollByFling) {
            if (mSpringScroller.isFinished()) {
                springBack(i == 2 ? mVelocityY : mVelocityX, i, false);
            }
            AnimationHelper.postInvalidateOnAnimation(this);
            return;
        }
        springBack(i);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mNestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Point screenSize = MiuiXUtils.getScreenSize(getContext());
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void dispatchScrollState(int i) {
        int i2 = mScrollState;
        if (i2 != i) {
            mScrollState = i;
            for (ViewCompatOnScrollChangeListener mOnScrollChangeListener : mOnScrollChangeListeners) {
                mOnScrollChangeListener.onStateChanged(i2, i, mSpringScroller.isFinished());
            }
        }
    }

    @Override
    public void addOnScrollChangeListener(ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener) {
        mOnScrollChangeListeners.add(viewCompatOnScrollChangeListener);
    }

    @Override
    public boolean onNestedCurrentFling(float f, float f2) {
        mVelocityX = f;
        mVelocityY = f2;
        return true;
    }

    private static class AnimationHelper {
        public static void postInvalidateOnAnimation(View view) {
            AnimationHandler.getInstance().postVsyncCallback();
            view.postInvalidateOnAnimation();
        }

        public static void postOnAnimation(View view, Runnable runnable) {
            AnimationHandler.getInstance().postVsyncCallback();
            view.postOnAnimation(runnable);
        }

        private static class AnimationHandler {
            public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
            private AnimationFrameCallbackProvider mProvider;
            private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new ArrayMap<>();
            private final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
            private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
            private final long mCurrentFrameTime = 0;
            private boolean mListDirty = false;

            public interface AnimationFrameCallback {
                boolean doAnimationFrame(long frameTimeNanos);
            }

            class AnimationCallbackDispatcher {
                AnimationCallbackDispatcher() {
                }

                void dispatchAnimationFrame(long frameTimeNanos) {
                    AnimationHandler.this.doAnimationFrame(frameTimeNanos);
                    if (!AnimationHandler.this.mAnimationCallbacks.isEmpty()) {
                        AnimationHandler.this.getProvider().postFrameCallback();
                    }
                }
            }

            public static AnimationHandler getInstance() {
                ThreadLocal<AnimationHandler> threadLocal = sAnimatorHandler;
                if (threadLocal.get() == null) {
                    threadLocal.set(new AnimationHandler());
                }
                return threadLocal.get();
            }

            public AnimationFrameCallbackProvider getProvider() {
                if (mProvider == null) {
                    mProvider = new FrameCallbackProvider33(mCallbackDispatcher);
                }
                return mProvider;
            }

            public void postVsyncCallback() {
                getProvider().postVsyncCallback();
            }

            public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback, long millis) {
                if (mAnimationCallbacks.isEmpty()) {
                    getProvider().postFrameCallback();
                }
                if (!mAnimationCallbacks.contains(animationFrameCallback)) {
                    mAnimationCallbacks.add(animationFrameCallback);
                }
                if (millis > 0) {
                    mDelayedCallbackStartTime.put(animationFrameCallback, SystemClock.uptimeMillis() + millis);
                }
            }

            public void removeCallback(AnimationFrameCallback animationFrameCallback) {
                mDelayedCallbackStartTime.remove(animationFrameCallback);
                int indexOf = mAnimationCallbacks.indexOf(animationFrameCallback);
                if (indexOf >= 0) {
                    mAnimationCallbacks.set(indexOf, null);
                    mListDirty = true;
                }
            }

            boolean isCurrentThread() {
                return getProvider().isCurrentThread();
            }

            private void doAnimationFrame(long frameTimeNanos) {
                long uptimeMillis = SystemClock.uptimeMillis();
                for (int i = 0; i < mAnimationCallbacks.size(); i++) {
                    AnimationFrameCallback animationFrameCallback = mAnimationCallbacks.get(i);
                    if (animationFrameCallback != null && isCallbackDue(animationFrameCallback, uptimeMillis)) {
                        animationFrameCallback.doAnimationFrame(frameTimeNanos);
                    }
                }
                cleanUpList();
            }

            private boolean isCallbackDue(AnimationFrameCallback animationFrameCallback, long time) {
                Long l = mDelayedCallbackStartTime.get(animationFrameCallback);
                if (l == null)
                    return true;
                if (l >= time)
                    return false;

                mDelayedCallbackStartTime.remove(animationFrameCallback);
                return true;
            }

            private void cleanUpList() {
                if (mListDirty) {
                    for (int size = mAnimationCallbacks.size() - 1; size >= 0; size--) {
                        if (mAnimationCallbacks.get(size) == null) {
                            mAnimationCallbacks.remove(size);
                        }
                    }
                    mListDirty = false;
                }
            }

            public long getFrameDeltaNanos() {
                return getProvider().getFrameDeltaNanos();
            }

            private static class FrameCallbackProvider33 extends AnimationFrameCallbackProvider {
                private final Choreographer mChoreographer;
                private final Choreographer.FrameCallback mChoreographerCallback;
                private long mFrameDeltaNanos;
                private final Looper mLooper;
                private final Choreographer.VsyncCallback mVsyncCallback;

                @SuppressLint("NewApi")
                FrameCallbackProvider33(AnimationCallbackDispatcher animationCallbackDispatcher) {
                    super(animationCallbackDispatcher);
                    mChoreographer = Choreographer.getInstance();
                    mLooper = Looper.myLooper();
                    mFrameDeltaNanos = 0L;
                    mVsyncCallback = frameData -> {
                        Choreographer.FrameTimeline[] frameTimelines = frameData.getFrameTimelines();
                        int length = frameTimelines.length;
                        if (length > 1) {
                            int i = length - 1;
                            FrameCallbackProvider33.this.mFrameDeltaNanos = Math.round(((frameTimelines[i].getExpectedPresentationTimeNanos() - frameTimelines[0].getExpectedPresentationTimeNanos()) * 1.0d) / i);
                        }
                    };
                    mChoreographerCallback = FrameCallbackProvider33.this.mDispatcher::dispatchAnimationFrame;
                }

                @SuppressLint("NewApi")
                @Override
                void postFrameCallback() {
                    mChoreographer.postVsyncCallback(mVsyncCallback);
                    mChoreographer.postFrameCallback(mChoreographerCallback);
                }

                @SuppressLint("NewApi")
                @Override
                public void postVsyncCallback() {
                    mChoreographer.postVsyncCallback(mVsyncCallback);
                }

                @Override
                boolean isCurrentThread() {
                    return Thread.currentThread() == mLooper.getThread();
                }

                @Override
                long getFrameDeltaNanos() {
                    return mFrameDeltaNanos;
                }
            }

            private static abstract class AnimationFrameCallbackProvider {
                final AnimationCallbackDispatcher mDispatcher;

                abstract long getFrameDeltaNanos();

                abstract boolean isCurrentThread();

                abstract void postFrameCallback();

                abstract void postVsyncCallback();

                AnimationFrameCallbackProvider(AnimationCallbackDispatcher animationCallbackDispatcher) {
                    mDispatcher = animationCallbackDispatcher;
                }
            }
        }
    }
}
