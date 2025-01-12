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
    int mActivePointerId = -1;
    float mInitialDownX;
    float mInitialDownY;
    int mScrollOrientation;
    private final ViewGroup mTarget;
    int mTargetScrollOrientation;
    private final int mTouchSlop;

    public SpringBackLayoutHelper(ViewGroup viewGroup, int ori) {
        mTarget = viewGroup;
        mTargetScrollOrientation = ori;
        mTouchSlop = ViewConfiguration.get(viewGroup.getContext()).getScaledTouchSlop();
    }

    public boolean isTouchInTarget(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(motionEvent.getPointerId(0));
        if (findPointerIndex < 0) {
            return false;
        }
        float y = motionEvent.getY(findPointerIndex);
        float x = motionEvent.getX(findPointerIndex);
        int[] outLocation = {0, 0};
        mTarget.getLocationInWindow(outLocation);
        int locationX = outLocation[0];
        int locationY = outLocation[1];
        return new Rect(locationX, locationY, mTarget.getWidth() + locationX, mTarget.getHeight() + locationY).contains((int) x, (int) y);
    }

    public void checkOrientation(MotionEvent motionEvent) {
        int findPointerIndex;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            int pointerId = motionEvent.getPointerId(0);
            mActivePointerId = pointerId;
            int findPointerIndex2 = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex2 < 0) {
                return;
            }
            mInitialDownY = motionEvent.getY(findPointerIndex2);
            mInitialDownX = motionEvent.getX(findPointerIndex2);
            mScrollOrientation = 0;
            return;
        }
        if (actionMasked != MotionEvent.ACTION_UP) {
            if (actionMasked == MotionEvent.ACTION_MOVE) {
                if (mActivePointerId != -1 && (findPointerIndex = motionEvent.findPointerIndex(mActivePointerId)) >= 0) {
                    float x = motionEvent.getX(findPointerIndex) - mInitialDownX;
                    float y = motionEvent.getY(findPointerIndex) - mInitialDownY;
                    if (Math.abs(x) > mTouchSlop || Math.abs(y) > mTouchSlop) {
                        mScrollOrientation = Math.abs(x) <= Math.abs(y) ? 2 : 1;
                        return;
                    }
                    return;
                }
                return;
            }
            if (actionMasked != MotionEvent.ACTION_CANCEL) {
                return;
            }
        }
        mScrollOrientation = 0;
        mTarget.requestDisallowInterceptTouchEvent(false);
    }
}
