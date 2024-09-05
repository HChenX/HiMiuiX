package com.hchen.himiuix.springback;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class SpringBackLayoutHelper {
    private static final int INVALID_POINTER = -1;
    int mActivePointerId = -1;
    float mInitialDownX;
    float mInitialDownY;
    int mScrollOrientation;
    private ViewGroup mTarget;
    int mTargetScrollOrientation;
    private int mTouchSlop;

    public SpringBackLayoutHelper(ViewGroup target, int orientation) {
        this.mTarget = target;
        this.mTargetScrollOrientation = orientation;
        this.mTouchSlop = ViewConfiguration.get(target.getContext()).getScaledTouchSlop();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.checkOrientation(ev);
        boolean disallowIntercept = this.mScrollOrientation != 0 && this.mScrollOrientation != this.mTargetScrollOrientation;
        this.mTarget.requestDisallowInterceptTouchEvent(disallowIntercept);
        return !disallowIntercept;
    }

    public boolean isTouchInTarget(MotionEvent ev) {
        int findPointerIndex = ev.findPointerIndex(ev.getPointerId(0));
        if (findPointerIndex >= 0) {
            float y = ev.getY(findPointerIndex);
            float x = ev.getX(findPointerIndex);
            int[] iArr = new int[]{0, 0};
            this.mTarget.getLocationInWindow(iArr);
            int i = iArr[0];
            int i2 = iArr[1];
            return (new Rect(i, i2, this.mTarget.getWidth() + i, this.mTarget.getHeight() + i2)).contains((int)x, (int)y);
        } else {
            return false;
        }
    }

    public void checkOrientation(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        int findPointerIndex;
        switch (actionMasked) {
            case 0:
                this.mActivePointerId = ev.getPointerId(0);
                findPointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex >= 0) {
                    this.mInitialDownY = ev.getY(findPointerIndex);
                    this.mInitialDownX = ev.getX(findPointerIndex);
                    this.mScrollOrientation = 0;
                }
                break;
            case 1:
                this.mScrollOrientation = 0;
                this.mTarget.requestDisallowInterceptTouchEvent(false);
                break;
            case 2:
                if (this.mActivePointerId != -1) {
                    findPointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex >= 0) {
                        float x = ev.getX(findPointerIndex) - this.mInitialDownX;
                        float y = ev.getY(findPointerIndex) - this.mInitialDownY;
                        if (Math.abs(x) > (float)this.mTouchSlop || Math.abs(y) > (float)this.mTouchSlop) {
                            this.mScrollOrientation = Math.abs(x) <= Math.abs(y) ? 2 : 1;
                        }
                    }
                }
        }

    }
}
