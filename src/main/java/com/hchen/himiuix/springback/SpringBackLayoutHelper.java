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
    private final ViewGroup mTarget;
    int mTargetScrollOrientation;
    private final int mTouchSlop;

    public SpringBackLayoutHelper(ViewGroup target, int orientation) {
        mTarget = target;
        mTargetScrollOrientation = orientation;
        mTouchSlop = ViewConfiguration.get(target.getContext()).getScaledTouchSlop();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        checkOrientation(ev);
        boolean disallowIntercept = mScrollOrientation != 0 && mScrollOrientation != mTargetScrollOrientation;
        mTarget.requestDisallowInterceptTouchEvent(disallowIntercept);
        return !disallowIntercept;
    }

    public boolean isTouchInTarget(MotionEvent ev) {
        int findPointerIndex = ev.findPointerIndex(ev.getPointerId(0));
        if (findPointerIndex >= 0) {
            float y = ev.getY(findPointerIndex);
            float x = ev.getX(findPointerIndex);
            int[] iArr = new int[]{0, 0};
            mTarget.getLocationInWindow(iArr);
            int i = iArr[0];
            int i2 = iArr[1];
            return (new Rect(i, i2, mTarget.getWidth() + i, mTarget.getHeight() + i2)).contains((int)x, (int)y);
        } else {
            return false;
        }
    }

    public void checkOrientation(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        int findPointerIndex;
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                findPointerIndex = ev.findPointerIndex(mActivePointerId);
                if (findPointerIndex >= 0) {
                    mInitialDownY = ev.getY(findPointerIndex);
                    mInitialDownX = ev.getX(findPointerIndex);
                    mScrollOrientation = 0;
                }
                break;
            case MotionEvent.ACTION_UP:
                mScrollOrientation = 0;
                mTarget.requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != -1) {
                    findPointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (findPointerIndex >= 0) {
                        float x = ev.getX(findPointerIndex) - mInitialDownX;
                        float y = ev.getY(findPointerIndex) - mInitialDownY;
                        if (Math.abs(x) > (float)mTouchSlop || Math.abs(y) > (float)mTouchSlop) {
                            mScrollOrientation = Math.abs(x) <= Math.abs(y) ? 2 : 1;
                        }
                    }
                }
        }
    }
}
