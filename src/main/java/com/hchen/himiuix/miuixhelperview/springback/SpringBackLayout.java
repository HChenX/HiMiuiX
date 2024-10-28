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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
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
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

import java.util.ArrayList;
import java.util.List;

/** @noinspection NullableProblems */
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3, NestedCurrentFling, ScrollStateDispatcher {
    private static final String TAG = "SpringBackLayout";
    public static final int ANGLE = 4;
    public static final int HORIZONTAL = 1;
    private static final int INVALID_ID = -1;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    public static final int SPRING_BACK_BOTTOM = 2;
    public static final int SPRING_BACK_TOP = 1;
    public static final int UNCHECK_ORIENTATION = 0;
    private static final int VELOCITY_THRADHOLD = 2000;
    public static final int VERTICAL = 2;
    private int consumeNestFlingCounter;
    private int mActivePointerId;
    private final SpringBackLayoutHelper mHelper;
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
    private final List<ViewCompatOnScrollChangeListener> mOnScrollChangeListeners;
    private OnSpringListener mOnSpringListener;
    private int mOriginScrollOrientation;
    private final int[] mParentOffsetInWindow;
    private final int[] mParentScrollConsumed;
    public int mScreenHeight;
    public int mScreenWidth;
    private boolean mScrollByFling;
    private int mScrollOrientation;
    private int mScrollState;
    private boolean mSpringBackEnable;
    private int mSpringBackMode;
    private final SpringScroller mSpringScroller;
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
        mActivePointerId = -1;
        consumeNestFlingCounter = 0;
        mParentScrollConsumed = new int[2];
        mParentOffsetInWindow = new int[2];
        mNestedScrollingV2ConsumedCompat = new int[2];
        mSpringBackEnable = true;
        mOnScrollChangeListeners = new ArrayList<>();
        mScrollState = 0;
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SpringBackLayout);
        mTargetId = obtainStyledAttributes.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        mOriginScrollOrientation = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
        mSpringBackMode = obtainStyledAttributes.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        obtainStyledAttributes.recycle();
        mSpringScroller = new SpringScroller();
        mHelper = new SpringBackLayoutHelper(this, mOriginScrollOrientation);
        setNestedScrollingEnabled(true);
        Point screenSize = MiuiXUtils.getScreenSize(context);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    private void checkHorizontalScrollStart(int i2) {
        if (getScrollX() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollX()), Math.abs(obtainMaxSpringBackDistance(i2)), 2);
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

    private void checkOrientation(MotionEvent motionEvent) {
        int i2;
        mHelper.checkOrientation(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (mScrollOrientation != 0 || (i2 = mHelper.mScrollOrientation) == 0) {
                        return;
                    }
                    mScrollOrientation = i2;
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
            return;
        }
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
    }

    private void checkScrollStart(int i2) {
        if (i2 == 2) {
            checkVerticalScrollStart(i2);
        } else {
            checkHorizontalScrollStart(i2);
        }
    }

    private void checkVerticalScrollStart(int i2) {
        if (getScrollY() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance(Math.abs(getScrollY()), Math.abs(obtainMaxSpringBackDistance(i2)), 2);
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

    private void consumeDelta(int i2, int[] iArr, int i3) {
        if (i3 == 2) {
            iArr[1] = i2;
        } else {
            iArr[0] = i2;
        }
    }

    private void disallowParentInterceptTouchEvent(boolean z2) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(z2);
        }
    }

    private void dispatchScrollState(int i2) {
        int i3 = mScrollState;
        if (i3 != i2) {
            mScrollState = i2;
            for (ViewCompatOnScrollChangeListener mOnScrollChangeListener : mOnScrollChangeListeners) {
                mOnScrollChangeListener.onStateChanged(i3, i2, mSpringScroller.isFinished());
            }
        }
    }

    private void ensureTarget() {
        if (mTarget == null) {
            int i2 = mTargetId;
            if (i2 != -1) {
                mTarget = findViewById(i2);
            } else {
                throw new IllegalArgumentException("invalid target Id");
            }
        }
        if (mTarget != null) {
            if (Build.VERSION.SDK_INT >= 21 && isEnabled()) {
                View view = mTarget;
                if ((view instanceof NestedScrollingChild3) && !view.isNestedScrollingEnabled()) {
                    mTarget.setNestedScrollingEnabled(true);
                }
            }
            if (mTarget.getOverScrollMode() != 2) {
                mTarget.setOverScrollMode(2);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("fail to get target");
    }

    private boolean isHorizontalTargetScrollToTop() {
        return !mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollOrientation(int i2) {
        return mScrollOrientation == i2;
    }

    private boolean isTargetScrollToBottom(int i2) {
        if (i2 == 2) {
            if (mTarget instanceof ListView) {
                return !((ListView) mTarget).canScrollList(1);
            }
            return !mTarget.canScrollVertically(1);
        }
        return !mTarget.canScrollHorizontally(1);
    }

    private boolean isTargetScrollToTop(int i2) {
        if (i2 == 2) {
            if (mTarget instanceof ListView) {
                return !((ListView) mTarget).canScrollList(-1);
            }
            return !mTarget.canScrollVertically(-1);
        }
        return !mTarget.canScrollHorizontally(-1);
    }

    private boolean isVerticalTargetScrollToTop() {
        if (mTarget instanceof ListView) {
            return !((ListView) mTarget).canScrollList(-1);
        }
        return !mTarget.canScrollVertically(-1);
    }

    private void moveTarget(float f2, int i2) {
        if (i2 == 2) {
            scrollTo(0, (int) (-f2));
        } else {
            scrollTo((int) (-f2), 0);
        }
    }

    private boolean onHorizontalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z2 = false;
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
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int i2 = mActivePointerId;
                    if (i2 == -1) {
                        Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                        return false;
                    }
                    int findPointerIndex = motionEvent.findPointerIndex(i2);
                    if (findPointerIndex < 0) {
                        Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    float x2 = motionEvent.getX(findPointerIndex);
                    if (isTargetScrollToBottom(1) && isTargetScrollToTop(1)) {
                        z2 = true;
                    }
                    if ((z2 || !isTargetScrollToTop(1)) && (!z2 || x2 <= mInitialDownX)) {
                        if (mInitialDownX - x2 > mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionX = x2;
                        }
                    } else if (x2 - mInitialDownX > mTouchSlop && !mIsBeingDragged) {
                        mIsBeingDragged = true;
                        dispatchScrollState(1);
                        mInitialMotionX = x2;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            mIsBeingDragged = false;
            mActivePointerId = -1;
        } else {
            int pointerId = motionEvent.getPointerId(0);
            mActivePointerId = pointerId;
            int findPointerIndex2 = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex2 < 0) {
                return false;
            }
            mInitialDownX = motionEvent.getX(findPointerIndex2);
            if (getScrollX() != 0) {
                mIsBeingDragged = true;
                mInitialMotionX = mInitialDownX;
            } else {
                mIsBeingDragged = false;
            }
        }
        return mIsBeingDragged;
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

    private boolean onScrollDownEvent(MotionEvent motionEvent, int i2, int i3) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i2 != 0) {
            if (i2 != 1) {
                if (i2 == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        if (i3 == 2) {
                            float y2 = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(y2 - mInitialMotionY);
                            obtainSpringBackDistance = obtainSpringBackDistance(y2 - mInitialMotionY, i3);
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(x2 - mInitialMotionX);
                            obtainSpringBackDistance = obtainSpringBackDistance(x2 - mInitialMotionX, i3);
                        }
                        float f2 = signum * obtainSpringBackDistance;
                        if (f2 > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(f2, i3);
                        } else {
                            moveTarget(0.0f, i3);
                            return false;
                        }
                    }
                } else if (i2 != 3) {
                    if (i2 == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i3 == 2) {
                            float y3 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y4 = motionEvent.getY(actionIndex) - y3;
                            mInitialDownY = y4;
                            mInitialMotionY = y4;
                        } else {
                            float x3 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x4 = motionEvent.getX(actionIndex) - x3;
                            mInitialDownX = x4;
                            mInitialMotionX = x4;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i2 == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (mIsBeingDragged) {
                mIsBeingDragged = false;
                springBack(i3);
            }
            mActivePointerId = -1;
            return false;
        }
        mActivePointerId = motionEvent.getPointerId(0);
        checkScrollStart(i3);
        return true;
    }

    private boolean onScrollEvent(MotionEvent motionEvent, int i2, int i3) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i2 == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i3);
        } else {
            if (i2 == 1) {
                if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(i3);
                }
                mActivePointerId = -1;
                return false;
            }
            if (i2 == 2) {
                int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                if (findPointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    if (i3 == 2) {
                        float y2 = motionEvent.getY(findPointerIndex);
                        signum = Math.signum(y2 - mInitialMotionY);
                        obtainSpringBackDistance = obtainSpringBackDistance(y2 - mInitialMotionY, i3);
                    } else {
                        float x2 = motionEvent.getX(findPointerIndex);
                        signum = Math.signum(x2 - mInitialMotionX);
                        obtainSpringBackDistance = obtainSpringBackDistance(x2 - mInitialMotionX, i3);
                    }
                    requestDisallowParentInterceptTouchEvent(true);
                    moveTarget(signum * obtainSpringBackDistance, i3);
                }
            } else {
                if (i2 == 3) {
                    return false;
                }
                if (i2 == 5) {
                    int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                        return false;
                    }
                    if (i3 == 2) {
                        float y3 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float y4 = motionEvent.getY(actionIndex) - y3;
                        mInitialDownY = y4;
                        mInitialMotionY = y4;
                    } else {
                        float x3 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                        actionIndex = motionEvent.getActionIndex();
                        if (actionIndex < 0) {
                            Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                            return false;
                        }
                        float x4 = motionEvent.getX(actionIndex) - x3;
                        mInitialDownX = x4;
                        mInitialMotionX = x4;
                    }
                    mActivePointerId = motionEvent.getPointerId(actionIndex);
                } else if (i2 == 6) {
                    onSecondaryPointerUp(motionEvent);
                }
            }
        }
        return true;
    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int i2, int i3) {
        float signum;
        float obtainSpringBackDistance;
        int actionIndex;
        if (i2 != 0) {
            if (i2 != 1) {
                if (i2 == 2) {
                    int findPointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex < 0) {
                        Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    if (mIsBeingDragged) {
                        if (i3 == 2) {
                            float y2 = motionEvent.getY(findPointerIndex);
                            signum = Math.signum(mInitialMotionY - y2);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionY - y2, i3);
                        } else {
                            float x2 = motionEvent.getX(findPointerIndex);
                            signum = Math.signum(mInitialMotionX - x2);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionX - x2, i3);
                        }
                        float f2 = signum * obtainSpringBackDistance;
                        if (f2 > 0.0f) {
                            requestDisallowParentInterceptTouchEvent(true);
                            moveTarget(-f2, i3);
                        } else {
                            moveTarget(0.0f, i3);
                            return false;
                        }
                    }
                } else if (i2 != 3) {
                    if (i2 == 5) {
                        int findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }
                        if (i3 == 2) {
                            float y3 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float y4 = motionEvent.getY(actionIndex) - y3;
                            mInitialDownY = y4;
                            mInitialMotionY = y4;
                        } else {
                            float x3 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }
                            float x4 = motionEvent.getX(actionIndex) - x3;
                            mInitialDownX = x4;
                            mInitialMotionX = x4;
                        }
                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i2 == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            }
            if (mIsBeingDragged) {
                mIsBeingDragged = false;
                springBack(i3);
            }
            mActivePointerId = -1;
            return false;
        }
        mActivePointerId = motionEvent.getPointerId(0);
        checkScrollStart(i3);
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == mActivePointerId) {
            mActivePointerId = motionEvent.getPointerId(actionIndex == 0 ? 1 : 0);
        }
    }

    private boolean onVerticalInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z2 = false;
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
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int i2 = mActivePointerId;
                    if (i2 == -1) {
                        Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                        return false;
                    }
                    int findPointerIndex = motionEvent.findPointerIndex(i2);
                    if (findPointerIndex < 0) {
                        Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }
                    float y2 = motionEvent.getY(findPointerIndex);
                    if (isTargetScrollToBottom(2) && isTargetScrollToTop(2)) {
                        z2 = true;
                    }
                    if ((z2 || !isTargetScrollToTop(2)) && (!z2 || y2 <= mInitialDownY)) {
                        if (mInitialDownY - y2 > mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionY = y2;
                        }
                    } else if (y2 - mInitialDownY > mTouchSlop && !mIsBeingDragged) {
                        mIsBeingDragged = true;
                        dispatchScrollState(1);
                        mInitialMotionY = y2;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            mIsBeingDragged = false;
            mActivePointerId = -1;
        } else {
            int pointerId = motionEvent.getPointerId(0);
            mActivePointerId = pointerId;
            int findPointerIndex2 = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex2 < 0) {
                return false;
            }
            mInitialDownY = motionEvent.getY(findPointerIndex2);
            if (getScrollY() != 0) {
                mIsBeingDragged = true;
                mInitialMotionY = mInitialDownY;
            } else {
                mIsBeingDragged = false;
            }
        }
        return mIsBeingDragged;
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

    private void springBack(int i2) {
        springBack(0.0f, i2, true);
    }

    private void stopNestedFlingScroll(int i2) {
        mNestedFlingInProgress = false;
        if (mScrollByFling) {
            if (mSpringScroller.isFinished()) {
                springBack(i2 == 2 ? mVelocityY : mVelocityX, i2, false);
            }
            postInvalidateOnAnimation();
            return;
        }
        springBack(i2);
    }

    private boolean supportBottomSpringBackMode() {
        return (mSpringBackMode & 2) != 0;
    }

    private boolean supportTopSpringBackMode() {
        return (mSpringBackMode & 1) != 0;
    }

    @Override // miuix.core.view.ScrollStateDispatcher
    public void addOnScrollChangeListener(ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener) {
        mOnScrollChangeListeners.add(viewCompatOnScrollChangeListener);
    }

    @Override // android.view.View
    public void computeScroll() {
        super.computeScroll();
        if (mSpringScroller.computeScrollOffset()) {
            scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
            if (!mSpringScroller.isFinished()) {
                postInvalidateOnAnimation();
                return;
            }
            if (getScrollX() != 0 || getScrollY() != 0) {
                if (mScrollState != 2) {
                    Log.d(TAG, "Scroll stop but state is not correct.");
                    springBack(mNestedScrollAxes == 2 ? 2 : 1);
                    return;
                }
            }
            dispatchScrollState(0);
        }
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public boolean dispatchNestedFling(float f2, float f3, boolean z2) {
        return mNestedScrollingChildHelper.dispatchNestedFling(f2, f3, z2);
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public boolean dispatchNestedPreFling(float f2, float f3) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(f2, f3);
    }

    @Override // androidx.core.view.NestedScrollingChild2
    public boolean dispatchNestedPreScroll(int i2, int i3, int[] iArr, int[] iArr2, int i4) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(i2, i3, iArr, iArr2, i4);
    }

    @Override // androidx.core.view.NestedScrollingChild3
    public void dispatchNestedScroll(int i2, int i3, int i4, int i5, int[] iArr, int i6, int[] iArr2) {
        mNestedScrollingChildHelper.dispatchNestedScroll(i2, i3, i4, i5, iArr, i6, iArr2);
    }

    @Override // android.view.ViewGroup, android.view.View
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

    public int getSpringBackMode() {
        return mSpringBackMode;
    }

    public int getSpringBackRange(int i2) {
        return i2 == 2 ? mScreenHeight : mScreenWidth;
    }

    public View getTarget() {
        return mTarget;
    }

    @Override // androidx.core.view.NestedScrollingChild2
    public boolean hasNestedScrollingParent(int i2) {
        return mNestedScrollingChildHelper.hasNestedScrollingParent(i2);
    }

    public boolean hasSpringListener() {
        return mOnSpringListener != null;
    }

    public void internalRequestDisallowInterceptTouchEvent(boolean z2) {
        super.requestDisallowInterceptTouchEvent(z2);
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public float obtainDampingDistance(float f2, int i2) {
        double min = Math.min(f2, 1.0f);
        return ((float) (((Math.pow(min, 3.0d) / 3.0d) - Math.pow(min, 2.0d)) + min)) * i2;
    }

    public float obtainMaxSpringBackDistance(int i2) {
        return obtainDampingDistance(1.0f, getSpringBackRange(i2));
    }

    public float obtainSpringBackDistance(float f2, int i2) {
        int springBackRange = getSpringBackRange(i2);
        return obtainDampingDistance(Math.min(Math.abs(f2) / springBackRange, 1.0f), springBackRange);
    }

    public float obtainTouchDistance(float f2, float f3, int i2) {
        int springBackRange = getSpringBackRange(i2);
        if (Math.abs(f2) >= Math.abs(f3)) {
            f2 = f3;
        }
        return (float) ((double) springBackRange - (Math.pow(springBackRange, 0.6666666666666666d) * Math.pow(springBackRange - (f2 * 3.0f), 0.3333333333333333d)));
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Point screenSize = MiuiXUtils.getScreenSize(getContext());
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        mInitPaddingTop = getPaddingTop();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!mSpringBackEnable || !isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || (Build.VERSION.SDK_INT >= 21 && mTarget.isNestedScrollingEnabled())) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (!mSpringScroller.isFinished() && actionMasked == 0) {
            mSpringScroller.forceStop();
        }
        if (!supportTopSpringBackMode() && !supportBottomSpringBackMode()) {
            return false;
        }
        int i2 = mOriginScrollOrientation;
        if ((i2 & 4) != 0) {
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
            mScrollOrientation = i2;
        }
        if (isTargetScrollOrientation(2)) {
            return onVerticalInterceptTouchEvent(motionEvent);
        }
        if (isTargetScrollOrientation(1)) {
            return onHorizontalInterceptTouchEvent(motionEvent);
        }
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z2, int i2, int i3, int i4, int i5) {
        if (mTarget.getVisibility() != View.GONE) {
            int measuredWidth = mTarget.getMeasuredWidth();
            int measuredHeight = mTarget.getMeasuredHeight();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            mTarget.layout(paddingLeft, paddingTop, measuredWidth + paddingLeft, measuredHeight + paddingTop);
        }
    }

    @Override // android.view.View
    public void onMeasure(int i2, int i3) {
        int min;
        int min2;
        ensureTarget();
        int mode = View.MeasureSpec.getMode(i2);
        int mode2 = View.MeasureSpec.getMode(i3);
        measureChild(mTarget, i2, i3);
        if (mode == 0) {
            min = mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        } else if (mode == 1073741824) {
            min = View.MeasureSpec.getSize(i2);
        } else {
            min = Math.min(View.MeasureSpec.getSize(i2), mTarget.getMeasuredWidth() + getPaddingLeft() + getPaddingRight());
        }
        if (mode2 == 0) {
            min2 = mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        } else if (mode2 == 1073741824) {
            min2 = View.MeasureSpec.getSize(i3);
        } else {
            min2 = Math.min(View.MeasureSpec.getSize(i3), mTarget.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(min, min2);
    }

    @Override // miuix.core.view.NestedCurrentFling
    public boolean onNestedCurrentFling(float f2, float f3) {
        mVelocityX = f2;
        mVelocityY = f3;
        return true;
    }

    @Override
    // android.view.ViewGroup, android.view.ViewParent, androidx.core.view.NestedScrollingParent
    public boolean onNestedFling(View view, float f2, float f3, boolean z2) {
        return dispatchNestedFling(f2, f3, z2);
    }

    @Override
    // android.view.ViewGroup, android.view.ViewParent, androidx.core.view.NestedScrollingParent
    public boolean onNestedPreFling(View view, float f2, float f3) {
        return dispatchNestedPreFling(f2, f3);
    }

    @Override // androidx.core.view.NestedScrollingParent2
    public void onNestedPreScroll(View view, int i2, int i3, int[] iArr, int i4) {
        if (mSpringBackEnable) {
            if (mNestedScrollAxes == 2) {
                onNestedPreScroll(i3, iArr, i4);
            } else {
                onNestedPreScroll(i2, iArr, i4);
            }
        }
        int[] iArr2 = mParentScrollConsumed;
        if (dispatchNestedPreScroll(i2 - iArr[0], i3 - iArr[1], iArr2, null, i4)) {
            iArr[0] = iArr[0] + iArr2[0];
            iArr[1] = iArr[1] + iArr2[1];
        }
    }

    @Override // androidx.core.view.NestedScrollingParent3
    public void onNestedScroll(View view, int i2, int i3, int i4, int i5, int i6, int[] iArr) {
        boolean z2 = mNestedScrollAxes == 2;
        int i7 = z2 ? i3 : i2;
        int i8 = z2 ? iArr[1] : iArr[0];
        dispatchNestedScroll(i2, i3, i4, i5, mParentOffsetInWindow, i6, iArr);
        if (mSpringBackEnable) {
            int i9 = (z2 ? iArr[1] : iArr[0]) - i8;
            int i10 = z2 ? i5 - i9 : i4 - i9;
            int i12 = z2 ? 2 : 1;
            if (i10 < 0 && isTargetScrollToTop(i12) && supportTopSpringBackMode()) {
                if (i6 != 0) {
                    float obtainMaxSpringBackDistance = obtainMaxSpringBackDistance(i12);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (i7 != 0 && (-i10) <= obtainMaxSpringBackDistance) {
                            mSpringScroller.setFirstStep(i10);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollTopUnconsumed != 0.0f) {
                        return;
                    }
                    float f2 = obtainMaxSpringBackDistance - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f2 <= Math.abs(i10)) {
                            mTotalFlingUnconsumed += f2;
                            iArr[1] = (int) (iArr[1] + f2);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(i10);
                            iArr[1] = iArr[1] + i10;
                        }
                        dispatchScrollState(2);
                        moveTarget(obtainSpringBackDistance(mTotalFlingUnconsumed, i12), i12);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollTopUnconsumed += Math.abs(i10);
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i12), i12);
                    iArr[1] = iArr[1] + i10;
                    return;
                }
                return;
            }
            if (i10 > 0 && isTargetScrollToBottom(i12) && supportBottomSpringBackMode()) {
                if (i6 != 0) {
                    float obtainMaxSpringBackDistance2 = obtainMaxSpringBackDistance(i12);
                    if (mVelocityY != 0.0f || mVelocityX != 0.0f) {
                        mScrollByFling = true;
                        if (i7 != 0 && i10 <= obtainMaxSpringBackDistance2) {
                            mSpringScroller.setFirstStep(i10);
                        }
                        dispatchScrollState(2);
                        return;
                    }
                    if (mTotalScrollBottomUnconsumed != 0.0f) {
                        return;
                    }
                    float f3 = obtainMaxSpringBackDistance2 - mTotalFlingUnconsumed;
                    if (consumeNestFlingCounter < 4) {
                        if (f3 <= Math.abs(i10)) {
                            mTotalFlingUnconsumed += f3;
                            iArr[1] = (int) (iArr[1] + f3);
                        } else {
                            mTotalFlingUnconsumed += Math.abs(i10);
                            iArr[1] = iArr[1] + i10;
                        }
                        dispatchScrollState(2);
                        moveTarget(-obtainSpringBackDistance(mTotalFlingUnconsumed, i12), i12);
                        consumeNestFlingCounter++;
                        return;
                    }
                    return;
                }
                if (mSpringScroller.isFinished()) {
                    mTotalScrollBottomUnconsumed += Math.abs(i10);
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i12), i12);
                    iArr[1] = iArr[1] + i10;
                }
            }
        }
    }

    @Override // androidx.core.view.NestedScrollingParent2
    public void onNestedScrollAccepted(View view, View view2, int i2, int i3) {
        if (mSpringBackEnable) {
            boolean z2 = mNestedScrollAxes == 2;
            int i4 = z2 ? 2 : 1;
            float scrollY = z2 ? getScrollY() : getScrollX();
            if (i3 != 0) {
                if (scrollY == 0.0f) {
                    mTotalFlingUnconsumed = 0.0f;
                } else {
                    mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                }
                mNestedFlingInProgress = true;
                consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0f) {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else if (scrollY < 0.0f) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                    mTotalScrollBottomUnconsumed = 0.0f;
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                }
                mNestedScrollInProgress = true;
            }
            mVelocityY = 0.0f;
            mVelocityX = 0.0f;
            mScrollByFling = false;
            mSpringScroller.forceStop();
        }
        onNestedScrollAccepted(view, view2, i2);
    }

    @Override // android.view.View
    public void onScrollChanged(int i2, int i3, int i4, int i5) {
        super.onScrollChanged(i2, i3, i4, i5);
        for (ViewCompatOnScrollChangeListener mOnScrollChangeListener : mOnScrollChangeListeners) {
            mOnScrollChangeListener.onScrollChange(this, i2, i3, i4, i5);
        }
    }

    @Override // androidx.core.view.NestedScrollingParent2
    public boolean onStartNestedScroll(View view, View view2, int i2, int i3) {
        mNestedScrollAxes = i2;
        boolean z2 = i2 == 2;
        if (((z2 ? 2 : 1) & mOriginScrollOrientation) == 0) {
            return false;
        }
        if (mSpringBackEnable) {
            if (!onStartNestedScroll(view, view, i2)) {
                return false;
            }
            float scrollY = z2 ? getScrollY() : getScrollX();
            if (i3 != 0 && scrollY != 0.0f && (mTarget instanceof NestedScrollView)) {
                return false;
            }
        }
        mNestedScrollingChildHelper.startNestedScroll(i2, i3);
        return true;
    }

    @Override // androidx.core.view.NestedScrollingParent2
    public void onStopNestedScroll(View view, int i2) {
        mNestedScrollingParentHelper.onStopNestedScroll(view, i2);
        stopNestedScroll(i2);
        if (mSpringBackEnable) {
            boolean z2 = mNestedScrollAxes == 2;
            int i3 = z2 ? 2 : 1;
            if (mNestedScrollInProgress) {
                mNestedScrollInProgress = false;
                float scrollY = z2 ? getScrollY() : getScrollX();
                if (!mNestedFlingInProgress && scrollY != 0.0f) {
                    springBack(i3);
                } else {
                    if (scrollY != 0.0f) {
                        stopNestedFlingScroll(i3);
                        return;
                    }
                }
                return;
            }
            if (mNestedFlingInProgress) {
                stopNestedFlingScroll(i3);
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (!isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress || (Build.VERSION.SDK_INT >= 21 && mTarget.isNestedScrollingEnabled())) {
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

    @Override // miuix.core.view.ScrollStateDispatcher
    public void removeOnScrollChangeListener(ViewCompatOnScrollChangeListener viewCompatOnScrollChangeListener) {
        mOnScrollChangeListeners.remove(viewCompatOnScrollChangeListener);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z2) {
        if (isEnabled() && mSpringBackEnable) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(z2);
    }

    public void requestDisallowParentInterceptTouchEvent(boolean z2) {
        ViewParent parent = getParent();
        parent.requestDisallowInterceptTouchEvent(z2);
        while (parent != null) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(z2);
            }
            parent = parent.getParent();
        }
    }

    @Override // android.view.View
    public void setEnabled(boolean z2) {
        super.setEnabled(z2);
        View view = mTarget;
        if (!(view instanceof NestedScrollingChild3) || Build.VERSION.SDK_INT < 21 || z2 == view.isNestedScrollingEnabled()) {
            return;
        }
        mTarget.setNestedScrollingEnabled(z2);
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public void setNestedScrollingEnabled(boolean z2) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(z2);
    }

    public void setOnSpringListener(OnSpringListener onSpringListener) {
        mOnSpringListener = onSpringListener;
    }

    public void setScrollOrientation(int i2) {
        mOriginScrollOrientation = i2;
        mHelper.mTargetScrollOrientation = i2;
    }

    public void setSpringBackEnable(boolean z2) {
        mSpringBackEnable = z2;
    }

    public void setSpringBackMode(int i2) {
        mSpringBackMode = i2;
    }

    public void setTarget(View view) {
        mTarget = view;
        if (Build.VERSION.SDK_INT < 21 || !(view instanceof NestedScrollingChild3) || view.isNestedScrollingEnabled()) {
            return;
        }
        mTarget.setNestedScrollingEnabled(true);
    }

    public void smoothScrollTo(int i2, int i3) {
        if (i2 - getScrollX() == 0 && i3 - getScrollY() == 0) {
            return;
        }
        mSpringScroller.forceStop();
        mSpringScroller.scrollByFling(getScrollX(), i2, getScrollY(), i3, 0.0f, 2, true);
        dispatchScrollState(2);
        postInvalidateOnAnimation();
    }

    public boolean springBackEnable() {
        return mSpringBackEnable;
    }

    @Override // androidx.core.view.NestedScrollingChild2
    public boolean startNestedScroll(int i2, int i3) {
        return mNestedScrollingChildHelper.startNestedScroll(i2, i3);
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    private void springBack(float f2, int i2, boolean z2) {
        OnSpringListener onSpringListener = mOnSpringListener;
        if (onSpringListener == null || !onSpringListener.onSpringBack()) {
            mSpringScroller.forceStop();
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            mSpringScroller.scrollByFling(scrollX, 0.0f, scrollY, 0.0f, f2, i2, false);
            if (scrollX == 0 && scrollY == 0 && f2 == 0.0f) {
                dispatchScrollState(0);
            } else {
                dispatchScrollState(2);
            }
            if (z2) {
                postInvalidateOnAnimation();
            }
        }
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public boolean dispatchNestedPreScroll(int i2, int i3, int[] iArr, int[] iArr2) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(i2, i3, iArr, iArr2);
    }

    @Override // androidx.core.view.NestedScrollingChild2
    public boolean dispatchNestedScroll(int i2, int i3, int i4, int i5, int[] iArr, int i6) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(i2, i3, i4, i5, iArr, i6);
    }

    @Override // android.view.View, androidx.core.view.NestedScrollingChild
    public boolean startNestedScroll(int i2) {
        return mNestedScrollingChildHelper.startNestedScroll(i2);
    }

    @Override // androidx.core.view.NestedScrollingChild2
    public void stopNestedScroll(int i2) {
        mNestedScrollingChildHelper.stopNestedScroll(i2);
    }

    @Override
    // android.view.ViewGroup, android.view.ViewParent, androidx.core.view.NestedScrollingParent
    public boolean onStartNestedScroll(View view, View view2, int i2) {
        return isEnabled();
    }

    private void onNestedPreScroll(int i2, int[] iArr, int i3) {
        boolean z2 = mNestedScrollAxes == 2;
        int i4 = z2 ? 2 : 1;
        int abs = Math.abs(z2 ? getScrollY() : getScrollX());
        float f2 = 0.0f;
        if (i3 == 0) {
            if (i2 > 0) {
                float f3 = mTotalScrollTopUnconsumed;
                if (f3 > 0.0f) {
                    if ((float) i2 > f3) {
                        consumeDelta((int) f3, iArr, i4);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        mTotalScrollTopUnconsumed = f3 - (float) i2;
                        consumeDelta(i2, iArr, i4);
                    }
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i4), i4);
                    return;
                }
            }
            if (i2 < 0) {
                float f5 = mTotalScrollBottomUnconsumed;
                if ((-f5) < 0.0f) {
                    if ((float) i2 < (-f5)) {
                        consumeDelta((int) f5, iArr, i4);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        mTotalScrollBottomUnconsumed = f5 + (float) i2;
                        consumeDelta(i2, iArr, i4);
                    }
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i4), i4);
                    return;
                }
                return;
            }
            return;
        }
        float f7 = i4 == 2 ? mVelocityY : mVelocityX;
        if (i2 > 0) {
            float f8 = mTotalScrollTopUnconsumed;
            if (f8 > 0.0f) {
                if (f7 > 2000.0f) {
                    float obtainSpringBackDistance = obtainSpringBackDistance(f8, i4);
                    if ((float) i2 > obtainSpringBackDistance) {
                        consumeDelta((int) obtainSpringBackDistance, iArr, i4);
                        mTotalScrollTopUnconsumed = 0.0f;
                    } else {
                        consumeDelta(i2, iArr, i4);
                        f2 = obtainSpringBackDistance - (float) i2;
                        mTotalScrollTopUnconsumed = obtainTouchDistance(f2, Math.signum(f2) * Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                    }
                    moveTarget(f2, i4);
                    dispatchScrollState(1);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(f7, i4, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollTopUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                } else {
                    mTotalScrollTopUnconsumed = 0.0f;
                }
                consumeDelta(i2, iArr, i4);
                return;
            }
        }
        if (i2 < 0) {
            float f10 = mTotalScrollBottomUnconsumed;
            if ((-f10) < 0.0f) {
                if (f7 < -2000.0f) {
                    float obtainSpringBackDistance2 = obtainSpringBackDistance(f10, i4);
                    if ((float) i2 < (-obtainSpringBackDistance2)) {
                        consumeDelta((int) obtainSpringBackDistance2, iArr, i4);
                        mTotalScrollBottomUnconsumed = 0.0f;
                    } else {
                        consumeDelta(i2, iArr, i4);
                        f2 = obtainSpringBackDistance2 + (float) i2;
                        mTotalScrollBottomUnconsumed = obtainTouchDistance(f2, Math.signum(f2) * Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                    }
                    dispatchScrollState(1);
                    moveTarget(-f2, i4);
                    return;
                }
                if (!mScrollByFling) {
                    mScrollByFling = true;
                    springBack(f7, i4, false);
                }
                if (mSpringScroller.computeScrollOffset()) {
                    scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(abs, Math.abs(obtainMaxSpringBackDistance(i4)), i4);
                } else {
                    mTotalScrollBottomUnconsumed = 0.0f;
                }
                consumeDelta(i2, iArr, i4);
                return;
            }
        }
        if (i2 != 0) {
            if ((mTotalScrollBottomUnconsumed == 0.0f || mTotalScrollTopUnconsumed == 0.0f) && mScrollByFling && getScrollY() == 0) {
                consumeDelta(i2, iArr, i4);
            }
        }
    }

    @Override
    // android.view.ViewGroup, android.view.ViewParent, androidx.core.view.NestedScrollingParent
    public void onNestedScrollAccepted(View view, View view2, int i2) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(view, view2, i2);
        startNestedScroll(i2 & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override // androidx.core.view.NestedScrollingParent2
    public void onNestedScroll(View view, int i2, int i3, int i4, int i5, int i6) {
        onNestedScroll(view, i2, i3, i4, i5, i6, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    // android.view.ViewGroup, android.view.ViewParent, androidx.core.view.NestedScrollingParent
    public void onNestedScroll(View view, int i2, int i3, int i4, int i5) {
        onNestedScroll(view, i2, i3, i4, i5, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    public interface OnSpringListener {
        boolean onSpringBack();
    }
}