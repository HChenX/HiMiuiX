package com.hchen.himiuix.springback;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.R;

import java.util.ArrayList;
import java.util.List;

public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3, NestedCurrentFling {
    private static final String TAG = "SpringBackLayout";
    public static final int ANGLE = 4;
    public static final int HORIZONTAL = 1;
    private static final int INVALID_ID = -1;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    public static final int SPRING_BACK_BOTTOM = 2;
    public static final int SPRING_BACK_TOP = 1;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SETTLING = 2;
    public static final int UNCHECK_ORIENTATION = 0;
    private static final int VELOCITY_THRADHOLD = 2000;
    public static final int VERTICAL = 2;
    private int consumeNestFlingCounter;
    private int mActivePointerId;
    private final SpringBackLayoutHelper mHelper;
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
    private final List<OnScrollListener> mOnScrollListeners;
    private OnSpringListener mOnSpringListener;
    private int mOriginScrollOrientation;
    private final int[] mParentOffsetInWindow;
    private final int[] mParentScrollConsumed;
    protected final int mScreenHeight;
    protected final int mScreenWith;
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

    /** @noinspection deprecation*/
    public SpringBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        consumeNestFlingCounter = 0;
        mActivePointerId = -1;
        mNestedScrollingV2ConsumedCompat = new int[2];
        mOnScrollListeners = new ArrayList<>();
        mParentOffsetInWindow = new int[2];
        mParentScrollConsumed = new int[2];
        mScrollState = 0;
        mSpringBackEnable = true;
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new androidx.core.view.NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpringBackLayout);
        mTargetId = a.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        mOriginScrollOrientation = a.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
        mSpringBackMode = a.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        a.recycle();

        mSpringScroller = new SpringScroller();
        mHelper = new SpringBackLayoutHelper(this, mOriginScrollOrientation);
        setNestedScrollingEnabled(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWith = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    public void setSpringBackEnable(boolean springBackEnable) {
        mSpringBackEnable = springBackEnable;
    }

    public boolean isSpringBackEnable() {
        return mSpringBackEnable;
    }

    public void setScrollOrientation(int orientation) {
        mOriginScrollOrientation = orientation;
        mHelper.mTargetScrollOrientation = orientation;
    }

    public void setSpringBackMode(int mode) {
        mSpringBackMode = mode;
    }

    public int getSpringBackMode() {
        return mSpringBackMode;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mTarget == null) return;
        if (mTarget instanceof NestedScrollingChild3 || enabled == mTarget.isNestedScrollingEnabled()) {
            mTarget.setNestedScrollingEnabled(enabled);
        }
    }

    private boolean isSupportTopSpringBackMode() {
        return (mSpringBackMode & 1) != 0;
    }

    private boolean isSupportBottomSpringBackMode() {
        return (mSpringBackMode & 2) != 0;
    }

    public void setTarget(@NonNull View view) {
        mTarget = view;
        if (mTarget instanceof NestedScrollingChild3 && mTarget.isNestedScrollingEnabled()) {
            mTarget.setNestedScrollingEnabled(true);
        }

    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (mTargetId == -1) {
                throw new IllegalArgumentException("invalid target Id");
            }

            mTarget = findViewById(mTargetId);
        }

        if (mTarget == null) {
            throw new IllegalArgumentException("fail to get target");
        } else {
            if (isEnabled() && mTarget instanceof NestedScrollingChild3 && !mTarget.isNestedScrollingEnabled()) {
                mTarget.setNestedScrollingEnabled(true);
            }

            if (mTarget.getOverScrollMode() != View.OVER_SCROLL_NEVER) {
                mTarget.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = measuredWidth - paddingLeft - getPaddingRight() + paddingLeft;
        int paddingBottom = measuredHeight - paddingTop - getPaddingBottom() + paddingTop;
        mTarget.layout(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureTarget();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);
        if (widthSize > mTarget.getMeasuredWidth()) {
            widthSize = mTarget.getMeasuredWidth();
        }

        if (heightSize > mTarget.getMeasuredHeight()) {
            heightSize = mTarget.getMeasuredHeight();
        }

        if (widthMode != MeasureSpec.UNSPECIFIED) {
            widthSize = mTarget.getMeasuredWidth();
        }

        if (heightMode != MeasureSpec.UNSPECIFIED) {
            heightSize = mTarget.getMeasuredHeight();
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    public void computeScroll() {
        super.computeScroll();
        if (mSpringScroller.computeScrollOffset()) {
            scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
            if (!mSpringScroller.isFinished()) {
                postInvalidateOnAnimation();
            } else if (getScrollX() == 0 && getScrollY() == 0) {
                dispatchScrollState(0);
            } else if (mScrollState != 2) {
                Log.d("SpringBackLayout", "Scroll stop but state is not correct.");
                springBack(mNestedScrollAxes == 2 ? 2 : 1);
            }
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        for (OnScrollListener onScrollListener : mOnScrollListeners) {
            onScrollListener.onScrolled(this, l - oldl, t - oldt);
        }
    }

    private boolean isVerticalTargetScrollToTop() {
        return mTarget instanceof ListView ? !((ListView) mTarget).canScrollList(-1)
                : !mTarget.canScrollVertically(-1);
    }

    private boolean isHorizontalTargetScrollToTop() {
        return !mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollOrientation(int orientation) {
        return mScrollOrientation == orientation;
    }

    private boolean isTargetScrollToTop(int orientation) {
        return orientation == 2 ? (mTarget instanceof ListView ? !((ListView) mTarget).canScrollList(-1)
                : !mTarget.canScrollVertically(-1)) : !mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollToBottom(int orientation) {
        return orientation == 2 ? (mTarget instanceof ListView ? !((ListView) mTarget).canScrollList(1)
                : !mTarget.canScrollVertically(1)) : !mTarget.canScrollHorizontally(1);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && mScrollState == 2 && mHelper.isTouchInTarget(ev)) {
            dispatchScrollState(1);
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_UP && mScrollState != 2) {
            dispatchScrollState(0);
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSpringBackEnable || !isEnabled() || mNestedFlingInProgress || mNestedScrollInProgress && mTarget.isNestedScrollingEnabled()) {
            return false;
        } else {
            if (!mSpringScroller.isFinished() && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mSpringScroller.forceStop();
            }

            if (!isSupportTopSpringBackMode() && !isSupportBottomSpringBackMode()) {
                return false;
            } else {
                if ((mOriginScrollOrientation & 4) != 0) {
                    checkOrientation(ev);
                    if (isTargetScrollOrientation(2) && (mOriginScrollOrientation & 1) != 0 && (float) getScrollX() == 0.0F) {
                        return false;
                    }

                    if (isTargetScrollOrientation(1) && (mOriginScrollOrientation & 2) != 0 && (float) getScrollY() == 0.0F) {
                        return false;
                    }

                    if (isTargetScrollOrientation(2) || isTargetScrollOrientation(1)) {
                        disallowParentInterceptTouchEvent(true);
                    }
                } else {
                    mScrollOrientation = mOriginScrollOrientation;
                }

                if (isTargetScrollOrientation(2)) {
                    return onVerticalInterceptTouchEvent(ev);
                } else {
                    return isTargetScrollOrientation(1) && onHorizontalInterceptTouchEvent(ev);
                }
            }
        }
    }

    private void disallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void checkOrientation(MotionEvent ev) {
        mHelper.checkOrientation(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = mHelper.mInitialDownY;
                mInitialDownX = mHelper.mInitialDownX;
                mActivePointerId = mHelper.mActivePointerId;
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
                break;
            case MotionEvent.ACTION_UP:
                disallowParentInterceptTouchEvent(false);
                if ((mOriginScrollOrientation & 2) != 0) {
                    springBack(2);
                } else {
                    springBack(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrollOrientation == 0 && mHelper.mScrollOrientation != 0) {
                    mScrollOrientation = mHelper.mScrollOrientation;
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_POINTER_DOWN:
            default:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
        }

    }

    private boolean onVerticalInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2)) {
            return false;
        } else if (isTargetScrollToTop(2) && !isSupportTopSpringBackMode()) {
            return false;
        } else {
            if (!isTargetScrollToBottom(2) || isSupportBottomSpringBackMode()) {
                int findPointerIndex2;
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivePointerId = ev.getPointerId(0);
                        findPointerIndex2 = ev.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            return false;
                        }

                        mInitialDownY = ev.getY(findPointerIndex2);
                        if (getScrollY() != 0) {
                            mIsBeingDragged = true;
                            mInitialMotionY = mInitialDownY;
                        } else {
                            mIsBeingDragged = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mIsBeingDragged = false;
                        mActivePointerId = -1;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (mActivePointerId == -1) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                            return false;
                        }

                        findPointerIndex2 = ev.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                            return false;
                        }

                        float y = ev.getY(findPointerIndex2);
                        if (isTargetScrollToBottom(2) && isTargetScrollToTop(2)) {
                            z = true;
                        }

                        if (!z && isTargetScrollToTop(2) || z && y > mInitialDownY) {
                            if (y - mInitialDownY > (float) mTouchSlop && !mIsBeingDragged) {
                                mIsBeingDragged = true;
                                dispatchScrollState(1);
                                mInitialMotionY = y;
                            }
                        } else if (mInitialDownY - y > (float) mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionY = y;
                        }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    default:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        onSecondaryPointerUp(ev);
                }
            }
            return false;
        }
    }

    private boolean onHorizontalInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1)) {
            return false;
        } else if (isTargetScrollToTop(1) && !isSupportTopSpringBackMode()) {
            return false;
        } else {
            if (!isTargetScrollToBottom(1) || isSupportBottomSpringBackMode()) {
                int findPointerIndex2;
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivePointerId = ev.getPointerId(0);
                        findPointerIndex2 = ev.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            return false;
                        }

                        mInitialDownX = ev.getX(findPointerIndex2);
                        if (getScrollX() != 0) {
                            mIsBeingDragged = true;
                            mInitialMotionX = mInitialDownX;
                        } else {
                            mIsBeingDragged = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mIsBeingDragged = false;
                        mActivePointerId = -1;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (mActivePointerId == -1) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                            return false;
                        }

                        findPointerIndex2 = ev.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                            return false;
                        }

                        float x = ev.getX(findPointerIndex2);
                        if (isTargetScrollToBottom(1) && isTargetScrollToTop(1)) {
                            z = true;
                        }

                        if (!z && isTargetScrollToTop(1) || z && x > mInitialDownX) {
                            if (x - mInitialDownX > (float) mTouchSlop && !mIsBeingDragged) {
                                mIsBeingDragged = true;
                                dispatchScrollState(1);
                                mInitialMotionX = x;
                            }
                        } else if (mInitialDownX - x > (float) mTouchSlop && !mIsBeingDragged) {
                            mIsBeingDragged = true;
                            dispatchScrollState(1);
                            mInitialMotionX = x;
                        }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    default:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        onSecondaryPointerUp(ev);
                }
            }

            return false;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (!isEnabled() || !mSpringBackEnable) {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    public void internalRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void requestDisallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        parent.requestDisallowInterceptTouchEvent(disallowIntercept);

        for (; parent != null; parent = parent.getParent()) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (isEnabled() && !mNestedFlingInProgress && !mNestedScrollInProgress && (!mTarget.isNestedScrollingEnabled())) {
            if (!mSpringScroller.isFinished() && actionMasked == 0) {
                mSpringScroller.forceStop();
            }

            if (isTargetScrollOrientation(2)) {
                return onVerticalTouchEvent(event);
            } else {
                return isTargetScrollOrientation(1) && onHorizontalTouchEvent(event);
            }
        } else {
            return false;
        }
    }

    private boolean onHorizontalTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (!isTargetScrollToTop(1) && !isTargetScrollToBottom(1)) {
            return onScrollEvent(event, actionMasked, 1);
        } else {
            return isTargetScrollToBottom(1) ? onScrollUpEvent(event, actionMasked, 1) : onScrollDownEvent(event, actionMasked, 1);
        }
    }

    private boolean onVerticalTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (!isTargetScrollToTop(2) && !isTargetScrollToBottom(2)) {
            return onScrollEvent(event, actionMasked, 2);
        } else {
            return isTargetScrollToBottom(2) ? onScrollUpEvent(event, actionMasked, 2) : onScrollDownEvent(event, actionMasked, 2);
        }
    }

    private boolean onScrollEvent(MotionEvent event, int actionMasked, int orientation) {
        int findPointerIndex = event.findPointerIndex(mActivePointerId);
        float y2;
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                checkScrollStart(orientation);
                break;
            case MotionEvent.ACTION_UP:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(orientation);
                }

                mActivePointerId = -1;
                return false;
            case MotionEvent.ACTION_MOVE:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    boolean isVertical = orientation == 2;
                    y2 = isVertical ? event.getY(findPointerIndex) : event.getX(findPointerIndex);
                    float signum = Math.signum(y2 - (isVertical ? mInitialMotionY : mInitialMotionX));
                    float obtainSpringBackDistance = obtainSpringBackDistance(y2 - (isVertical ? mInitialMotionY : mInitialMotionX), orientation);
                    requestDisallowParentInterceptTouchEvent(true);
                    moveTarget(signum * obtainSpringBackDistance, orientation);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_OUTSIDE:
            default:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                    return false;
                }

                int actionIndex;
                if (orientation == 2) {
                    y2 = event.getY(findPointerIndex) - mInitialDownY;
                    actionIndex = event.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }

                    mInitialDownY = event.getY(actionIndex) - y2;
                    mInitialMotionY = mInitialDownY;
                } else {
                    y2 = event.getX(findPointerIndex) - mInitialDownX;
                    actionIndex = event.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }

                    mInitialDownX = event.getX(actionIndex) - y2;
                    mInitialMotionX = mInitialDownX;
                }

                mActivePointerId = event.getPointerId(actionIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
        }

        return true;
    }

    private void checkVerticalScrollStart(int orientation) {
        if (getScrollY() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance((float) Math.abs(getScrollY()), Math.abs(obtainMaxSpringBackDistance(orientation)), 2);
            if (getScrollY() < 0) {
                mInitialDownY -= obtainTouchDistance;
            } else {
                mInitialDownY += obtainTouchDistance;
            }

            mInitialMotionY = mInitialDownY;
        } else {
            mIsBeingDragged = false;
        }
    }

    private void checkScrollStart(int orientation) {
        if (orientation == 2) {
            checkVerticalScrollStart(orientation);
        } else {
            checkHorizontalScrollStart(orientation);
        }

    }

    private void checkHorizontalScrollStart(int orientation) {
        if (getScrollX() != 0) {
            mIsBeingDragged = true;
            float obtainTouchDistance = obtainTouchDistance((float) Math.abs(getScrollX()), Math.abs(obtainMaxSpringBackDistance(orientation)), 2);
            if (getScrollX() < 0) {
                mInitialDownX -= obtainTouchDistance;
            } else {
                mInitialDownX += obtainTouchDistance;
            }

            mInitialMotionX = mInitialDownX;
        } else {
            mIsBeingDragged = false;
        }

    }

    private boolean onScrollDownEvent(MotionEvent motionEvent, int i, int i2) {
        if (i == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i2);
            return true;
        } else {
            if (i != 1) {
                int findPointerIndex2;
                float y2;
                if (i == 2) {
                    findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    if (mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2);
                            signum = Math.signum(y2 - mInitialMotionY);
                            obtainSpringBackDistance = obtainSpringBackDistance(y2 - mInitialMotionY, i2);
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2);
                            signum = Math.signum(y2 - mInitialMotionX);
                            obtainSpringBackDistance = obtainSpringBackDistance(y2 - mInitialMotionX, i2);
                        }

                        y2 = signum * obtainSpringBackDistance;
                        if (!(y2 > 0.0F)) {
                            moveTarget(0.0F, i2);
                            return false;
                        }

                        requestDisallowParentInterceptTouchEvent(true);
                        moveTarget(y2, i2);
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            mInitialDownY = motionEvent.getY(actionIndex) - y2;
                            mInitialMotionY = mInitialDownY;
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            mInitialDownX = motionEvent.getX(actionIndex) - y2;
                            mInitialMotionX = mInitialDownX;
                        }

                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }

            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
            } else {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(i2);
                }

                mActivePointerId = -1;
            }
            return false;
        }
    }

    private void moveTarget(float f, int i) {
        if (i == 2) {
            scrollTo(0, (int) (-f));
        } else {
            scrollTo((int) (-f), 0);
        }
    }

    private void springBack(int i) {
        springBack(0.0F, i, true);
    }

    private void springBack(float f, int i, boolean z) {
        OnSpringListener onSpringListener = mOnSpringListener;
        if (onSpringListener == null || !onSpringListener.onSpringBack()) {
            mSpringScroller.forceStop();
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            mSpringScroller.scrollByFling((float) scrollX, 0.0F, (float) scrollY, 0.0F, f, i, false);
            if (scrollX == 0 && scrollY == 0 && f == 0.0F) {
                dispatchScrollState(0);
            } else {
                dispatchScrollState(2);
            }

            if (z) {
                postInvalidateOnAnimation();
            }
        }
    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int i, int i2) {
        if (i == 0) {
            mActivePointerId = motionEvent.getPointerId(0);
            checkScrollStart(i2);
            return true;
        } else {
            if (i != 1) {
                int findPointerIndex2;
                float y2;
                if (i == 2) {
                    findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    if (mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2);
                            signum = Math.signum(mInitialMotionY - y2);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionY - y2, i2);
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2);
                            signum = Math.signum(mInitialMotionX - y2);
                            obtainSpringBackDistance = obtainSpringBackDistance(mInitialMotionX - y2, i2);
                        }

                        y2 = signum * obtainSpringBackDistance;
                        if (!(y2 > 0.0F)) {
                            moveTarget(0.0F, i2);
                            return false;
                        }

                        requestDisallowParentInterceptTouchEvent(true);
                        moveTarget(-y2, i2);
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        findPointerIndex2 = motionEvent.findPointerIndex(mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2) - mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            mInitialDownY = motionEvent.getY(actionIndex) - y2;
                            mInitialMotionY = mInitialDownY;
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2) - mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            mInitialDownX = motionEvent.getX(actionIndex) - y2;
                            mInitialMotionX = mInitialDownX;
                        }

                        mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }

            if (motionEvent.findPointerIndex(mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
            } else {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(i2);
                }

                mActivePointerId = -1;
            }
            return false;
        }
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == mActivePointerId) {
            mActivePointerId = motionEvent.getPointerId(actionIndex == 0 ? 1 : 0);
        }
    }

    protected int getSpringBackRange(int i) {
        return i == 2 ? mScreenHeight : mScreenWith;
    }

    protected float obtainSpringBackDistance(float f, int i) {
        return obtainDampingDistance(Math.min(Math.abs(f) / (float) getSpringBackRange(i), 1.0F), i);
    }

    protected float obtainMaxSpringBackDistance(int i) {
        return obtainDampingDistance(1.0F, i);
    }

    protected float obtainDampingDistance(float f, int i) {
        int springBackRange = getSpringBackRange(i);
        double min = Math.min(f, 1.0F);
        return (float) (Math.pow(min, 3.0) / 3.0 - Math.pow(min, 2.0) + min) * (float) springBackRange;
    }

    protected float obtainTouchDistance(float f, float f2, int i) {
        int springBackRange = getSpringBackRange(i);
        if (Math.abs(f) >= Math.abs(f2)) {
            f = f2;
        }

        return (float) ((double) springBackRange - Math.pow(springBackRange, 0.6666666666666666)
                * Math.pow(((float) springBackRange - f * 3.0F), 0.3333333333333333));
    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        boolean z = mNestedScrollAxes == 2;
        int i6 = z ? dyConsumed : dxConsumed;
        int i7 = z ? consumed[1] : consumed[0];
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type, consumed);
        if (mSpringBackEnable) {
            int i8 = (z ? consumed[1] : consumed[0]) - i7;
            int i9 = z ? dyUnconsumed - i8 : dxUnconsumed - i8;
            int i11 = z ? 2 : 1;
            float obtainMaxSpringBackDistance2;
            float f2;
            if (i9 < 0 && isTargetScrollToTop(i11) && isSupportTopSpringBackMode()) {
                if (type != 0) {
                    obtainMaxSpringBackDistance2 = obtainMaxSpringBackDistance(i11);
                    if (mVelocityY == 0.0F && mVelocityX == 0.0F) {
                        if (mTotalScrollTopUnconsumed == 0.0F) {
                            f2 = obtainMaxSpringBackDistance2 - mTotalFlingUnconsumed;
                            if (consumeNestFlingCounter < 4) {
                                if (f2 <= (float) Math.abs(i9)) {
                                    mTotalFlingUnconsumed += f2;
                                    consumed[1] = (int) ((float) consumed[1] + f2);
                                } else {
                                    mTotalFlingUnconsumed += (float) Math.abs(i9);
                                    consumed[1] += i9;
                                }

                                dispatchScrollState(2);
                                moveTarget(obtainSpringBackDistance(mTotalFlingUnconsumed, i11), i11);
                                ++consumeNestFlingCounter;
                            }
                        }
                    } else {
                        mScrollByFling = true;
                        if (i6 != 0 && (float) (-i9) <= obtainMaxSpringBackDistance2) {
                            mSpringScroller.setFirstStep(i9);
                        }

                        dispatchScrollState(2);
                    }
                } else if (mSpringScroller.isFinished()) {
                    mTotalScrollTopUnconsumed += (float) Math.abs(i9);
                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i11), i11);
                    consumed[1] += i9;
                }
            } else if (i9 > 0 && isTargetScrollToBottom(i11) && isSupportBottomSpringBackMode()) {
                if (type != 0) {
                    obtainMaxSpringBackDistance2 = obtainMaxSpringBackDistance(i11);
                    if (mVelocityY == 0.0F && mVelocityX == 0.0F) {
                        if (mTotalScrollBottomUnconsumed == 0.0F) {
                            f2 = obtainMaxSpringBackDistance2 - mTotalFlingUnconsumed;
                            if (consumeNestFlingCounter < 4) {
                                if (f2 <= (float) Math.abs(i9)) {
                                    mTotalFlingUnconsumed += f2;
                                    consumed[1] = (int) ((float) consumed[1] + f2);
                                } else {
                                    mTotalFlingUnconsumed += (float) Math.abs(i9);
                                    consumed[1] += i9;
                                }

                                dispatchScrollState(2);
                                moveTarget(-obtainSpringBackDistance(mTotalFlingUnconsumed, i11), i11);
                                ++consumeNestFlingCounter;
                            }
                        }
                    } else {
                        mScrollByFling = true;
                        if (i6 != 0 && (float) i9 <= obtainMaxSpringBackDistance2) {
                            mSpringScroller.setFirstStep(i9);
                        }

                        dispatchScrollState(2);
                    }
                } else if (mSpringScroller.isFinished()) {
                    mTotalScrollBottomUnconsumed += (float) Math.abs(i9);
                    dispatchScrollState(1);
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i11), i11);
                    consumed[1] += i9;
                }
            }
        }
    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if (mSpringBackEnable) {
            mNestedScrollAxes = axes;
            boolean z = mNestedScrollAxes == 2;
            if (((z ? 2 : 1) & mOriginScrollOrientation) == 0 || !onStartNestedScroll(child, child, axes)) {
                return false;
            }

            float scrollY = z ? (float) getScrollY() : (float) getScrollX();
            if (type != 0 && scrollY != 0.0F && mTarget instanceof NestedScrollView) {
                return false;
            }
        }

        mNestedScrollingChildHelper.startNestedScroll(axes, type);

        return true;
    }

    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled();
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (mSpringBackEnable) {
            boolean z = mNestedScrollAxes == 2;
            int i3 = z ? 2 : 1;
            float scrollY = z ? (float) getScrollY() : (float) getScrollX();
            if (type != 0) {
                if (scrollY == 0.0F) {
                    mTotalFlingUnconsumed = 0.0F;
                } else {
                    mTotalFlingUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }

                mNestedFlingInProgress = true;
                consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0F) {
                    mTotalScrollTopUnconsumed = 0.0F;
                    mTotalScrollBottomUnconsumed = 0.0F;
                } else if (scrollY < 0.0F) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    mTotalScrollBottomUnconsumed = 0.0F;
                } else {
                    mTotalScrollTopUnconsumed = 0.0F;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scrollY), Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                }

                mNestedScrollInProgress = true;
            }

            mVelocityY = 0.0F;
            mVelocityX = 0.0F;
            mScrollByFling = false;
            mSpringScroller.forceStop();
        }

        onNestedScrollAccepted(child, target, axes);
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (mSpringBackEnable) {
            if (mNestedScrollAxes == 2) {
                onNestedPreScroll(dy, consumed, type);
            } else {
                onNestedPreScroll(dx, consumed, type);
            }
        }

        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], mParentScrollConsumed, null, type)) {
            consumed[0] += mParentScrollConsumed[0];
            consumed[1] += mParentScrollConsumed[1];
        }
    }

    private void onNestedPreScroll(int i, @NonNull int[] consumed, int type) {
        boolean z = mNestedScrollAxes == 2;
        int i3 = z ? 2 : 1;
        int abs = Math.abs(z ? getScrollY() : getScrollX());
        float f = 0.0F;
        float f6;
        if (type == 0) {
            if (i > 0) {
                if (mTotalScrollTopUnconsumed > 0.0F) {
                    f6 = (float) i;
                    if (f6 > mTotalScrollTopUnconsumed) {
                        consumeDelta((int) mTotalScrollTopUnconsumed, consumed, i3);
                        mTotalScrollTopUnconsumed = 0.0F;
                    } else {
                        mTotalScrollTopUnconsumed -= f6;
                        consumeDelta(i, consumed, i3);
                    }

                    dispatchScrollState(1);
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, i3), i3);
                }
            } else if (i < 0 && -mTotalScrollBottomUnconsumed < 0.0F) {
                f6 = (float) i;
                if (f6 < -mTotalScrollBottomUnconsumed) {
                    consumeDelta((int) mTotalScrollBottomUnconsumed, consumed, i3);
                    mTotalScrollBottomUnconsumed = 0.0F;
                } else {
                    mTotalScrollBottomUnconsumed += f6;
                    consumeDelta(i, consumed, i3);
                }

                dispatchScrollState(1);
                moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, i3), i3);
            }
        } else {
            f6 = i3 == 2 ? mVelocityY : mVelocityX;
            float obtainSpringBackDistance;
            float obtainSpringBackDistance2;
            if (i > 0) {
                if (mTotalScrollTopUnconsumed > 0.0F) {
                    if (f6 > 2000.0F) {
                        obtainSpringBackDistance = obtainSpringBackDistance(mTotalScrollTopUnconsumed, i3);
                        obtainSpringBackDistance2 = (float) i;
                        if (obtainSpringBackDistance2 > obtainSpringBackDistance) {
                            consumeDelta((int) obtainSpringBackDistance, consumed, i3);
                            mTotalScrollTopUnconsumed = 0.0F;
                        } else {
                            consumeDelta(i, consumed, i3);
                            f = obtainSpringBackDistance - obtainSpringBackDistance2;
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
                        mTotalScrollTopUnconsumed = obtainTouchDistance((float) abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    } else {
                        mTotalScrollTopUnconsumed = 0.0F;
                    }

                    consumeDelta(i, consumed, i3);
                    return;
                }
            } else if (i < 0) {
                obtainSpringBackDistance = mTotalScrollBottomUnconsumed;
                if (-obtainSpringBackDistance < 0.0F) {
                    if (f6 < -2000.0F) {
                        obtainSpringBackDistance2 = obtainSpringBackDistance(obtainSpringBackDistance, i3);
                        float f10 = (float) i;
                        if (f10 < -obtainSpringBackDistance2) {
                            consumeDelta((int) obtainSpringBackDistance2, consumed, i3);
                            mTotalScrollBottomUnconsumed = 0.0F;
                        } else {
                            consumeDelta(i, consumed, i3);
                            f = obtainSpringBackDistance2 + f10;
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
                        mTotalScrollBottomUnconsumed = obtainTouchDistance((float) abs, Math.abs(obtainMaxSpringBackDistance(i3)), i3);
                    } else {
                        mTotalScrollBottomUnconsumed = 0.0F;
                    }

                    consumeDelta(i, consumed, i3);
                    return;
                }
            }

            if (i != 0 && (mTotalScrollBottomUnconsumed == 0.0F || mTotalScrollTopUnconsumed == 0.0F) && mScrollByFling && getScrollY() == 0) {
                consumeDelta(i, consumed, i3);
            }
        }
    }

    private void consumeDelta(int i, @NonNull int[] consumed, int type) {
        if (type == 2) {
            consumed[1] = i;
        } else {
            consumed[0] = i;
        }
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public void onStopNestedScroll(@NonNull View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
        if (mSpringBackEnable) {
            boolean z = mNestedScrollAxes == 2;
            int i2 = z ? 2 : 1;
            if (mNestedScrollInProgress) {
                mNestedScrollInProgress = false;
                float scrollY = z ? (float) getScrollY() : (float) getScrollX();
                if (!mNestedFlingInProgress && scrollY != 0.0F) {
                    springBack(i2);
                } else if (scrollY != 0.0F) {
                    stopNestedFlingScroll(i2);
                }
            } else if (mNestedFlingInProgress) {
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

            postInvalidateOnAnimation();
        } else {
            springBack(i);
        }
    }

    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    public boolean startNestedScroll(int axes, int type) {
        return mNestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll(int type) {
        mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    public boolean hasNestedScrollingParent(int type) {
        return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public void smoothScrollTo(int i, int i2) {
        if (i - getScrollX() != 0 && i2 - getScrollY() != 0) {
            mSpringScroller.forceStop();
            mSpringScroller.scrollByFling((float) getScrollX(), (float) i, (float) getScrollY(), (float) i2, 0.0F, 2, true);
            dispatchScrollState(2);
            postInvalidateOnAnimation();
        }
    }

    private void dispatchScrollState(int state) {
        if (mScrollState != state) {
            mScrollState = state;

            for (OnScrollListener onScrollListener : mOnScrollListeners) {
                onScrollListener.onStateChanged(mScrollState, state, mSpringScroller.isFinished());
            }
        }
    }

    public void addOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListeners.add(onScrollListener);
    }

    public void removeOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListeners.remove(onScrollListener);
    }

    public void setOnSpringListener(OnSpringListener onSpringListener) {
        mOnSpringListener = onSpringListener;
    }

    public boolean hasSpringListener() {
        return mOnSpringListener != null;
    }

    public boolean onNestedCurrentFling(float velocityX, float velocityY) {
        mVelocityX = velocityX;
        mVelocityY = velocityY;
        return true;
    }

    public interface OnScrollListener {
        void onScrolled(SpringBackLayout var1, int var2, int var3);

        void onStateChanged(int var1, int var2, boolean var3);
    }

    public interface OnSpringListener {
        boolean onSpringBack();
    }
}
