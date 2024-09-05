package com.hchen.himiuix.springback;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
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
import androidx.core.widget.ListViewCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.R;

import java.util.ArrayList;
import java.util.Iterator;
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
    private SpringBackLayoutHelper mHelper;
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
    private List<OnScrollListener> mOnScrollListeners;
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
    private SpringScroller mSpringScroller;
    private View mTarget;
    private int mTargetId;
    private float mTotalFlingUnconsumed;
    private float mTotalScrollBottomUnconsumed;
    private float mTotalScrollTopUnconsumed;
    private int mTouchSlop;
    private float mVelocityX;
    private float mVelocityY;

    public SpringBackLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public SpringBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.consumeNestFlingCounter = 0;
        this.mActivePointerId = -1;
        this.mNestedScrollingV2ConsumedCompat = new int[2];
        this.mOnScrollListeners = new ArrayList();
        this.mParentOffsetInWindow = new int[2];
        this.mParentScrollConsumed = new int[2];
        this.mScrollState = 0;
        this.mSpringBackEnable = true;
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        this.mNestedScrollingChildHelper = new androidx.core.view.NestedScrollingChildHelper(this);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpringBackLayout);
        this.mTargetId = a.getResourceId(R.styleable.SpringBackLayout_scrollableView, -1);
        this.mOriginScrollOrientation = a.getInt(R.styleable.SpringBackLayout_scrollOrientation, 2);
        this.mSpringBackMode = a.getInt(R.styleable.SpringBackLayout_springBackMode, 3);
        a.recycle();
        this.mSpringScroller = new SpringScroller();
        this.mHelper = new SpringBackLayoutHelper(this, this.mOriginScrollOrientation);
        this.setNestedScrollingEnabled(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        this.mScreenWith = displayMetrics.widthPixels;
        this.mScreenHeight = displayMetrics.heightPixels;
    }

    public void setSpringBackEnable(boolean springBackEnable) {
        this.mSpringBackEnable = springBackEnable;
    }

    public boolean isSpringBackEnable() {
        return this.mSpringBackEnable;
    }

    public void setScrollOrientation(int orientation) {
        this.mOriginScrollOrientation = orientation;
        this.mHelper.mTargetScrollOrientation = orientation;
    }

    public void setSpringBackMode(int mode) {
        this.mSpringBackMode = mode;
    }

    public int getSpringBackMode() {
        return this.mSpringBackMode;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mTarget != null && this.mTarget instanceof NestedScrollingChild3 || VERSION.SDK_INT < 21 || enabled == this.mTarget.isNestedScrollingEnabled()) {
            this.mTarget.setNestedScrollingEnabled(enabled);
        }

    }

    private boolean isSupportTopSpringBackMode() {
        return (this.mSpringBackMode & 1) != 0;
    }

    private boolean isSupportBottomSpringBackMode() {
        return (this.mSpringBackMode & 2) != 0;
    }

    public void setTarget(@NonNull View view) {
        this.mTarget = view;
        if (VERSION.SDK_INT >= 21 && this.mTarget instanceof NestedScrollingChild3 && this.mTarget.isNestedScrollingEnabled()) {
            this.mTarget.setNestedScrollingEnabled(true);
        }

    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            if (this.mTargetId == -1) {
                throw new IllegalArgumentException("invalid target Id");
            }

            this.mTarget = this.findViewById(this.mTargetId);
        }

        if (this.mTarget == null) {
            throw new IllegalArgumentException("fail to get target");
        } else {
            if (VERSION.SDK_INT >= 21 && this.isEnabled() && this.mTarget instanceof NestedScrollingChild3 && !this.mTarget.isNestedScrollingEnabled()) {
                this.mTarget.setNestedScrollingEnabled(true);
            }

            if (this.mTarget.getOverScrollMode() != 2) {
                this.mTarget.setOverScrollMode(2);
            }

        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measuredWidth = this.getMeasuredWidth();
        int measuredHeight = this.getMeasuredHeight();
        int paddingLeft = this.getPaddingLeft();
        int paddingTop = this.getPaddingTop();
        int paddingRight = measuredWidth - paddingLeft - this.getPaddingRight() + paddingLeft;
        int paddingBottom = measuredHeight - paddingTop - this.getPaddingBottom() + paddingTop;
        this.mTarget.layout(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.ensureTarget();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        this.measureChild(this.mTarget, widthMeasureSpec, heightMeasureSpec);
        if (widthSize > this.mTarget.getMeasuredWidth()) {
            widthSize = this.mTarget.getMeasuredWidth();
        }

        if (heightSize > this.mTarget.getMeasuredHeight()) {
            heightSize = this.mTarget.getMeasuredHeight();
        }

        if (widthMode != 1073741824) {
            widthSize = this.mTarget.getMeasuredWidth();
        }

        if (heightMode != 1073741824) {
            heightSize = this.mTarget.getMeasuredHeight();
        }

        this.setMeasuredDimension(widthSize, heightSize);
    }

    public void computeScroll() {
        super.computeScroll();
        if (this.mSpringScroller.computeScrollOffset()) {
            this.scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
            if (!this.mSpringScroller.isFinished()) {
                this.postInvalidateOnAnimation();
            } else if (this.getScrollX() == 0 && this.getScrollY() == 0) {
                this.dispatchScrollState(0);
            } else if (this.mScrollState != 2) {
                Log.d("SpringBackLayout", "Scroll stop but state is not correct.");
                this.springBack(this.mNestedScrollAxes == 2 ? 2 : 1);
            }
        }

    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Iterator var5 = this.mOnScrollListeners.iterator();

        while (var5.hasNext()) {
            OnScrollListener onScrollListener = (OnScrollListener) var5.next();
            onScrollListener.onScrolled(this, l - oldl, t - oldt);
        }

    }

    private boolean isVerticalTargetScrollToTop() {
        return this.mTarget instanceof ListView ? !ListViewCompat.canScrollList((ListView) this.mTarget, -1) : !this.mTarget.canScrollVertically(-1);
    }

    private boolean isHorizontalTargetScrollToTop() {
        return !this.mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollOrientation(int orientation) {
        return this.mScrollOrientation == orientation;
    }

    private boolean isTargetScrollToTop(int orientation) {
        return orientation == 2 ? (this.mTarget instanceof ListView ? !ListViewCompat.canScrollList((ListView) this.mTarget, -1) : !this.mTarget.canScrollVertically(-1)) : !this.mTarget.canScrollHorizontally(-1);
    }

    private boolean isTargetScrollToBottom(int orientation) {
        return orientation == 2 ? (this.mTarget instanceof ListView ? !ListViewCompat.canScrollList((ListView) this.mTarget, 1) : !this.mTarget.canScrollVertically(1)) : !this.mTarget.canScrollHorizontally(1);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == 0 && this.mScrollState == 2 && this.mHelper.isTouchInTarget(ev)) {
            this.dispatchScrollState(1);
        }

        if (ev.getActionMasked() == 1 && this.mScrollState != 2) {
            this.dispatchScrollState(0);
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!this.mSpringBackEnable || !this.isEnabled() || this.mNestedFlingInProgress || this.mNestedScrollInProgress || VERSION.SDK_INT >= 21 && this.mTarget.isNestedScrollingEnabled()) {
            return false;
        } else {
            if (!this.mSpringScroller.isFinished() && ev.getActionMasked() == 0) {
                this.mSpringScroller.forceStop();
            }

            if (!this.isSupportTopSpringBackMode() && !this.isSupportBottomSpringBackMode()) {
                return false;
            } else {
                if ((this.mOriginScrollOrientation & 4) != 0) {
                    this.checkOrientation(ev);
                    if (this.isTargetScrollOrientation(2) && (this.mOriginScrollOrientation & 1) != 0 && (float) this.getScrollX() == 0.0F) {
                        return false;
                    }

                    if (this.isTargetScrollOrientation(1) && (this.mOriginScrollOrientation & 2) != 0 && (float) this.getScrollY() == 0.0F) {
                        return false;
                    }

                    if (this.isTargetScrollOrientation(2) || this.isTargetScrollOrientation(1)) {
                        this.disallowParentInterceptTouchEvent(true);
                    }
                } else {
                    this.mScrollOrientation = this.mOriginScrollOrientation;
                }

                if (this.isTargetScrollOrientation(2)) {
                    return this.onVerticalInterceptTouchEvent(ev);
                } else {
                    return this.isTargetScrollOrientation(1) ? this.onHorizontalInterceptTouchEvent(ev) : false;
                }
            }
        }
    }

    private void disallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = this.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

    }

    private void checkOrientation(MotionEvent ev) {
        this.mHelper.checkOrientation(ev);
        switch (ev.getActionMasked()) {
            case 0:
                this.mInitialDownY = this.mHelper.mInitialDownY;
                this.mInitialDownX = this.mHelper.mInitialDownX;
                this.mActivePointerId = this.mHelper.mActivePointerId;
                if (this.getScrollY() != 0) {
                    this.mScrollOrientation = 2;
                    this.requestDisallowParentInterceptTouchEvent(true);
                } else if (this.getScrollX() != 0) {
                    this.mScrollOrientation = 1;
                    this.requestDisallowParentInterceptTouchEvent(true);
                } else {
                    this.mScrollOrientation = 0;
                }

                if ((this.mOriginScrollOrientation & 2) != 0) {
                    this.checkScrollStart(2);
                } else {
                    this.checkScrollStart(1);
                }
                break;
            case 1:
                this.disallowParentInterceptTouchEvent(false);
                if ((this.mOriginScrollOrientation & 2) != 0) {
                    this.springBack(2);
                } else {
                    this.springBack(1);
                }
                break;
            case 2:
                if (this.mScrollOrientation == 0 && this.mHelper.mScrollOrientation != 0) {
                    this.mScrollOrientation = this.mHelper.mScrollOrientation;
                }
            case 3:
            case 4:
            case 5:
            default:
                break;
            case 6:
                this.onSecondaryPointerUp(ev);
        }

    }

    private boolean onVerticalInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (!this.isTargetScrollToTop(2) && !this.isTargetScrollToBottom(2)) {
            return false;
        } else if (this.isTargetScrollToTop(2) && !this.isSupportTopSpringBackMode()) {
            return false;
        } else {
            if (!this.isTargetScrollToBottom(2) || this.isSupportBottomSpringBackMode()) {
                int findPointerIndex2;
                switch (ev.getActionMasked()) {
                    case 0:
                        this.mActivePointerId = ev.getPointerId(0);
                        findPointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            return false;
                        }

                        this.mInitialDownY = ev.getY(findPointerIndex2);
                        if (this.getScrollY() != 0) {
                            this.mIsBeingDragged = true;
                            this.mInitialMotionY = this.mInitialDownY;
                        } else {
                            this.mIsBeingDragged = false;
                        }
                        break;
                    case 1:
                        this.mIsBeingDragged = false;
                        this.mActivePointerId = -1;
                        return this.mIsBeingDragged;
                    case 2:
                        if (this.mActivePointerId == -1) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                            return false;
                        }

                        findPointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                            return false;
                        }

                        float y = ev.getY(findPointerIndex2);
                        if (this.isTargetScrollToBottom(2) && this.isTargetScrollToTop(2)) {
                            z = true;
                        }

                        if (!z && this.isTargetScrollToTop(2) || z && y > this.mInitialDownY) {
                            if (y - this.mInitialDownY > (float) this.mTouchSlop && !this.mIsBeingDragged) {
                                this.mIsBeingDragged = true;
                                this.dispatchScrollState(1);
                                this.mInitialMotionY = y;
                            }
                        } else if (this.mInitialDownY - y > (float) this.mTouchSlop && !this.mIsBeingDragged) {
                            this.mIsBeingDragged = true;
                            this.dispatchScrollState(1);
                            this.mInitialMotionY = y;
                        }
                    case 3:
                    case 4:
                    case 5:
                    default:
                        break;
                    case 6:
                        this.onSecondaryPointerUp(ev);
                }
            }

            return false;
        }
    }

    private boolean onHorizontalInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (!this.isTargetScrollToTop(1) && !this.isTargetScrollToBottom(1)) {
            return false;
        } else if (this.isTargetScrollToTop(1) && !this.isSupportTopSpringBackMode()) {
            return false;
        } else {
            if (!this.isTargetScrollToBottom(1) || this.isSupportBottomSpringBackMode()) {
                int findPointerIndex2;
                switch (ev.getActionMasked()) {
                    case 0:
                        this.mActivePointerId = ev.getPointerId(0);
                        findPointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            return false;
                        }

                        this.mInitialDownX = ev.getX(findPointerIndex2);
                        if (this.getScrollX() != 0) {
                            this.mIsBeingDragged = true;
                            this.mInitialMotionX = this.mInitialDownX;
                        } else {
                            this.mIsBeingDragged = false;
                        }
                        break;
                    case 1:
                        this.mIsBeingDragged = false;
                        this.mActivePointerId = -1;
                        return this.mIsBeingDragged;
                    case 2:
                        if (this.mActivePointerId == -1) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but don't have an active pointer id.");
                            return false;
                        }

                        findPointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                            return false;
                        }

                        float x = ev.getX(findPointerIndex2);
                        if (this.isTargetScrollToBottom(1) && this.isTargetScrollToTop(1)) {
                            z = true;
                        }

                        if (!z && this.isTargetScrollToTop(1) || z && x > this.mInitialDownX) {
                            if (x - this.mInitialDownX > (float) this.mTouchSlop && !this.mIsBeingDragged) {
                                this.mIsBeingDragged = true;
                                this.dispatchScrollState(1);
                                this.mInitialMotionX = x;
                            }
                        } else if (this.mInitialDownX - x > (float) this.mTouchSlop && !this.mIsBeingDragged) {
                            this.mIsBeingDragged = true;
                            this.dispatchScrollState(1);
                            this.mInitialMotionX = x;
                        }
                    case 3:
                    case 4:
                    case 5:
                    default:
                        break;
                    case 6:
                        this.onSecondaryPointerUp(ev);
                }
            }

            return false;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (!this.isEnabled() || !this.mSpringBackEnable) {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

    }

    public void internalRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void requestDisallowParentInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = this.getParent();
        parent.requestDisallowInterceptTouchEvent(disallowIntercept);

        for (; parent != null; parent = parent.getParent()) {
            if (parent instanceof SpringBackLayout) {
                ((SpringBackLayout) parent).internalRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (this.isEnabled() && !this.mNestedFlingInProgress && !this.mNestedScrollInProgress && (VERSION.SDK_INT < 21 || !this.mTarget.isNestedScrollingEnabled())) {
            if (!this.mSpringScroller.isFinished() && actionMasked == 0) {
                this.mSpringScroller.forceStop();
            }

            if (this.isTargetScrollOrientation(2)) {
                return this.onVerticalTouchEvent(event);
            } else {
                return this.isTargetScrollOrientation(1) ? this.onHorizontalTouchEvent(event) : false;
            }
        } else {
            return false;
        }
    }

    private boolean onHorizontalTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (!this.isTargetScrollToTop(1) && !this.isTargetScrollToBottom(1)) {
            return this.onScrollEvent(event, actionMasked, 1);
        } else {
            return this.isTargetScrollToBottom(1) ? this.onScrollUpEvent(event, actionMasked, 1) : this.onScrollDownEvent(event, actionMasked, 1);
        }
    }

    private boolean onVerticalTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (!this.isTargetScrollToTop(2) && !this.isTargetScrollToBottom(2)) {
            return this.onScrollEvent(event, actionMasked, 2);
        } else {
            return this.isTargetScrollToBottom(2) ? this.onScrollUpEvent(event, actionMasked, 2) : this.onScrollDownEvent(event, actionMasked, 2);
        }
    }

    private boolean onScrollEvent(MotionEvent event, int actionMasked, int orientation) {
        int findPointerIndex = event.findPointerIndex(this.mActivePointerId);
        float y2;
        switch (actionMasked) {
            case 0:
                this.mActivePointerId = event.getPointerId(0);
                this.checkScrollStart(orientation);
                break;
            case 1:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (this.mIsBeingDragged) {
                    this.mIsBeingDragged = false;
                    this.springBack(orientation);
                }

                this.mActivePointerId = -1;
                return false;
            case 2:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                if (this.mIsBeingDragged) {
                    boolean isVertical = orientation == 2;
                    y2 = isVertical ? event.getY(findPointerIndex) : event.getX(findPointerIndex);
                    float signum = Math.signum(y2 - (isVertical ? this.mInitialMotionY : this.mInitialMotionX));
                    float obtainSpringBackDistance = this.obtainSpringBackDistance(y2 - (isVertical ? this.mInitialMotionY : this.mInitialMotionX), orientation);
                    this.requestDisallowParentInterceptTouchEvent(true);
                    this.moveTarget(signum * obtainSpringBackDistance, orientation);
                }
                break;
            case 3:
                return false;
            case 4:
            default:
                break;
            case 5:
                if (findPointerIndex < 0) {
                    Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                    return false;
                }

                int actionIndex;
                if (orientation == 2) {
                    y2 = event.getY(findPointerIndex) - this.mInitialDownY;
                    actionIndex = event.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }

                    this.mInitialDownY = event.getY(actionIndex) - y2;
                    this.mInitialMotionY = this.mInitialDownY;
                } else {
                    y2 = event.getX(findPointerIndex) - this.mInitialDownX;
                    actionIndex = event.getActionIndex();
                    if (actionIndex < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                        return false;
                    }

                    this.mInitialDownX = event.getX(actionIndex) - y2;
                    this.mInitialMotionX = this.mInitialDownX;
                }

                this.mActivePointerId = event.getPointerId(actionIndex);
                break;
            case 6:
                this.onSecondaryPointerUp(event);
        }

        return true;
    }

    private void checkVerticalScrollStart(int orientation) {
        if (this.getScrollY() != 0) {
            this.mIsBeingDragged = true;
            float obtainTouchDistance = this.obtainTouchDistance((float) Math.abs(this.getScrollY()), Math.abs(this.obtainMaxSpringBackDistance(orientation)), 2);
            if (this.getScrollY() < 0) {
                this.mInitialDownY -= obtainTouchDistance;
            } else {
                this.mInitialDownY += obtainTouchDistance;
            }

            this.mInitialMotionY = this.mInitialDownY;
        } else {
            this.mIsBeingDragged = false;
        }

    }

    private void checkScrollStart(int orientation) {
        if (orientation == 2) {
            this.checkVerticalScrollStart(orientation);
        } else {
            this.checkHorizontalScrollStart(orientation);
        }

    }

    private void checkHorizontalScrollStart(int orientation) {
        if (this.getScrollX() != 0) {
            this.mIsBeingDragged = true;
            float obtainTouchDistance = this.obtainTouchDistance((float) Math.abs(this.getScrollX()), Math.abs(this.obtainMaxSpringBackDistance(orientation)), 2);
            if (this.getScrollX() < 0) {
                this.mInitialDownX -= obtainTouchDistance;
            } else {
                this.mInitialDownX += obtainTouchDistance;
            }

            this.mInitialMotionX = this.mInitialDownX;
        } else {
            this.mIsBeingDragged = false;
        }

    }

    private boolean onScrollDownEvent(MotionEvent motionEvent, int i, int i2) {
        if (i == 0) {
            this.mActivePointerId = motionEvent.getPointerId(0);
            this.checkScrollStart(i2);
            return true;
        } else {
            if (i != 1) {
                int findPointerIndex2;
                float y2;
                if (i == 2) {
                    findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    if (this.mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2);
                            signum = Math.signum(y2 - this.mInitialMotionY);
                            obtainSpringBackDistance = this.obtainSpringBackDistance(y2 - this.mInitialMotionY, i2);
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2);
                            signum = Math.signum(y2 - this.mInitialMotionX);
                            obtainSpringBackDistance = this.obtainSpringBackDistance(y2 - this.mInitialMotionX, i2);
                        }

                        y2 = signum * obtainSpringBackDistance;
                        if (!(y2 > 0.0F)) {
                            this.moveTarget(0.0F, i2);
                            return false;
                        }

                        this.requestDisallowParentInterceptTouchEvent(true);
                        this.moveTarget(y2, i2);
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2) - this.mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            this.mInitialDownY = motionEvent.getY(actionIndex) - y2;
                            this.mInitialMotionY = this.mInitialDownY;
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2) - this.mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            this.mInitialDownX = motionEvent.getX(actionIndex) - y2;
                            this.mInitialMotionX = this.mInitialDownX;
                        }

                        this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        this.onSecondaryPointerUp(motionEvent);
                    }
                }
            }

            if (motionEvent.findPointerIndex(this.mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            } else {
                if (this.mIsBeingDragged) {
                    this.mIsBeingDragged = false;
                    this.springBack(i2);
                }

                this.mActivePointerId = -1;
                return false;
            }
        }
    }

    private void moveTarget(float f, int i) {
        if (i == 2) {
            this.scrollTo(0, (int) (-f));
        } else {
            this.scrollTo((int) (-f), 0);
        }

    }

    private void springBack(int i) {
        this.springBack(0.0F, i, true);
    }

    private void springBack(float f, int i, boolean z) {
        OnSpringListener onSpringListener = this.mOnSpringListener;
        if (onSpringListener == null || !onSpringListener.onSpringBack()) {
            this.mSpringScroller.forceStop();
            int scrollX = this.getScrollX();
            int scrollY = this.getScrollY();
            this.mSpringScroller.scrollByFling((float) scrollX, 0.0F, (float) scrollY, 0.0F, f, i, false);
            if (scrollX == 0 && scrollY == 0 && f == 0.0F) {
                this.dispatchScrollState(0);
            } else {
                this.dispatchScrollState(2);
            }

            if (z) {
                this.postInvalidateOnAnimation();
            }
        }

    }

    private boolean onScrollUpEvent(MotionEvent motionEvent, int i, int i2) {
        if (i == 0) {
            this.mActivePointerId = motionEvent.getPointerId(0);
            this.checkScrollStart(i2);
            return true;
        } else {
            if (i != 1) {
                int findPointerIndex2;
                float y2;
                if (i == 2) {
                    findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex2 < 0) {
                        Log.e("SpringBackLayout", "Got ACTION_MOVE event but have an invalid active pointer id.");
                        return false;
                    }

                    if (this.mIsBeingDragged) {
                        float signum;
                        float obtainSpringBackDistance;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2);
                            signum = Math.signum(this.mInitialMotionY - y2);
                            obtainSpringBackDistance = this.obtainSpringBackDistance(this.mInitialMotionY - y2, i2);
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2);
                            signum = Math.signum(this.mInitialMotionX - y2);
                            obtainSpringBackDistance = this.obtainSpringBackDistance(this.mInitialMotionX - y2, i2);
                        }

                        y2 = signum * obtainSpringBackDistance;
                        if (!(y2 > 0.0F)) {
                            this.moveTarget(0.0F, i2);
                            return false;
                        }

                        this.requestDisallowParentInterceptTouchEvent(true);
                        this.moveTarget(-y2, i2);
                    }
                } else if (i != 3) {
                    if (i == 5) {
                        findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex2 < 0) {
                            Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                            return false;
                        }

                        int actionIndex;
                        if (i2 == 2) {
                            y2 = motionEvent.getY(findPointerIndex2) - this.mInitialDownY;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            this.mInitialDownY = motionEvent.getY(actionIndex) - y2;
                            this.mInitialMotionY = this.mInitialDownY;
                        } else {
                            y2 = motionEvent.getX(findPointerIndex2) - this.mInitialDownX;
                            actionIndex = motionEvent.getActionIndex();
                            if (actionIndex < 0) {
                                Log.e("SpringBackLayout", "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                                return false;
                            }

                            this.mInitialDownX = motionEvent.getX(actionIndex) - y2;
                            this.mInitialMotionX = this.mInitialDownX;
                        }

                        this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (i == 6) {
                        this.onSecondaryPointerUp(motionEvent);
                    }
                }
            }

            if (motionEvent.findPointerIndex(this.mActivePointerId) < 0) {
                Log.e("SpringBackLayout", "Got ACTION_UP event but don't have an active pointer id.");
                return false;
            } else {
                if (this.mIsBeingDragged) {
                    this.mIsBeingDragged = false;
                    this.springBack(i2);
                }

                this.mActivePointerId = -1;
                return false;
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mActivePointerId) {
            this.mActivePointerId = motionEvent.getPointerId(actionIndex == 0 ? 1 : 0);
        }

    }

    protected int getSpringBackRange(int i) {
        return i == 2 ? this.mScreenHeight : this.mScreenWith;
    }

    protected float obtainSpringBackDistance(float f, int i) {
        return this.obtainDampingDistance(Math.min(Math.abs(f) / (float) this.getSpringBackRange(i), 1.0F), i);
    }

    protected float obtainMaxSpringBackDistance(int i) {
        return this.obtainDampingDistance(1.0F, i);
    }

    protected float obtainDampingDistance(float f, int i) {
        int springBackRange = this.getSpringBackRange(i);
        double min = (double) Math.min(f, 1.0F);
        return (float) (Math.pow(min, 3.0) / 3.0 - Math.pow(min, 2.0) + min) * (float) springBackRange;
    }

    protected float obtainTouchDistance(float f, float f2, int i) {
        int springBackRange = this.getSpringBackRange(i);
        if (Math.abs(f) >= Math.abs(f2)) {
            f = f2;
        }

        double d = (double) springBackRange;
        return (float) (d - Math.pow(d, 0.6666666666666666) * Math.pow((double) ((float) springBackRange - f * 3.0F), 0.3333333333333333));
    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        boolean z = this.mNestedScrollAxes == 2;
        int i6 = z ? dyConsumed : dxConsumed;
        int i7 = z ? consumed[1] : consumed[0];
        this.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, this.mParentOffsetInWindow, type, consumed);
        if (this.mSpringBackEnable) {
            int i8 = (z ? consumed[1] : consumed[0]) - i7;
            int i9 = z ? dyUnconsumed - i8 : dxUnconsumed - i8;
            int i10 = i9 != 0 ? i9 : 0;
            int i11 = z ? 2 : 1;
            float obtainMaxSpringBackDistance2;
            float f2;
            if (i10 < 0 && this.isTargetScrollToTop(i11) && this.isSupportTopSpringBackMode()) {
                if (type != 0) {
                    obtainMaxSpringBackDistance2 = this.obtainMaxSpringBackDistance(i11);
                    if (this.mVelocityY == 0.0F && this.mVelocityX == 0.0F) {
                        if (this.mTotalScrollTopUnconsumed == 0.0F) {
                            f2 = obtainMaxSpringBackDistance2 - this.mTotalFlingUnconsumed;
                            if (this.consumeNestFlingCounter < 4) {
                                if (f2 <= (float) Math.abs(i10)) {
                                    this.mTotalFlingUnconsumed += f2;
                                    consumed[1] = (int) ((float) consumed[1] + f2);
                                } else {
                                    this.mTotalFlingUnconsumed += (float) Math.abs(i10);
                                    consumed[1] += i9;
                                }

                                this.dispatchScrollState(2);
                                this.moveTarget(this.obtainSpringBackDistance(this.mTotalFlingUnconsumed, i11), i11);
                                ++this.consumeNestFlingCounter;
                            }
                        }
                    } else {
                        this.mScrollByFling = true;
                        if (i6 != 0 && (float) (-i10) <= obtainMaxSpringBackDistance2) {
                            this.mSpringScroller.setFirstStep(i10);
                        }

                        this.dispatchScrollState(2);
                    }
                } else if (this.mSpringScroller.isFinished()) {
                    this.mTotalScrollTopUnconsumed += (float) Math.abs(i10);
                    this.dispatchScrollState(1);
                    this.moveTarget(this.obtainSpringBackDistance(this.mTotalScrollTopUnconsumed, i11), i11);
                    consumed[1] += i9;
                }
            } else if (i10 > 0 && this.isTargetScrollToBottom(i11) && this.isSupportBottomSpringBackMode()) {
                if (type != 0) {
                    obtainMaxSpringBackDistance2 = this.obtainMaxSpringBackDistance(i11);
                    if (this.mVelocityY == 0.0F && this.mVelocityX == 0.0F) {
                        if (this.mTotalScrollBottomUnconsumed == 0.0F) {
                            f2 = obtainMaxSpringBackDistance2 - this.mTotalFlingUnconsumed;
                            if (this.consumeNestFlingCounter < 4) {
                                if (f2 <= (float) Math.abs(i10)) {
                                    this.mTotalFlingUnconsumed += f2;
                                    consumed[1] = (int) ((float) consumed[1] + f2);
                                } else {
                                    this.mTotalFlingUnconsumed += (float) Math.abs(i10);
                                    consumed[1] += i9;
                                }

                                this.dispatchScrollState(2);
                                this.moveTarget(-this.obtainSpringBackDistance(this.mTotalFlingUnconsumed, i11), i11);
                                ++this.consumeNestFlingCounter;
                            }
                        }
                    } else {
                        this.mScrollByFling = true;
                        if (i6 != 0 && (float) i10 <= obtainMaxSpringBackDistance2) {
                            this.mSpringScroller.setFirstStep(i10);
                        }

                        this.dispatchScrollState(2);
                    }
                } else if (this.mSpringScroller.isFinished()) {
                    this.mTotalScrollBottomUnconsumed += (float) Math.abs(i10);
                    this.dispatchScrollState(1);
                    this.moveTarget(-this.obtainSpringBackDistance(this.mTotalScrollBottomUnconsumed, i11), i11);
                    consumed[1] += i9;
                }
            }
        }

    }

    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        this.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, this.mNestedScrollingV2ConsumedCompat);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        this.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, this.mNestedScrollingV2ConsumedCompat);
    }

    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if (this.mSpringBackEnable) {
            this.mNestedScrollAxes = axes;
            boolean z = this.mNestedScrollAxes == 2;
            if (((z ? 2 : 1) & this.mOriginScrollOrientation) == 0 || !this.onStartNestedScroll(child, child, axes)) {
                return false;
            }

            float scrollY = z ? (float) this.getScrollY() : (float) this.getScrollX();
            if (type != 0 && scrollY != 0.0F && this.mTarget instanceof NestedScrollView) {
                return false;
            }
        }

        if (this.mNestedScrollingChildHelper.startNestedScroll(axes, type)) {
        }

        return true;
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return this.isEnabled();
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (this.mSpringBackEnable) {
            boolean z = this.mNestedScrollAxes == 2;
            int i3 = z ? 2 : 1;
            float scrollY = z ? (float) this.getScrollY() : (float) this.getScrollX();
            if (type != 0) {
                if (scrollY == 0.0F) {
                    this.mTotalFlingUnconsumed = 0.0F;
                } else {
                    this.mTotalFlingUnconsumed = this.obtainTouchDistance(Math.abs(scrollY), Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                }

                this.mNestedFlingInProgress = true;
                this.consumeNestFlingCounter = 0;
            } else {
                if (scrollY == 0.0F) {
                    this.mTotalScrollTopUnconsumed = 0.0F;
                    this.mTotalScrollBottomUnconsumed = 0.0F;
                } else if (scrollY < 0.0F) {
                    this.mTotalScrollTopUnconsumed = this.obtainTouchDistance(Math.abs(scrollY), Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                    this.mTotalScrollBottomUnconsumed = 0.0F;
                } else {
                    this.mTotalScrollTopUnconsumed = 0.0F;
                    this.mTotalScrollBottomUnconsumed = this.obtainTouchDistance(Math.abs(scrollY), Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                }

                this.mNestedScrollInProgress = true;
            }

            this.mVelocityY = 0.0F;
            this.mVelocityX = 0.0F;
            this.mScrollByFling = false;
            this.mSpringScroller.forceStop();
        }

        this.onNestedScrollAccepted(child, target, axes);
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        this.startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (this.mSpringBackEnable) {
            if (this.mNestedScrollAxes == 2) {
                this.onNestedPreScroll(dy, consumed, type);
            } else {
                this.onNestedPreScroll(dx, consumed, type);
            }
        }

        if (this.dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], this.mParentScrollConsumed, (int[]) null, type)) {
            consumed[0] += this.mParentScrollConsumed[0];
            consumed[1] += this.mParentScrollConsumed[1];
        }

    }

    private void onNestedPreScroll(int i, @NonNull int[] consumed, int type) {
        boolean z = this.mNestedScrollAxes == 2;
        int i3 = z ? 2 : 1;
        int abs = Math.abs(z ? this.getScrollY() : this.getScrollX());
        float f = 0.0F;
        float f6;
        if (type == 0) {
            if (i > 0) {
                if (this.mTotalScrollTopUnconsumed > 0.0F) {
                    f6 = (float) i;
                    if (f6 > this.mTotalScrollTopUnconsumed) {
                        this.consumeDelta((int) this.mTotalScrollTopUnconsumed, consumed, i3);
                        this.mTotalScrollTopUnconsumed = 0.0F;
                    } else {
                        this.mTotalScrollTopUnconsumed -= f6;
                        this.consumeDelta(i, consumed, i3);
                    }

                    this.dispatchScrollState(1);
                    this.moveTarget(this.obtainSpringBackDistance(this.mTotalScrollTopUnconsumed, i3), i3);
                }
            } else if (i < 0 && -this.mTotalScrollBottomUnconsumed < 0.0F) {
                f6 = (float) i;
                if (f6 < -this.mTotalScrollBottomUnconsumed) {
                    this.consumeDelta((int) this.mTotalScrollBottomUnconsumed, consumed, i3);
                    this.mTotalScrollBottomUnconsumed = 0.0F;
                } else {
                    this.mTotalScrollBottomUnconsumed += f6;
                    this.consumeDelta(i, consumed, i3);
                }

                this.dispatchScrollState(1);
                this.moveTarget(-this.obtainSpringBackDistance(this.mTotalScrollBottomUnconsumed, i3), i3);
            }
        } else {
            f6 = i3 == 2 ? this.mVelocityY : this.mVelocityX;
            float obtainSpringBackDistance;
            float obtainSpringBackDistance2;
            if (i > 0) {
                if (this.mTotalScrollTopUnconsumed > 0.0F) {
                    if (f6 > 2000.0F) {
                        obtainSpringBackDistance = this.obtainSpringBackDistance(this.mTotalScrollTopUnconsumed, i3);
                        obtainSpringBackDistance2 = (float) i;
                        if (obtainSpringBackDistance2 > obtainSpringBackDistance) {
                            this.consumeDelta((int) obtainSpringBackDistance, consumed, i3);
                            this.mTotalScrollTopUnconsumed = 0.0F;
                        } else {
                            this.consumeDelta(i, consumed, i3);
                            f = obtainSpringBackDistance - obtainSpringBackDistance2;
                            this.mTotalScrollTopUnconsumed = this.obtainTouchDistance(f, Math.signum(f) * Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                        }

                        this.moveTarget(f, i3);
                        this.dispatchScrollState(1);
                        return;
                    }

                    if (!this.mScrollByFling) {
                        this.mScrollByFling = true;
                        this.springBack(f6, i3, false);
                    }

                    if (this.mSpringScroller.computeScrollOffset()) {
                        this.scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
                        this.mTotalScrollTopUnconsumed = this.obtainTouchDistance((float) abs, Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                    } else {
                        this.mTotalScrollTopUnconsumed = 0.0F;
                    }

                    this.consumeDelta(i, consumed, i3);
                    return;
                }
            } else if (i < 0) {
                obtainSpringBackDistance = this.mTotalScrollBottomUnconsumed;
                if (-obtainSpringBackDistance < 0.0F) {
                    if (f6 < -2000.0F) {
                        obtainSpringBackDistance2 = this.obtainSpringBackDistance(obtainSpringBackDistance, i3);
                        float f10 = (float) i;
                        if (f10 < -obtainSpringBackDistance2) {
                            this.consumeDelta((int) obtainSpringBackDistance2, consumed, i3);
                            this.mTotalScrollBottomUnconsumed = 0.0F;
                        } else {
                            this.consumeDelta(i, consumed, i3);
                            f = obtainSpringBackDistance2 + f10;
                            this.mTotalScrollBottomUnconsumed = this.obtainTouchDistance(f, Math.signum(f) * Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                        }

                        this.dispatchScrollState(1);
                        this.moveTarget(-f, i3);
                        return;
                    }

                    if (!this.mScrollByFling) {
                        this.mScrollByFling = true;
                        this.springBack(f6, i3, false);
                    }

                    if (this.mSpringScroller.computeScrollOffset()) {
                        this.scrollTo(this.mSpringScroller.getCurrX(), this.mSpringScroller.getCurrY());
                        this.mTotalScrollBottomUnconsumed = this.obtainTouchDistance((float) abs, Math.abs(this.obtainMaxSpringBackDistance(i3)), i3);
                    } else {
                        this.mTotalScrollBottomUnconsumed = 0.0F;
                    }

                    this.consumeDelta(i, consumed, i3);
                    return;
                }
            }

            if (i != 0 && (this.mTotalScrollBottomUnconsumed == 0.0F || this.mTotalScrollTopUnconsumed == 0.0F) && this.mScrollByFling && this.getScrollY() == 0) {
                this.consumeDelta(i, consumed, i3);
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
        this.mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return this.mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public void onStopNestedScroll(@NonNull View target, int type) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(target, type);
        this.stopNestedScroll(type);
        if (this.mSpringBackEnable) {
            boolean z = this.mNestedScrollAxes == 2;
            int i2 = z ? 2 : 1;
            if (this.mNestedScrollInProgress) {
                this.mNestedScrollInProgress = false;
                float scrollY = z ? (float) this.getScrollY() : (float) this.getScrollX();
                if (!this.mNestedFlingInProgress && scrollY != 0.0F) {
                    this.springBack(i2);
                } else if (scrollY != 0.0F) {
                    this.stopNestedFlingScroll(i2);
                }
            } else if (this.mNestedFlingInProgress) {
                this.stopNestedFlingScroll(i2);
            }
        }

    }

    private void stopNestedFlingScroll(int i) {
        this.mNestedFlingInProgress = false;
        if (this.mScrollByFling) {
            if (this.mSpringScroller.isFinished()) {
                this.springBack(i == 2 ? this.mVelocityY : this.mVelocityX, i, false);
            }

            this.postInvalidateOnAnimation();
        } else {
            this.springBack(i);
        }

    }

    public void stopNestedScroll() {
        this.mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return this.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return this.dispatchNestedPreFling(velocityX, velocityY);
    }

    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
        this.mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    public boolean startNestedScroll(int axes, int type) {
        return this.mNestedScrollingChildHelper.startNestedScroll(axes, type);
    }

    public boolean startNestedScroll(int axes) {
        return this.mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll(int type) {
        this.mNestedScrollingChildHelper.stopNestedScroll(type);
    }

    public boolean hasNestedScrollingParent(int type) {
        return this.mNestedScrollingChildHelper.hasNestedScrollingParent(type);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return this.mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return this.mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public void smoothScrollTo(int i, int i2) {
        if (i - this.getScrollX() != 0 && i2 - this.getScrollY() != 0) {
            this.mSpringScroller.forceStop();
            this.mSpringScroller.scrollByFling((float) this.getScrollX(), (float) i, (float) this.getScrollY(), (float) i2, 0.0F, 2, true);
            this.dispatchScrollState(2);
            this.postInvalidateOnAnimation();
        }

    }

    private void dispatchScrollState(int state) {
        if (this.mScrollState != state) {
            this.mScrollState = state;
            Iterator var2 = this.mOnScrollListeners.iterator();

            while (var2.hasNext()) {
                OnScrollListener onScrollListener = (OnScrollListener) var2.next();
                onScrollListener.onStateChanged(this.mScrollState, state, this.mSpringScroller.isFinished());
            }
        }

    }

    public void addOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListeners.add(onScrollListener);
    }

    public void removeOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListeners.remove(onScrollListener);
    }

    public void setOnSpringListener(OnSpringListener onSpringListener) {
        this.mOnSpringListener = onSpringListener;
    }

    public boolean hasSpringListener() {
        return this.mOnSpringListener != null;
    }

    public boolean onNestedCurrentFling(float velocityX, float velocityY) {
        this.mVelocityX = velocityX;
        this.mVelocityY = velocityY;
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
