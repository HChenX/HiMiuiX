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
package com.hchen.himiuix.miuixhelperview.springback;

import static com.hchen.himiuix.MiuiXUtils.getScreenSize;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @noinspection NullableProblems
 */
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedCurrentFling {
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
    private OnSpringListener mOnSpringListener;
    private int mOriginScrollOrientation;
    protected int mScreenHeight;
    protected int mScreenWidth;
    private boolean mScrollByFling;
    private int mScrollOrientation;
    private int mScrollState = 0;
    private boolean mSpringBackEnable;
    private int mSpringBackMode;
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
        try (TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SpringBackLayout)) {
            mTargetId = obtainStyledAttributes.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
            mOriginScrollOrientation = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
            mSpringBackMode = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mHelper = new SpringBackLayoutHelper(this, mOriginScrollOrientation);
        setNestedScrollingEnabled(true);
        Point screenSize = getScreenSize(context);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
        mSpringBackEnable = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInitPaddingTop = getPaddingTop();
    }

    public void setSpringBackEnable(boolean enable) {
        mSpringBackEnable = enable;
    }

    public void setSpringBackEnableOnTriggerAttached(boolean enable) {
        mSpringBackEnable = enable;
    }

    public void setScrollOrientation(int orientation) {
        mOriginScrollOrientation = orientation;
        mHelper.mTargetScrollOrientation = orientation;
    }

    public void setOnScrollChangeListeners(ViewCompatOnScrollChangeListener listener) {
        if (listener == null) return;
        if (mOnScrollChangeListeners.contains(listener))
            return;

        mOnScrollChangeListeners.add(listener);
    }

    public void removeOnScrollChangeListener(ViewCompatOnScrollChangeListener listener) {
        if (listener == null) {
            mOnScrollChangeListeners.clear();
            return;
        }
        mOnScrollChangeListeners.remove(listener);
    }

    public void setSpringBackMode(int mode) {
        mSpringBackMode = mode;
    }

    public int getSpringBackMode() {
        return mSpringBackMode;
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
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (!(mTarget instanceof NestedScrollingChild3) || enable == mTarget.isNestedScrollingEnabled())
            return;
        mTarget.setNestedScrollingEnabled(enable);
    }

    private boolean supportTopSpringBackMode() {
        return (mSpringBackMode & 1) != 0;
    }

    private boolean supportBottomSpringBackMode() {
        return (mSpringBackMode & 2) != 0;
    }

    public void setTarget(View view) {
        mTarget = view;
        if ((mTarget instanceof NestedScrollingChild3) && !mTarget.isNestedScrollingEnabled())
            mTarget.setNestedScrollingEnabled(true);
        if (mTarget.getOverScrollMode() == View.OVER_SCROLL_NEVER || !mSpringBackEnable)
            return;

        mTarget.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (mTargetId == -1)
                throw new IllegalArgumentException("invalid target Id");
            mTarget = findViewById(mTargetId);

            if (mTarget == null)
                throw new IllegalArgumentException("fail to get target");
        }

        if (isEnabled())
            if ((mTarget instanceof NestedScrollingChild3) && !mTarget.isNestedScrollingEnabled())
                mTarget.setNestedScrollingEnabled(true);
        if (mTarget.getOverScrollMode() == View.OVER_SCROLL_NEVER || !mSpringBackEnable)
            return;

        mTarget.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public View getTarget() {
        return mTarget;
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
        ensureTarget();

        int min;
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            min = mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        } else if (mode == MeasureSpec.EXACTLY) {
            min = View.MeasureSpec.getSize(widthMeasureSpec);
        } else {
            min = Math.min(View.MeasureSpec.getSize(widthMeasureSpec), mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
        }

        int min2;
        int mode2 = View.MeasureSpec.getMode(heightMeasureSpec);
        if (mode2 == MeasureSpec.UNSPECIFIED) {
            min2 = mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        } else if (mode2 == MeasureSpec.EXACTLY) {
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
                    springBack(mNestedScrollAxes == 2 ? 2 : 1);
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
        if (mFakeScrollX == x && mFakeScrollY == y)
            return;

        mFakeScrollX = x;
        mFakeScrollY = y;
        onScrollChanged(x, y, mFakeScrollX, mFakeScrollY);
        if (!awakenScrollBars())
            postInvalidateOnAnimation();
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

    private boolean isTargetScrollToTop(int state) {
        if (state == 2) {
            if (mTarget instanceof ListView listView)
                return !listView.canScrollList(-1);
            return !mTarget.canScrollVertically(-1);
        }
        return !mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollToBottom(int state) {
        if (state == 2) {
            if (mTarget instanceof ListView listView)
                return !listView.canScrollList(1);
            return !mTarget.canScrollVertically(1);
        }
        return !mTarget.canScrollHorizontally(1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && mScrollState == 2 && mHelper.isTouchInTarget(motionEvent)) {
            dispatchScrollState(1);
        }
        boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP && mScrollState != 2) {
            dispatchScrollState(0);
        }
        return dispatchTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!mSpringBackEnable || !isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || mTarget.isNestedScrollingEnabled())
            return false;
        if (!mSpringScroller.isFinished() && motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
            mSpringScroller.forceStop();
        if (!supportTopSpringBackMode() && !supportBottomSpringBackMode())
            return false;

        if ((mOriginScrollOrientation & 4) != 0) {
            checkOrientation(motionEvent);
            if (isTargetScrollOrientation(2) && (mOriginScrollOrientation & 1) != 0 && getScrollX() == 0.0f)
                return false;
            if (isTargetScrollOrientation(1) && (mOriginScrollOrientation & 2) != 0 && getScrollY() == 0.0f)
                return false;
            if (isTargetScrollOrientation(2) || isTargetScrollOrientation(1))
                disallowParentInterceptTouchEvent(true);
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
        if (actionMasked == MotionEvent.ACTION_DOWN) {
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

        int ori;
        if (actionMasked != MotionEvent.ACTION_UP) {
            if (actionMasked == MotionEvent.ACTION_MOVE) {
                if (mScrollOrientation != 0 || (ori = mHelper.mScrollOrientation) == 0) {
                    return;
                }
                mScrollOrientation = ori;
                return;
            }
            if (actionMasked != MotionEvent.ACTION_CANCEL) {
                if (actionMasked != MotionEvent.ACTION_POINTER_UP) {
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
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2))
            return false;
        if (isTargetScrollToTop(2) && !supportTopSpringBackMode())
            return false;
        if (isTargetScrollToBottom(2) && !supportBottomSpringBackMode())
            return false;

        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
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
            if (actionMasked != MotionEvent.ACTION_UP) {
                if (actionMasked == MotionEvent.ACTION_MOVE) {
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
                    boolean z = isTargetScrollToBottom(2) && isTargetScrollToTop(2);
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
                } else if (actionMasked != MotionEvent.ACTION_CANCEL) {
                    if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
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
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1))
            return false;
        if (isTargetScrollToTop(1) && !supportTopSpringBackMode())
            return false;
        if (isTargetScrollToBottom(1) && !supportBottomSpringBackMode())
            return false;

        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
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
            if (actionMasked != MotionEvent.ACTION_UP) {
                if (actionMasked == MotionEvent.ACTION_MOVE) {
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
                    boolean z = isTargetScrollToBottom(1) && isTargetScrollToTop(1);
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
                } else if (actionMasked != MotionEvent.ACTION_CANCEL) {
                    if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
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
        if (isEnabled() && mSpringBackEnable)
            return;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void internalRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void requestDisallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        while (parent != null) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
            parent = parent.getParent();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || mTarget.isNestedScrollingEnabled())
            return false;

        if (!mSpringScroller.isFinished() && motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
            mSpringScroller.forceStop();
        if (isTargetScrollOrientation(2))
            return onVerticalTouchEvent(motionEvent);
        if (isTargetScrollOrientation(1))
            return onHorizontalTouchEvent(motionEvent);

        return false;
    }

    private boolean onHorizontalTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1))
            return onScrollEvent(motionEvent, actionMasked, 1);
        if (isTargetScrollToBottom(1))
            return onScrollUpEvent(motionEvent, actionMasked, 1);

        return onScrollDownEvent(motionEvent, actionMasked, 1);
    }

    private boolean onVerticalTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2))
            return onScrollEvent(motionEvent, actionMasked, 2);
        if (isTargetScrollToBottom(2))
            return onScrollUpEvent(motionEvent, actionMasked, 2);

        return onScrollDownEvent(motionEvent, actionMasked, 2);
    }

    private boolean onScrollEvent(MotionEvent motionEvent, int actionMasked, int mode) {
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(mode);
        } else {
            if (actionMasked == MotionEvent.ACTION_UP) {
                if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(mode);
                }
                mActivePointerId = -1;
                return false;
            }
            if (actionMasked == MotionEvent.ACTION_MOVE) {
                int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    float signum;
                    float obtainSpringBackDistance;
                    if (mode == 2) {
                        float y = motionEvent.getY(findPointerIndex);
                        signum = Math.signum(y - mInitialMotionY);
                        obtainSpringBackDistance = obtainSpringBackDistance(y - mInitialMotionY, mode);
                    } else {
                        float x = motionEvent.getX(findPointerIndex);
                        signum = Math.signum(x - mInitialMotionX);
                        obtainSpringBackDistance = obtainSpringBackDistance(x - mInitialMotionX, mode);
                    }
                    requestDisallowParentInterceptTouchEvent(true);
                    moveTarget(signum * obtainSpringBackDistance, mode);
                }
            } else {
                if (actionMasked == MotionEvent.ACTION_CANCEL) {
                    return false;
                }
                if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                    int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                        return false;
                    }
                    int actionIndex;
                    if (mode == 2) {
                        float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float y3 = motionEvent.getY(actionIndex) - y2;
                        mInitialDownY = y3;
                        mInitialMotionY = y3;
                    } else {
                        float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float x3 = motionEvent.getX(actionIndex) - x2;
                        mInitialDownX = x3;
                        mInitialMotionX = x3;
                    }
                    mActivePointerId = motionEvent.getPointerId(actionIndex);
                } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
                    onSecondaryPointerUp(motionEvent);
                }
            }
        }
        return true;
    }

    private void checkVerticalScrollStart(int mode) {
        if (getScrollY() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollY()), Math.abs(obtainMaxSpringBackDistance(mode)), 2);
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

    private void checkScrollStart(int mode) {
        if (mode == 2) {
            checkVerticalScrollStart(mode);
        } else {
            checkHorizontalScrollStart(mode);
        }
    }

    private void checkHorizontalScrollStart(int mode) {
        if (getScrollX() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollX()), Math.abs(obtainMaxSpringBackDistance(mode)), 2);
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

    private boolean onScrollDownEvent(MotionEvent motionEvent, int actionMasked, int mode) {
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(mode);
        } else {
            if (actionMasked != MotionEvent.ACTION_UP) {
                if (actionMasked == MotionEvent.ACTION_MOVE) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (mode == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(y - mInitialMotionY);
                            obtainSpringBackDistance = obtainSpringBackDistance(y - mInitialMotionY, mode);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(x - mInitialMotionX);
                            obtainSpringBackDistance = obtainSpringBackDistance(x - mInitialMotionX, mode);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(f, mode);
                        } else {
                            moveTarget(0.0f, mode);
                            return false;
                        }
                    }
                } else if (actionMasked != MotionEvent.ACTION_CANCEL) {
                    if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (mode == 2) {
                            float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            mInitialDownY = y3;
                            mInitialMotionY = y3;
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            mInitialDownX = x3;
                            mInitialMotionX = x3;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
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
                springBack(mode);
            }
            mActivePointerId = -1;
            return false;
        }
        return true;
    }

    private void moveTarget(float xory, int mode) {
        if (mode == 2) {
            scrollTo(0, (int) (-xory));
        } else {
            scrollTo((int) (-xory), 0);
        }
    }

    private void springBack(int mode) {
        springBack(0.0f, mode, true);
    }

    private void springBack(float xory, int mode, boolean invalidateAnimation) {
        if (mOnSpringListener == null || !mOnSpringListener.onSpringBack()) {
            mSpringScroller.forceStop();
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            mSpringScroller.scrollByFling(scrollX, 0.0f, scrollY, 0.0f, xory, mode);
            if (scrollX == 0 && scrollY == 0 && xory == 0.0f) {
                dispatchScrollState(0);
            } else {
                dispatchScrollState(2);
            }
            if (invalidateAnimation) {
                AnimationHelper.postInvalidateOnAnimation(this);
            }
        }
    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int actionMasked, int mode) {
        if (actionMasked == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(mode);
        } else {
            if (actionMasked != MotionEvent.ACTION_UP) {
                if (actionMasked == MotionEvent.ACTION_MOVE) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (mode == 2) {
                            float y = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(mInitialMotionY - y);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionY - y, mode);
                        } else {
                            float x = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(mInitialMotionX - x);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionX - x, mode);
                        }
                        float f = signum * obtainSpringBackDistance;
                        if (f > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(-f, mode);
                        } else {
                            moveTarget(0.0f, mode);
                            return false;
                        }
                    }
                } else if (actionMasked != MotionEvent.ACTION_CANCEL) {
                    if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (mode == 2) {
                            float y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y3 = motionEvent.getY(actionIndex) - y2;
                            mInitialDownY = y3;
                            mInitialMotionY = y3;
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x3 = motionEvent.getX(actionIndex) - x2;
                            mInitialDownX = x3;
                            mInitialMotionX = x3;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
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
                springBack(mode);
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

    protected int getSpringBackRange(int range) {
        return range == 2 ? mScreenHeight : mScreenWidth;
    }

    protected float obtainSpringBackDistance(float distance, int mode) {
        int springBackRange = getSpringBackRange(mode);
        return obtainDampingDistance(Math.min(Math.abs(distance) / springBackRange, 1.0f), springBackRange);
    }

    protected float obtainMaxSpringBackDistance(int mode) {
        return obtainDampingDistance(1.0f, getSpringBackRange(mode));
    }

    protected float obtainDampingDistance(float distance, int range) {
        double min = Math.min(distance, 1.0f);
        return ((float) (((Math.pow(min, 3.0d) / 3.0d) - Math.pow(min, 2.0d)) + min)) * range;
    }

    protected float obtainTouchDistance(float distance, float max, int mode) {
        int springBackRange = getSpringBackRange(mode);
        if (Math.abs(distance) >= Math.abs(max))
            distance = max;

        return (float) ((double) springBackRange - (Math.pow(springBackRange, 0.6666666666666666d) * Math.pow(springBackRange - (distance * 3.0f), 0.3333333333333333d)));
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        boolean isVertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        int dxordyconsumed = isVertical ? dyConsumed : dxConsumed;
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type, consumed);
        if (mSpringBackEnable) {
            int unConsumed = isVertical ? dyUnconsumed : dxUnconsumed;
            int state = isVertical ? 2 : 1;
            if (unConsumed < 0 && isTargetScrollToTop(state) && supportTopSpringBackMode()) {
                if (type != 0) {
                    float maxSpringBackDistance = obtainMaxSpringBackDistance(state);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (dxordyconsumed != 0 && (-unConsumed) <= maxSpringBackDistance) {
                            mSpringScroller.setFirstStep(unConsumed);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollTopUnconsumed != 0.0f) {
                        return;
                    }
                    float f = maxSpringBackDistance - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f <= Math.abs(unConsumed)) {
                            mTotalFlingUnconsumed += f;
                            consumed[1] = (int) (consumed[1] + f);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(unConsumed);
                            consumed[1] = consumed[1] + unConsumed;
                        }
                        dispatchScrollState(2);
                        moveTarget(obtainSpringBackDistance(mTotalFlingUnconsumed, state), state);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollTopUnconsumed += Math.abs(unConsumed);
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, state), state);
                    consumed[1] = consumed[1] + unConsumed;
                    return;
                }
                return;
            }
            if (unConsumed > 0 && isTargetScrollToBottom(state) && supportBottomSpringBackMode()) {
                if (type != 0) {
                    float maxSpringBackDistance = obtainMaxSpringBackDistance(state);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (dxordyconsumed != 0 && unConsumed <= maxSpringBackDistance) {
                            mSpringScroller.setFirstStep(unConsumed);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollBottomUnconsumed != 0.0f) {
                        return;
                    }
                    float f2 = maxSpringBackDistance - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f2 <= Math.abs(unConsumed)) {
                            mTotalFlingUnconsumed += f2;
                            consumed[1] = (int) (consumed[1] + f2);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(unConsumed);
                            consumed[1] = consumed[1] + unConsumed;
                        }
                        dispatchScrollState(2);
                        moveTarget(-obtainSpringBackDistance(mTotalFlingUnconsumed, state), state);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollBottomUnconsumed += Math.abs(unConsumed);
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, state), state);
                    consumed[1] = consumed[1] + unConsumed;
                }
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollAxes = axes;
        boolean isVertical = axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        if (((isVertical ? 2 : 1) & mOriginScrollOrientation) == 0) {
            return false;
        }
        if (mSpringBackEnable) {
            if (!onStartNestedScroll(child, child, axes)) {
                return false;
            }
            float scrollY = isVertical ? getScrollY() : getScrollX();
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
            boolean isVertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
            int state = isVertical ? 2 : 1;
            float scrollY = isVertical ? getScrollY() : getScrollX();
            if (type != 0) {
                if (scrollY == 0.0f) {
                    mTotalFlingUnconsumed = 0.0f;
                } else {
                    mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(state)), state);
                }
                mNestedFlingInProgress = true;
                consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0f) {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else if (scrollY < 0.0f) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(state)), state);
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(state)), state);
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
            if (mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) {
                onNestedPreScroll(dy, consumed, type);
            } else {
                onNestedPreScroll(dx, consumed, type);
            }
        }
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], mParentScrollConsumed, type, null)) {
            consumed[0] = consumed[0] + mParentScrollConsumed[0];
            consumed[1] = consumed[1] + mParentScrollConsumed[1];
        }
    }

    private void onNestedPreScroll(int dy, int[] consumed, int type) {
        boolean isVertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        int state = isVertical ? 2 : 1;
        int abs = Math.abs(isVertical ? getScrollY() : getScrollX());
        float f = 0.0f;
        if (type == 0) {
            if (dy > 0) {
                if (mTotalScrollTopUnconsumed > 0.0f) {
                    if ((float) dy > mTotalScrollTopUnconsumed) {
                        consumeDelta((int) mTotalScrollTopUnconsumed, consumed, state);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        mTotalScrollTopUnconsumed = mTotalScrollTopUnconsumed - (float) dy;
                        consumeDelta(dy, consumed, state);
                    }
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, state), state);
                    return;
                }
            }
            if (dy < 0) {
                if ((-mTotalScrollBottomUnconsumed) < 0.0f) {
                    if ((float) dy < (-mTotalScrollBottomUnconsumed)) {
                        consumeDelta((int) mTotalScrollBottomUnconsumed, consumed, state);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        mTotalScrollBottomUnconsumed = mTotalScrollBottomUnconsumed + (float) dy;
                        consumeDelta(dy, consumed, state);
                    }
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, state), state);
                    return;
                }
                return;
            }
            return;
        }
        float xory = state == 2 ? mVelocityY : mVelocityX;
        if (dy > 0) {
            if (mTotalScrollTopUnconsumed > 0.0f) {
                if (xory > 2000.0f) {
                    float springBackDistance = obtainSpringBackDistance(mTotalScrollTopUnconsumed, state);
                    if ((float) dy > springBackDistance) {
                        consumeDelta((int) springBackDistance, consumed, state);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        consumeDelta(dy, consumed, state);
                        f = springBackDistance - (float) dy;
                        mTotalScrollTopUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(state)), state);
                    }
                    moveTarget(f, state);
                    dispatchScrollState(1);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(xory, state, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollTopUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(state)), state);
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                }
                consumeDelta(dy, consumed, state);
                return;
            }
        }
        if (dy < 0) {
            if ((-mTotalScrollBottomUnconsumed) < 0.0f) {
                if (xory < -2000.0f) {
                    float springBackDistance = obtainSpringBackDistance(mTotalScrollBottomUnconsumed, state);
                    if ((float) dy < (-springBackDistance)) {
                        consumeDelta((int) springBackDistance, consumed, state);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        consumeDelta(dy, consumed, state);
                        f = springBackDistance + (float) dy;
                        mTotalScrollBottomUnconsumed = obtainTouchDistance(f, Math.signum(f) * Math.abs(obtainMaxSpringBackDistance(state)), state);
                    }
                    dispatchScrollState(1);
                    moveTarget(-f, state);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(xory, state, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(state)), state);
                } else {
                    mTotalScrollBottomUnconsumed = 0.0f;
                }
                consumeDelta(dy, consumed, state);
                return;
            }
        }
        if (dy != 0) {
            if ((mTotalScrollBottomUnconsumed == 0.0f || mTotalScrollTopUnconsumed == 0.0f) && mScrollByFling && getScrollY() == 0) {
                consumeDelta(dy, consumed, state);
            }
        }
    }

    private void consumeDelta(int dy, int[] consumed, int state) {
        if (state == 2) {
            consumed[1] = dy;
        } else {
            consumed[0] = dy;
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
            boolean isVertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
            int state = isVertical ? 2 : 1;
            if (mNestedScrollInProgress) {
                mNestedScrollInProgress = false;
                float scrollY = isVertical ? getScrollY() : getScrollX();
                if (!mNestedFlingInProgress && scrollY != 0.0f) {
                    springBack(state);
                } else {
                    if (scrollY != 0.0f) {
                        stopNestedFlingScroll(state);
                        return;
                    }
                }
                return;
            }
            if (mNestedFlingInProgress) {
                stopNestedFlingScroll(state);
            }
        }
    }

    private void stopNestedFlingScroll(int state) {
        mNestedFlingInProgress = false;
        if (mScrollByFling) {
            if (mSpringScroller.isFinished()) {
                springBack(state == 2 ? mVelocityY : mVelocityX, state, false);
            }
            AnimationHelper.postInvalidateOnAnimation(this);
            return;
        }
        springBack(state);
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

    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] parentOffsetInWindow, int type, int[] consumed) {
        mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, parentOffsetInWindow, type, consumed);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll(int type) {
        mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    public boolean dispatchNestedPreScroll(int dxUnconsumed, int dyUnconsumed, int[] parentOffsetInWindow, int type, int[] consumed) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dxUnconsumed, dyUnconsumed, parentOffsetInWindow, consumed, type);
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
        Point screenSize = getScreenSize(getContext());
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void dispatchScrollState(int state) {
        int last = mScrollState;
        if (last != state) {
            mScrollState = state;
            for (ViewCompatOnScrollChangeListener mOnScrollChangeListener : mOnScrollChangeListeners) {
                mOnScrollChangeListener.onStateChanged(last, state, mSpringScroller.isFinished());
            }
        }
    }

    public void setOnSpringListener(OnSpringListener onSpringListener) {
        mOnSpringListener = onSpringListener;
    }

    @Override
    public boolean onNestedCurrentFling(float velocityX, float velocityY) {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        return true;
    }

    public interface OnSpringListener {
        boolean onSpringBack();
    }
}