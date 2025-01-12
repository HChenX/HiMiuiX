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


public class SpringScroller {
    private double mCurrX;
    private double mCurrY;
    private long mCurrentTimeNanos;
    private double mEndX;
    private double mEndY;
    private boolean mFinished = true;
    private int mFirstStep;
    private boolean mLastStep;
    private int mOrientation;
    private double mOriginStartX;
    private double mOriginStartY;
    private double mOriginVelocity;
    private SpringOperator mSpringOperator;
    private long mStartTimeNanos;
    private double mStartX;
    private double mStartY;
    private double mVelocity;

    public void scrollByFling(float scrollX, float endX, float scrollY, float endY, float xory, int mode) {
        mFinished = false;
        mLastStep = false;
        mStartX = scrollX;
        mOriginStartX = scrollX;
        mEndX = endX;
        mStartY = scrollY;
        mOriginStartY = scrollY;
        mCurrY = (int) (double) scrollY;
        mEndY = endY;
        mOriginVelocity = xory;
        mVelocity = xory;
        if (Math.abs(xory) <= 5000.0d) {
            mSpringOperator = new SpringOperator(1.0f, 0.4f);
        } else {
            mSpringOperator = new SpringOperator(1.0f, 0.55f);
        }
        mOrientation = mode;
        mStartTimeNanos = AnimationUtils.currentAnimationTimeNanos();
    }

    public boolean computeScrollOffset() {
        if (mSpringOperator == null || mFinished) {
            return false;
        }

        if (mFirstStep != 0) {
            if (mOrientation == 1) {
                mCurrX = mFirstStep;
                mStartX = mFirstStep;
            } else {
                mCurrY = mFirstStep;
                mStartY = mFirstStep;
            }
            mFirstStep = 0;
            return true;
        }
        if (mLastStep) {
            mFinished = true;
            return true;
        }
        mCurrentTimeNanos = AnimationUtils.currentAnimationTimeNanos();
        double min = Math.min((mCurrentTimeNanos - mStartTimeNanos) / 1.0E9d, 0.01600000075995922d);
        min = min != 0.0d ? min : 0.01600000075995922d;
        mStartTimeNanos = mCurrentTimeNanos;
        if (mOrientation == 2) {
            double updateVelocity = mSpringOperator.updateVelocity(mVelocity, min, mEndY, mStartY);
            mCurrY = mStartY + (min * updateVelocity);
            mVelocity = updateVelocity;
            if (isAtEquilibrium(mCurrY, mOriginStartY, mEndY)) {
                mLastStep = true;
                mCurrY = mEndY;
            } else {
                mStartY = mCurrY;
            }
        } else {
            double updateVelocity2 = mSpringOperator.updateVelocity(mVelocity, min, mEndX, mStartX);
            mCurrX = mStartX + (min * updateVelocity2);
            mVelocity = updateVelocity2;
            if (isAtEquilibrium(mCurrX, mOriginStartX, mEndX)) {
                mLastStep = true;
                mCurrX = mEndX;
            } else {
                mStartX = mCurrX;
            }
        }
        return true;
    }

    public boolean isAtEquilibrium(double curr, double originStart, double end) {
        if (originStart < end && curr > end) {
            return true;
        }
        if (originStart <= end || curr >= end) {
            return (originStart == end && Math.signum(mOriginVelocity) != Math.signum(curr)) || Math.abs(curr - end) < 1.0d;
        }
        return true;
    }

    public final int getCurrX() {
        return (int) mCurrX;
    }

    public final int getCurrY() {
        return (int) mCurrY;
    }

    public final boolean isFinished() {
        return mFinished;
    }

    public final void forceStop() {
        mFinished = true;
        mFirstStep = 0;
    }

    public void setFirstStep(int firstStep) {
        mFirstStep = firstStep;
    }
}
