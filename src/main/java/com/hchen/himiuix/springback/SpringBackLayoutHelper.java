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

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class SpringBackLayoutHelper {
    float mInitialDownX;
    float mInitialDownY;
    int mScrollOrientation;
    int mActivePointerId = -1;
    int mTargetScrollOrientation;
    private int mTouchSlop;
    private ViewGroup mTarget;

    public SpringBackLayoutHelper(ViewGroup target, int orientation) {
        mTarget = target;
        mTargetScrollOrientation = orientation;
        mTouchSlop = ViewConfiguration.get(target.getContext()).getScaledTouchSlop();
    }

    public boolean isTouchInTarget(MotionEvent ev) {
        int index = ev.findPointerIndex(ev.getPointerId(0));
        if (index >= 0) {
            float y = ev.getY(index);
            float x = ev.getX(index);
            int[] iArr = {0, 0};
            mTarget.getLocationInWindow(iArr);
            int i = iArr[0];
            int i2 = iArr[1];
            return new Rect(
                i,
                i2,
                mTarget.getWidth() + i,
                mTarget.getHeight() + i2
            ).contains((int) x, (int) y);
        }
        return false;
    }

    public void checkOrientation(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mActivePointerId = ev.getPointerId(0);
            int index = ev.findPointerIndex(mActivePointerId);
            if (index >= 0) {
                mInitialDownY = ev.getY(index);
                mInitialDownX = ev.getX(index);
                mScrollOrientation = 0;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mScrollOrientation = 0;
            mTarget.requestDisallowInterceptTouchEvent(false);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mActivePointerId != -1) {
                int index = ev.findPointerIndex(mActivePointerId);
                if (index >= 0) {
                    float y = ev.getY(index);
                    float x = ev.getX(index);
                    float f = y - mInitialDownY;
                    float f2 = x - mInitialDownX;
                    if (Math.abs(f2) > mTouchSlop || Math.abs(f) > mTouchSlop) {
                        mScrollOrientation = Math.abs(f2) <= Math.abs(f) ? 2 : 1;
                    }
                }
            }
        }
    }
}
