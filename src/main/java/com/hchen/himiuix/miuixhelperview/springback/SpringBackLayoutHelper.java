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

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class SpringBackLayoutHelper {
    private static final int INVALID_POINTER = -1;
    public int mActivePointerId = -1;
    public float mInitialDownX;
    public float mInitialDownY;
    public int mScrollOrientation;
    private final ViewGroup mTarget;
    public int mTargetScrollOrientation;
    private final int mTouchSlop;

    public SpringBackLayoutHelper(ViewGroup viewGroup, int i2) {
        this.mTarget = viewGroup;
        this.mTargetScrollOrientation = i2;
        this.mTouchSlop = ViewConfiguration.get(viewGroup.getContext()).getScaledTouchSlop();
    }

    public void checkOrientation(MotionEvent motionEvent) {
        int findPointerIndex;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int i2 = this.mActivePointerId;
                    if (i2 != -1 && (findPointerIndex = motionEvent.findPointerIndex(i2)) >= 0) {
                        float y2 = motionEvent.getY(findPointerIndex);
                        float x2 = motionEvent.getX(findPointerIndex);
                        float f2 = y2 - this.mInitialDownY;
                        float f3 = x2 - this.mInitialDownX;
                        if (Math.abs(f3) > this.mTouchSlop || Math.abs(f2) > this.mTouchSlop) {
                            this.mScrollOrientation = Math.abs(f3) <= Math.abs(f2) ? 2 : 1;
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (actionMasked != 3) {
                    return;
                }
            }
            this.mScrollOrientation = 0;
            this.mTarget.requestDisallowInterceptTouchEvent(false);
            return;
        }
        int pointerId = motionEvent.getPointerId(0);
        this.mActivePointerId = pointerId;
        int findPointerIndex2 = motionEvent.findPointerIndex(pointerId);
        if (findPointerIndex2 < 0) {
            return;
        }
        this.mInitialDownY = motionEvent.getY(findPointerIndex2);
        this.mInitialDownX = motionEvent.getX(findPointerIndex2);
        this.mScrollOrientation = 0;
    }

    public boolean isTouchInTarget(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(motionEvent.getPointerId(0));
        if (findPointerIndex < 0) {
            return false;
        }
        float y2 = motionEvent.getY(findPointerIndex);
        float x2 = motionEvent.getX(findPointerIndex);
        int[] iArr = {0, 0};
        this.mTarget.getLocationInWindow(iArr);
        int i2 = iArr[0];
        int i3 = iArr[1];
        return new Rect(i2, i3, this.mTarget.getWidth() + i2, this.mTarget.getHeight() + i3).contains((int) x2, (int) y2);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        checkOrientation(motionEvent);
        int i2 = this.mScrollOrientation;
        if (i2 != 0 && i2 != this.mTargetScrollOrientation) {
            this.mTarget.requestDisallowInterceptTouchEvent(true);
            return false;
        }
        this.mTarget.requestDisallowInterceptTouchEvent(false);
        return true;
    }
}
