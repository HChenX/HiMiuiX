/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.himiuix.springback;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

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

public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3, NestedCurrentFling, ScrollStateDispatcher {
    public static final int ANGLE = 4;
    public static final int HORIZONTAL = 1;
    private static final int INVALID_ID = -1;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    public static final int SPRING_BACK_BOTTOM = 2;
    public static final int SPRING_BACK_TOP = 1;
    private static final String TAG = "MiuiPreference";
    public static final int UNCHECK_ORIENTATION = 0;
    private static final int VELOCITY_THRESHOLD = 2000;
    public static final int VERTICAL = 2;
    private int consumeNestFlingCounter;
    private int mActivePointerId;
    private int mFakeScrollX;
    private int mFakeScrollY;
    private SpringBackLayoutHelper mHelper;
    private int mInitPaddingTop;
    private float mInitialDownX;
    private float mInitialDownY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private boolean mNestedFlingInProgress;
    private int mNestedScrollAxes;
    private boolean mNestedScrollInProgress;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final int[] mNestedScrollingV2ConsumedCompat;
    private List<ViewCompatOnScrollChangeListener> mOnScrollChangeListeners;
    private OnSpringListener mOnSpringListener;
    private int mOriginScrollOrientation;
    private final int[] mParentOffsetInWindow;
    private final int[] mParentScrollConsumed;
    protected int mScreenHeight;
    protected int mScreenWidth;
    private boolean mScrollByFling;
    private int mScrollOrientation;
    private int mScrollState;
    private boolean mSpringBackEnable;
    private int mSpringBackMode;
    private SpringScroller mSpringScroller;
    private View mTarget;
    private int mTargetId;
    private float mTotalFlingUnconsumed;
    private float mTotalScrollBottomUnconsumed;
    private float mTotalScrollTopUnconsumed;
    private int mTouchSlop;
    private float mVelocityX;
    private float mVelocityY;

    public interface OnSpringListener {
        boolean onSpringBack();
    }

    public SpringBackLayout(Context context) {
        this(context, null);
    }

    public SpringBackLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mActivePointerId = -1;
        this.consumeNestFlingCounter = 0;
        this.mParentScrollConsumed = new int[2];
        this.mParentOffsetInWindow = new int[2];
        this.mNestedScrollingV2ConsumedCompat = new int[2];
        this.mOnScrollChangeListeners = new ArrayList<>();
        this.mScrollState = 0;
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        this.mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SpringBackLayout);
        this.mTargetId = obtainStyledAttributes.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        this.mOriginScrollOrientation = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
        this.mSpringBackMode = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        obtainStyledAttributes.recycle();

        this.mSpringScroller = new SpringScroller();
        this.mHelper = new SpringBackLayoutHelper(this, this.mOriginScrollOrientation);
        setNestedScrollingEnabled(true);
        Point screenSize = MiuiXUtils.getScreenSize(context);
        this.mScreenWidth = screenSize.x;
        this.mScreenHeight = screenSize.y;
        this.mSpringBackEnable = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInitPaddingTop = getPaddingTop();
    }

    public void setSpringBackEnable(boolean z) {
        this.mSpringBackEnable = z;
    }

    public void setSpringBackEnableOnTriggerAttached(boolean z) {
        this.mSpringBackEnable = z;
    }

    public boolean springBackEnable() {
        return this.mSpringBackEnable;
    }

    public void setScrollOrientation(int i) {
        this.mOriginScrollOrientation = i;
        this.mHelper.mTargetScrollOrientation = i;
    }

    public void setSpringBackMode(int i) {
        this.mSpringBackMode = i;
    }

    public int getSpringBackMode() {
        return this.mSpringBackMode;
    }

    private int getFakeScrollX() {
        return this.mFakeScrollX;
    }

    private int getFakeScrollY() {
        return this.mFakeScrollY;
    }

    public int getSpringScrollX() {
        if (this.mSpringBackEnable) {
            return getScrollX();
        }
        return getFakeScrollX();
    }

    public int getSpringScrollY() {
        if (this.mSpringBackEnable) {
            return getScrollY();
        }
        return getFakeScrollY();
    }

    @Override
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        View view = this.mTarget;
        if (!(view instanceof NestedScrollingChild3) || z == this.mTarget.isNestedScrollingEnabled()) {
            return;
        }
        this.mTarget.setNestedScrollingEnabled(z);
    }

    private boolean supportTopSpringBackMode() {
        return (this.mSpringBackMode & 1) != 0;
    }

    private boolean supportBottomSpringBackMode() {
        return (this.mSpringBackMode & 2) != 0;
    }

    public void setTarget(View view) {
        this.mTarget = view;
        View view2 = this.mTarget;
        if ((view2 instanceof NestedScrollingChild3) && !view2.isNestedScrollingEnabled()) {
            this.mTarget.setNestedScrollingEnabled(true);
        }
        if (this.mTarget.getOverScrollMode() == 2 || !this.mSpringBackEnable) {
            return;
        }
        this.mTarget.setOverScrollMode(2);
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            int i = this.mTargetId;
            if (i == -1) {
                throw new IllegalArgumentException("invalid target Id");
            }
            this.mTarget = findViewById(i);
        }
        if (this.mTarget == null) {
            throw new IllegalArgumentException("fail to get target");
        }
        if (isEnabled()) {
            View view = this.mTarget;
            if ((view instanceof NestedScrollingChild3) && !view.isNestedScrollingEnabled()) {
                this.mTarget.setNestedScrollingEnabled(true);
            }
        }
        if (this.mTarget.getOverScrollMode() == 2 || !this.mSpringBackEnable) {
            return;
        }
        this.mTarget.setOverScrollMode(2);
    }

    public View getTarget() {
        return this.mTarget;
    }

    @Override
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mTarget.getVisibility() != View.GONE) {
            int measuredWidth = this.mTarget.getMeasuredWidth();
            int measuredHeight = this.mTarget.getMeasuredHeight();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            this.mTarget.layout(paddingLeft, paddingTop, measuredWidth + paddingLeft, measuredHeight + paddingTop);
        }
    }

    @Override
    public void onMeasure(int i, int i2) {
        int min;
        int min2;
        ensureTarget();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        measureChild(this.mTarget, i, i2);
        if (mode == 0) {
            min = this.mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        } else if (mode == 1073741824) {
            min = View.MeasureSpec.getSize(i);
        } else {
            min = Math.min(View.MeasureSpec.getSize(i), this.mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
        }
        if (mode2 == 0) {
            min2 = this.mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        } else if (mode2 == 1073741824) {
            min2 = View.MeasureSpec.getSize(i2);
        } else {
            min2 = Math.min(View.MeasureSpec.getSize(i2), this.mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(min, min2);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (this.mSpringScroller.computeScrollOffset()) {
            scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
            if (!this.mSpringScroller.isFinished()) {
                AnimationHelper.postInvalidateOnAnimation(this);
                return;
            }
            if (getSpringScrollX() != 0 || getSpringScrollY() != 0) {
                if (this.mScrollState != 2) {
                    Log.d("SpringBackLayout", "Scroll stop but state is not correct.");
                    springBack(this.mNestedScrollAxes == 2 ? 2 : 1);
                    return;
                }
            }
            dispatchScrollState(0);
        }
    }

    @Override
    public void scrollTo(int i, int i2) {
        if (this.mSpringBackEnable) {
            super.scrollTo(i, i2);
            return;
        }
        int i3 = this.mFakeScrollX;
        if (i3 == i && this.mFakeScrollY == i2) {
            return;
        }
        int i4 = this.mFakeScrollY;
        this.mFakeScrollX = i;
        this.mFakeScrollY = i2;
        onScrollChanged(i, i2, i3, i4);
        if (!awakenScrollBars()) {
            postInvalidateOnAnimation();
        }
        requestLayout();
    }

    @Override
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        for (ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener : this.mOnScrollChangeListeners) {
            viewCompatOnScrollChangeListener.onScrollChange(this, i, i2, i3, i4);
        }
    }

    private boolean isVerticalTargetScrollToTop() {
        View view = this.mTarget;
        if (view instanceof ListView) {
            return !ListViewCompat.canScrollList((ListView) view, -1);
        }
        return !view.canScrollVertically(-1);
    }

    private boolean isHorizontalTargetScrollToTop() {
        return !this.mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollOrientation(int i) {
        return this.mScrollOrientation == i;
    }

    private boolean isTargetScrollToTop(int i) {
        if (i == 2) {
            View view = this.mTarget;
            if (view instanceof ListView) {
                return !ListViewCompat.canScrollList((ListView) view, -1);
            }
            return !view.canScrollVertically(-1);
        }
        return !this.mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollToBottom(int i) {
        if (i == 2) {
            View view = this.mTarget;
            if (view instanceof ListView) {
                return !ListViewCompat.canScrollList((ListView) view, 1);
            }
            return !view.canScrollVertically(1);
        }
        return !this.mTarget.canScrollHorizontally(1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && this.mScrollState == 2 && this.mHelper.isTouchInTarget(motionEvent)) {
            dispatchScrollState(1);
        }
        boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
        if (motionEvent.getActionMasked() == 1 && this.mScrollState != 2) {
            dispatchScrollState(0);
        }
        return dispatchTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.mSpringBackEnable || !isEnabled() || this.mNestedFlingInProgress || this.mNestedScrollInProgress || this.mTarget.isNestedScrollingEnabled()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (!this.mSpringScroller.isFinished() && actionMasked == 0) {
            this.mSpringScroller.forceStop();
        }
        if (supportTopSpringBackMode() || supportBottomSpringBackMode()) {
            int i = this.mOriginScrollOrientation;
            if ((i & 4) != 0) {
                checkOrientation(motionEvent);
                if (isTargetScrollOrientation(2) && (this.mOriginScrollOrientation & 1) != 0 && getScrollX() == 0.0f) {
                    return false;
                }
                if (isTargetScrollOrientation(1) && (this.mOriginScrollOrientation & 2) != 0 && getScrollY() == 0.0f) {
                    return false;
                }
                if (isTargetScrollOrientation(2) || isTargetScrollOrientation(1)) {
                    disallowParentInterceptTouchEvent(true);
                }
            } else {
                this.mScrollOrientation = i;
            }
            if (isTargetScrollOrientation(2)) {
                return onVerticalInterceptTouchEvent(motionEvent);
            }
            if (isTargetScrollOrientation(1)) {
                return onHorizontalInterceptTouchEvent(motionEvent);
            }
            return false;
        }
        return false;
    }

    private void disallowParentInterceptTouchEvent(boolean z) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(z);
        }
    }

    private void checkOrientation(MotionEvent motionEvent) {
        this.mHelper.checkOrientation(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialDownY = this.mHelper.mInitialDownY;
            this.mInitialDownX = this.mHelper.mInitialDownX;
            this.mActivePointerId = this.mHelper.mActivePointerId;
            if (getScrollY() != 0) {
                this.mScrollOrientation = 2;
                requestDisallowParentInterceptTouchEvent(true);
            } else if (getScrollX() != 0) {
                this.mScrollOrientation = 1;
                requestDisallowParentInterceptTouchEvent(true);
            } else {
                this.mScrollOrientation = 0;
            }
            if ((this.mOriginScrollOrientation & 2) != 0) {
                checkScrollStart(2);
                return;
            } else {
                checkScrollStart(1);
                return;
            }
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (this.mScrollOrientation != 0 || this.mHelper.mScrollOrientation == 0) {
                    return;
                }
                this.mScrollOrientation = this.mHelper.mScrollOrientation;
                return;
            } else if (actionMasked != 3) {
                if (actionMasked != 6) {
                    return;
                }
                onSecondaryPointerUp(motionEvent);
                return;
            }
        }
        disallowParentInterceptTouchEvent(false);
        if ((this.mOriginScrollOrientation & 2) != 0) {
            springBack(2);
        } else {
            springBack(1);
        }
    }

    private boolean onVerticalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (isTargetScrollToTop(2) || isTargetScrollToBottom(2)) {
            if (!isTargetScrollToTop(2) || supportTopSpringBackMode()) {
                if (!isTargetScrollToBottom(2) || supportBottomSpringBackMode()) {
                    int actionMasked = motionEvent.getActionMasked();
                    if (actionMasked == 0) {
                        int pointerId = motionEvent.getPointerId(0);
                        this.mActivePointerId = pointerId;
                        int findPointerIndex = motionEvent.findPointerIndex(pointerId);
                        if (findPointerIndex < 0) {
                            return false;
                        }
                        this.mInitialDownY = motionEvent.getY(findPointerIndex);
                        if (getScrollY() != 0) {
                            this.mIsBeingDragged = true;
                            this.mInitialMotionY = this.mInitialDownY;
                        } else {
                            this.mIsBeingDragged = false;
                        }
                    } else {
                        if (actionMasked != 1) {
                            if (actionMasked == 2) {
                                int i = this.mActivePointerId;
                                if (i == -1) {
                                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                                    return false;
                                }
                                int findPointerIndex2 = motionEvent.findPointerIndex(i);
                                if (findPointerIndex2 < 0) {
                                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                                    return false;
                                }
                                float y = motionEvent.getY(findPointerIndex2);
                                if (isTargetScrollToBottom(2) && isTargetScrollToTop(2)) {
                                    z = true;
                                }
                                if ((!z && isTargetScrollToTop(2)) || (z && y > this.mInitialDownY)) {
                                    if (y - this.mInitialDownY > this.mTouchSlop && !this.mIsBeingDragged) {
                                        this.mIsBeingDragged = true;
                                        dispatchScrollState(1);
                                        this.mInitialMotionY = y;
                                    }
                                } else if (this.mInitialDownY - y > this.mTouchSlop && !this.mIsBeingDragged) {
                                    this.mIsBeingDragged = true;
                                    dispatchScrollState(1);
                                    this.mInitialMotionY = y;
                                }
                            } else if (actionMasked != 3) {
                                if (actionMasked == 6) {
                                    onSecondaryPointerUp(motionEvent);
                                }
                            }
                        }
                        this.mIsBeingDragged = false;
                        this.mActivePointerId = -1;
                    }
                    return this.mIsBeingDragged;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private boolean onHorizontalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (isTargetScrollToTop(1) || isTargetScrollToBottom(1)) {
            if (!isTargetScrollToTop(1) || supportTopSpringBackMode()) {
                if (!isTargetScrollToBottom(1) || supportBottomSpringBackMode()) {
                    int actionMasked = motionEvent.getActionMasked();
                    if (actionMasked == 0) {
                        int pointerId = motionEvent.getPointerId(0);
                        this.mActivePointerId = pointerId;
                        int findPointerIndex = motionEvent.findPointerIndex(pointerId);
                        if (findPointerIndex < 0) {
                            return false;
                        }
                        this.mInitialDownX = motionEvent.getX(findPointerIndex);
                        if (getScrollX() != 0) {
                            this.mIsBeingDragged = true;
                            this.mInitialMotionX = this.mInitialDownX;
                        } else {
                            this.mIsBeingDragged = false;
                        }
                    } else {
                        if (actionMasked != 1) {
                            if (actionMasked == 2) {
                                int i = this.mActivePointerId;
                                if (i == -1) {
                                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                                    return false;
                                }
                                int findPointerIndex2 = motionEvent.findPointerIndex(i);
                                if (findPointerIndex2 < 0) {
                                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                                    return false;
                                }
                                float x = motionEvent.getX(findPointerIndex2);
                                if (isTargetScrollToBottom(1) && isTargetScrollToTop(1)) {
                                    z = true;
                                }
                                if ((!z && isTargetScrollToTop(1)) || (z && x > this.mInitialDownX)) {
                                    if (x - this.mInitialDownX > this.mTouchSlop && !this.mIsBeingDragged) {
                                        this.mIsBeingDragged = true;
                                        dispatchScrollState(1);
                                        this.mInitialMotionX = x;
                                    }
                                } else if (this.mInitialDownX - x > this.mTouchSlop && !this.mIsBeingDragged) {
                                    this.mIsBeingDragged = true;
                                    dispatchScrollState(1);
                                    this.mInitialMotionX = x;
                                }
                            } else if (actionMasked != 3) {
                                if (actionMasked == 6) {
                                    onSecondaryPointerUp(motionEvent);
                                }
                            }
                        }
                        this.mIsBeingDragged = false;
                        this.mActivePointerId = -1;
                    }
                    return this.mIsBeingDragged;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (isEnabled() && this.mSpringBackEnable) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(z);
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
        if (!isEnabled() || this.mNestedFlingInProgress || this.mNestedScrollInProgress || this.mTarget.isNestedScrollingEnabled()) {
            return false;
        }
        if (!this.mSpringScroller.isFinished() && actionMasked == 0) {
            this.mSpringScroller.forceStop();
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

    private boolean onScrollEvent(MotionEvent motionEvent, int i, int i2) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i == 0) {
            this.mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i2);
        } else if (i == 1) {
            if (motionEvent.findPointerIndex(this.mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (this.mIsBeingDragged) {
                this.mIsBeingDragged = false;
                springBack(i2);
            }
            this.mActivePointerId = -1;
            return false;
        } else if (i == 2) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
            if (findPointerIndex < 0) {
                Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                return false;
            } else if (this.mIsBeingDragged) {
                if (i2 == 2) {
                    float y = motionEvent.getY(findPointerIndex);
                    signum = Math.signum(y - this.mInitialMotionY);
                    obtainSpringBackDistance = obtainSpringBackDistance(y - this.mInitialMotionY, i2);
                } else {
                    float x = motionEvent.getX(findPointerIndex);
                    signum = Math.signum(x - this.mInitialMotionX);
                    obtainSpringBackDistance = obtainSpringBackDistance(x - this.mInitialMotionX, i2);
                }
                requestDisallowParentInterceptTouchEvent(true);
                moveTarget(signum * obtainSpringBackDistance, i2);
            }
        } else if (i == 3) {
            return false;
        } else {
            if (i == 5) {
                int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex2 < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                    return false;
                }
                if (i2 == 2) {
                    float y2 = motionEvent.getY(findPointerIndex2) - this.mInitialDownY;
                    actionIndex = motionEvent.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }
                    float y3 = motionEvent.getY(actionIndex) - y2;
                    this.mInitialDownY = y3;
                    this.mInitialMotionY = y3;
                } else {
                    float x2 = motionEvent.getX(findPointerIndex2) - this.mInitialDownX;
                    actionIndex = motionEvent.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }
                    float x3 = motionEvent.getX(actionIndex) - x2;
                    this.mInitialDownX = x3;
                    this.mInitialMotionX = x3;
                }
                this.mActivePointerId = motionEvent.getPointerId(actionIndex);
            } else if (i == 6) {
                onSecondaryPointerUp(motionEvent);
            }
        }
        return true;
    }

    private void checkVerticalScrollStart(int i) {
        if (getScrollY() != 0) {
            this.mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollY()), Math.abs(obtainMaxSpringBackDistance(i)), 2);
            if (getScrollY() < 0) {
                this.mInitialDownY -= obtainTouchDistance;
            } else {
                this.mInitialDownY += obtainTouchDistance;
            }
            this.mInitialMotionY = this.mInitialDownY;
            return;
        }
        this.mIsBeingDragged = false;
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
            this.mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollX()), Math.abs(obtainMaxSpringBackDistance(i)), 2);
            if (getScrollX() < 0) {
                this.mInitialDownX -= obtainTouchDistance;
            } else {
                this.mInitialDownX += obtainTouchDistance;
            }
            this.mInitialMotionX = this.mInitialDownX;
            return;
        }
        this.mIsBeingDragged = false;
    }

    private boolean onScrollDownEvent(MotionEvent motionEvent, int i, int i2) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i == 0) {
            this.mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i2);
        } else {
            if (i != 1) {
                if (i == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    } else if (this.mIsBeingDragged) {
                        if (i2 == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(y - this.mInitialMotionY);
                            obtainSpringBackDistance = obtainSpringBackDistance(y - this.mInitialMotionY, i2);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(x - this.mInitialMotionX);
                            obtainSpringBackDistance = obtainSpringBackDistance(x - this.mInitialMotionX, i2);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(f, i2);
                        } else {
                            moveTarget(0.0f, i2);
                            return false;
                        }
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i2 == 2) {
                            float y2 = motionEvent.getY(findPointerIndex2) - this.mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            this.mInitialDownY = y3;
                            this.mInitialMotionY = y3;
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex2) - this.mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            this.mInitialDownX = x3;
                            this.mInitialMotionX = x3;
                        }
                        this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(this.mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (this.mIsBeingDragged) {
                this.mIsBeingDragged = false;
                springBack(i2);
            }
            this.mActivePointerId = -1;
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
        OnSpringListener onSpringListener = this.mOnSpringListener;
        if (onSpringListener == null || !onSpringListener.onSpringBack()) {
            this.mSpringScroller.forceStop();
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            this.mSpringScroller.scrollByFling(scrollX, 0.0f, scrollY, 0.0f, f, i, false);
            if (scrollX == 0 && scrollY == 0 && f == 0.0f) {
                dispatchScrollState(0);
            } else {
                dispatchScrollState(2);
            }
            if (z) {
                AnimationHelper.postInvalidateOnAnimation(this);
            }
        }
    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int i, int i2) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i == 0) {
            this.mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i2);
        } else {
            if (i != 1) {
                if (i == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    } else if (this.mIsBeingDragged) {
                        if (i2 == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(this.mInitialMotionY - y);
                            obtainSpringBackDistance = obtainSpringBackDistance(this.mInitialMotionY - y, i2);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(this.mInitialMotionX - x);
                            obtainSpringBackDistance = obtainSpringBackDistance(this.mInitialMotionX - x, i2);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(-f, i2);
                        } else {
                            moveTarget(0.0f, i2);
                            return false;
                        }
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i2 == 2) {
                            float y2 = motionEvent.getY(findPointerIndex2) - this.mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            this.mInitialDownY = y3;
                            this.mInitialMotionY = y3;
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex2) - this.mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            this.mInitialDownX = x3;
                            this.mInitialMotionX = x3;
                        }
                        this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(this.mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (this.mIsBeingDragged) {
                this.mIsBeingDragged = false;
                springBack(i2);
            }
            this.mActivePointerId = -1;
            return false;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mActivePointerId) {
            this.mActivePointerId = motionEvent.getPointerId(actionIndex == 0 ? 1 : 0);
        }
    }

    protected int getSpringBackRange(int i) {
        return i == 2 ? this.mScreenHeight : this.mScreenWidth;
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
        double d = springBackRange;
        return (float) (d - (Math.pow(d, 0.6666666666666666d) * Math.pow(springBackRange - (f * 3.0f), 0.3333333333333333d)));
    }

    @Override
    public void onNestedScroll(View view, int i, int i2, int i3, int i4, int i5, int[] iArr) {
        boolean z = this.mNestedScrollAxes == 2;
        int i6 = z ? i2 : i;
        int i7 = z ? iArr[1] : iArr[0];
        dispatchNestedScroll(i, i2, i3, i4, this.mParentOffsetInWindow, i5, iArr);
        if (this.mSpringBackEnable) {
            int i8 = (z ? iArr[1] : iArr[0]) - i7;
            int i9 = z ? i4 - i8 : i3 - i8;
            int i10 = i9 != 0 ? i9 : 0;
            int i11 = z ? 2 : 1;
            if (i10 < 0 && isTargetScrollToTop(i11) && supportTopSpringBackMode()) {
                if (i5 != 0) {
                    float obtainMaxSpringBackDistance = obtainMaxSpringBackDistance(i11);
                    if (this.mVelocityY != 0.0f || this.mVelocityX != 0.0f) {
                        this.mScrollByFling = true;
                        if (i6 != 0 && (-i10) <= obtainMaxSpringBackDistance) {
                            this.mSpringScroller.setFirstStep(i10);
                        }
                        dispatchScrollState(2);
                    } else if (this.mTotalScrollTopUnconsumed != 0.0f) {
                    } else {
                        float f = obtainMaxSpringBackDistance - this.mTotalFlingUnconsumed;
                        if (this.consumeNestFlingCounter < 4) {
                            if (f <= Math.abs(i10)) {
                                this.mTotalFlingUnconsumed += f;
                                iArr[1] = (int) (iArr[1] + f);
                            } else {
                                this.mTotalFlingUnconsumed += Math.abs(i10);
                                iArr[1] = iArr[1] + i9;
                            }
                            dispatchScrollState(2);
                            moveTarget(obtainSpringBackDistance(this.mTotalFlingUnconsumed, i11), i11);
                            this.consumeNestFlingCounter++;
                        }
                    }
                } else if (this.mSpringScroller.isFinished()) {
                    this.mTotalScrollTopUnconsumed += Math.abs(i10);
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(this.mTotalScrollTopUnconsumed, i11), i11);
                    iArr[1] = iArr[1] + i9;
                }
            } else if (i10 > 0 && isTargetScrollToBottom(i11) && supportBottomSpringBackMode()) {
                if (i5 != 0) {
                    float obtainMaxSpringBackDistance2 = obtainMaxSpringBackDistance(i11);
                    if (this.mVelocityY != 0.0f || this.mVelocityX != 0.0f) {
                        this.mScrollByFling = true;
                        if (i6 != 0 && i10 <= obtainMaxSpringBackDistance2) {
                            this.mSpringScroller.setFirstStep(i10);
                        }
                        dispatchScrollState(2);
                    } else if (this.mTotalScrollBottomUnconsumed != 0.0f) {
                    } else {
                        float f2 = obtainMaxSpringBackDistance2 - this.mTotalFlingUnconsumed;
                        if (this.consumeNestFlingCounter < 4) {
                            if (f2 <= Math.abs(i10)) {
                                this.mTotalFlingUnconsumed += f2;
                                iArr[1] = (int) (iArr[1] + f2);
                            } else {
                                this.mTotalFlingUnconsumed += Math.abs(i10);
                                iArr[1] = iArr[1] + i9;
                            }
                            dispatchScrollState(2);
                            moveTarget(-obtainSpringBackDistance(this.mTotalFlingUnconsumed, i11), i11);
                            this.consumeNestFlingCounter++;
                        }
                    }
                } else if (this.mSpringScroller.isFinished()) {
                    this.mTotalScrollBottomUnconsumed += Math.abs(i10);
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(this.mTotalScrollBottomUnconsumed, i11), i11);
                    iArr[1] = iArr[1] + i9;
                }
            }
        }
    }

    @Override
    public void onNestedScroll(View view, int i, int i2, int i3, int i4, int i5) {
        onNestedScroll(view, i, i2, i3, i4, i5, this.mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(View view, int i, int i2, int i3, int i4) {
        onNestedScroll(view, i, i2, i3, i4, ViewCompat.TYPE_TOUCH, this.mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public boolean onStartNestedScroll(View view, View view2, int i, int i2) {
        this.mNestedScrollAxes = i;
        boolean z = i == 2;
        if (((z ? 2 : 1) & this.mOriginScrollOrientation) == 0) {
            return false;
        }
        if (this.mSpringBackEnable) {
            if (!onStartNestedScroll(view, view, i)) {
                return false;
            }
            float scrollY = z ? getScrollY() : getScrollX();
            if (i2 != 0 && scrollY != 0.0f && (this.mTarget instanceof NestedScrollView)) {
                return false;
            }
        }
        this.mNestedScrollingChildHelper.startNestedScroll(i, i2);
        return true;
    }

    @Override
    public boolean onStartNestedScroll(View view, View view2, int i) {
        return isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(View view, View view2, int i, int i2) {
        if (this.mSpringBackEnable) {
            boolean z = this.mNestedScrollAxes == 2;
            int i3 = z ? 2 : 1;
            float scrollY = z ? getScrollY() : getScrollX();
            if (i2 != 0) {
                if (scrollY == 0.0f) {
                    this.mTotalFlingUnconsumed = 0.0f;
                } else {
                    this.mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }
                this.mNestedFlingInProgress = true;
                this.consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0f) {
                    this.mTotalScrollTopUnconsumed = 0.0f;
                    this.mTotalScrollBottomUnconsumed = 0.0f;
                } else if (scrollY < 0.0f) {
                    this.mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    this.mTotalScrollBottomUnconsumed = 0.0f;
                } else {
                    this.mTotalScrollTopUnconsumed = 0.0f;
                    this.mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }
                this.mNestedScrollInProgress = true;
            }
            this.mVelocityY = 0.0f;
            this.mVelocityX = 0.0f;
            this.mScrollByFling = false;
            this.mSpringScroller.forceStop();
        }
        onNestedScrollAccepted(view, view2, i);
    }

    @Override
    public void onNestedScrollAccepted(View view, View view2, int i) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(view, view2, i);
        startNestedScroll(i & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View view, int i, int i2, int[] iArr, int i3) {
        if (this.mSpringBackEnable) {
            if (this.mNestedScrollAxes == 2) {
                onNestedPreScroll(i2, iArr, i3);
            } else {
                onNestedPreScroll(i, iArr, i3);
            }
        }
        int[] iArr2 = this.mParentScrollConsumed;
        if (dispatchNestedPreScroll(i - iArr[0], i2 - iArr[1], iArr2, null, i3)) {
            iArr[0] = iArr[0] + iArr2[0];
            iArr[1] = iArr[1] + iArr2[1];
        }
    }

    private void onNestedPreScroll(int i, int[] iArr, int i2) {
        boolean z = this.mNestedScrollAxes == 2;
        int i3 = z ? 2 : 1;
        int abs = Math.abs(z ? getScrollY() : getScrollX());
        float f = 0.0f;
        if (i2 == 0) {
            if (i > 0) {
                float f2 = this.mTotalScrollTopUnconsumed;
                if (f2 > 0.0f) {
                    float f3 = i;
                    if (f3 > f2) {
                        consumeDelta((int) f2, iArr, i3);
                        this.mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        this.mTotalScrollTopUnconsumed = f2 - f3;
                        consumeDelta(i, iArr, i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(this.mTotalScrollTopUnconsumed, i3), i3);
                    return;
                }
            }
            if (i < 0) {
                float f4 = this.mTotalScrollBottomUnconsumed;
                if ((-f4) < 0.0f) {
                    float f5 = i;
                    if (f5 < (-f4)) {
                        consumeDelta((int) f4, iArr, i3);
                        this.mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        this.mTotalScrollBottomUnconsumed = f4 + f5;
                        consumeDelta(i, iArr, i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(this.mTotalScrollBottomUnconsumed, i3), i3);
                    return;
                }
                return;
            }
            return;
        }
        float f6 = i3 == 2 ? this.mVelocityY : this.mVelocityX;
        if (i > 0) {
            float f7 = this.mTotalScrollTopUnconsumed;
            if (f7 > 0.0f) {
                if (f6 > 2000.0f) {
                    float obtainSpringBackDistance = obtainSpringBackDistance(f7, i3);
                    float f8 = i;
                    if (f8 > obtainSpringBackDistance) {
                        consumeDelta((int) obtainSpringBackDistance, iArr, i3);
                        this.mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        consumeDelta(i, iArr, i3);
                        f = obtainSpringBackDistance - f8;
                        this.mTotalScrollTopUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    }
                    moveTarget(f, i3);
                    dispatchScrollState(1);
                    return;
                }
                if (!this.mScrollByFling) {
                    this.mScrollByFling = true;
                    springBack(f6, i3, false);
                }
                if (this.mSpringScroller.computeScrollOffset()) {
                    scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
                    this.mTotalScrollTopUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                } else {
                    this.mTotalScrollTopUnconsumed = 0.0f;
                }
                consumeDelta(i, iArr, i3);
                return;
            }
        }
        if (i < 0) {
            float f9 = this.mTotalScrollBottomUnconsumed;
            if ((-f9) < 0.0f) {
                if (f6 < -2000.0f) {
                    float obtainSpringBackDistance2 = obtainSpringBackDistance(f9, i3);
                    float f10 = i;
                    if (f10 < (-obtainSpringBackDistance2)) {
                        consumeDelta((int) obtainSpringBackDistance2, iArr, i3);
                        this.mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        consumeDelta(i, iArr, i3);
                        f = obtainSpringBackDistance2 + f10;
                        this.mTotalScrollBottomUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    }
                    dispatchScrollState(1);
                    moveTarget(-f, i3);
                    return;
                }
                if (!this.mScrollByFling) {
                    this.mScrollByFling = true;
                    springBack(f6, i3, false);
                }
                if (this.mSpringScroller.computeScrollOffset()) {
                    scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
                    this.mTotalScrollBottomUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                } else {
                    this.mTotalScrollBottomUnconsumed = 0.0f;
                }
                consumeDelta(i, iArr, i3);
                return;
            }
        }
        if (i != 0) {
            if ((this.mTotalScrollBottomUnconsumed == 0.0f || this.mTotalScrollTopUnconsumed == 0.0f) && this.mScrollByFling && getScrollY() == 0) {
                consumeDelta(i, iArr, i3);
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
    public void setNestedScrollingEnabled(boolean z) {
        this.mNestedScrollingChildHelper.setNestedScrollingEnabled(z);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return this.mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void onStopNestedScroll(View view, int i) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(view, i);
        stopNestedScroll(i);
        if (this.mSpringBackEnable) {
            boolean z = this.mNestedScrollAxes == 2;
            int i2 = z ? 2 : 1;
            if (this.mNestedScrollInProgress) {
                this.mNestedScrollInProgress = false;
                float scrollY = z ? getScrollY() : getScrollX();
                if (!this.mNestedFlingInProgress && scrollY != 0.0f) {
                    springBack(i2);
                } else if (scrollY != 0.0f) {
                    stopNestedFlingScroll(i2);
                }
            } else if (this.mNestedFlingInProgress) {
                stopNestedFlingScroll(i2);
            }
        }
    }

    private void stopNestedFlingScroll(int i) {
        this.mNestedFlingInProgress = false;
        if (this.mScrollByFling) {
            if (this.mSpringScroller.isFinished()) {
                springBack(i == 2 ? this.mVelocityY : this.mVelocityX, i, false);
            }
            AnimationHelper.postInvalidateOnAnimation(this);
            return;
        }
        springBack(i);
    }

    @Override
    public void stopNestedScroll() {
        this.mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean onNestedFling(View view, float f, float f2, boolean z) {
        return dispatchNestedFling(f, f2, z);
    }

    @Override
    public boolean onNestedPreFling(View view, float f, float f2) {
        return dispatchNestedPreFling(f, f2);
    }

    @Override
    public void dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr, int i5, int[] iArr2) {
        this.mNestedScrollingChildHelper.dispatchNestedScroll(i, i2, i3, i4, iArr, i5, iArr2);
    }

    @Override
    public boolean startNestedScroll(int i, int i2) {
        return this.mNestedScrollingChildHelper.startNestedScroll(i, i2);
    }

    @Override
    public boolean startNestedScroll(int i) {
        return this.mNestedScrollingChildHelper.startNestedScroll(i);
    }

    @Override
    public void stopNestedScroll(int i) {
        this.mNestedScrollingChildHelper.stopNestedScroll(i);
    }

    @Override
    public boolean hasNestedScrollingParent(int i) {
        return this.mNestedScrollingChildHelper.hasNestedScrollingParent(i);
    }

    @Override
    public boolean dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr, int i5) {
        return this.mNestedScrollingChildHelper.dispatchNestedScroll(i, i2, i3, i4, iArr, i5);
    }

    @Override
    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2, int i3) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(i, i2, iArr, iArr2, i3);
    }

    @Override
    public boolean dispatchNestedPreFling(float f, float f2) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreFling(f, f2);
    }

    @Override
    public boolean dispatchNestedFling(float f, float f2, boolean z) {
        return this.mNestedScrollingChildHelper.dispatchNestedFling(f, f2, z);
    }

    @Override
    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(i, i2, iArr, iArr2);
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Point screenSize = MiuiXUtils.getScreenSize(getContext());
        this.mScreenWidth = screenSize.x;
        this.mScreenHeight = screenSize.y;
    }

    public void smoothScrollTo(int i, int i2) {
        if (i - getScrollX() == 0 && i2 - getScrollY() == 0) {
            return;
        }
        this.mSpringScroller.forceStop();
        this.mSpringScroller.scrollByFling(getScrollX(), i, getScrollY(), i2, 0.0f, 2, true);
        dispatchScrollState(2);
        AnimationHelper.postInvalidateOnAnimation(this);
    }

    private void dispatchScrollState(int i) {
        int i2 = this.mScrollState;
        if (i2 != i) {
            this.mScrollState = i;
            for (ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener : this.mOnScrollChangeListeners) {
                viewCompatOnScrollChangeListener.onStateChanged(i2, i, this.mSpringScroller.isFinished());
            }
        }
    }

    @Override
    public void addOnScrollChangeListener(ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener) {
        this.mOnScrollChangeListeners.add(viewCompatOnScrollChangeListener);
    }

    @Override
    public void removeOnScrollChangeListener(ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener) {
        this.mOnScrollChangeListeners.remove(viewCompatOnScrollChangeListener);
    }

    public void setOnSpringListener(OnSpringListener onSpringListener) {
        this.mOnSpringListener = onSpringListener;
    }

    public boolean hasSpringListener() {
        return this.mOnSpringListener != null;
    }

    @Override
    public boolean onNestedCurrentFling(float f, float f2) {
        this.mVelocityX = f;
        this.mVelocityY = f2;
        return true;
    }
}