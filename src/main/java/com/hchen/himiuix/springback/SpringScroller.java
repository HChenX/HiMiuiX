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

import android.view.animation.AnimationUtils;

public class SpringScroller {
    private static final float MAX_DELTA_TIME = 0.016f;
    private static final float VALUE_THRESHOLD = 1.0f;
    private double mCurrX;
    private double mCurrY;
    private long mCurrentTime;
    private double mEndX;
    private double mEndY;
    private boolean mFinished;
    private int mFirstStep;
    private boolean mLastStep;
    private int mOrientation;
    private double mOriginStartX;
    private double mOriginStartY;
    private double mOriginVelocity;
    private SpringOperator mSpringOperator;
    private long mStartTime;
    private double mStartX;
    private double mStartY;
    private double mVelocity;

    public SpringScroller() {
        super();
        this.mFinished = true;
    }

    public boolean computeScrollOffset() {
        if (this.mSpringOperator == null || this.mFinished) {
            return false;
        }
        final int mFirstStep = this.mFirstStep;
        if (mFirstStep != 0) {
            if (this.mOrientation == 1) {
                this.mCurrX = mFirstStep;
                this.mStartX = mFirstStep;
            }
            else {
                this.mCurrY = mFirstStep;
                this.mStartY = mFirstStep;
            }
            this.mFirstStep = 0;
            return true;
        }
        if (this.mLastStep) {
            return this.mFinished = true;
        }
        final long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mCurrentTime = currentAnimationTimeMillis;
        final float a = (currentAnimationTimeMillis - mStartTime) / 1000.0f;
        float n = 0.016f;
        final float min = Math.min(a, 0.016f);
        if (min != 0.0f) {
            n = min;
        }
        this.mStartTime = mCurrentTime;
        if (this.mOrientation == 2) {
            double updateVelocity = mSpringOperator.updateVelocity(mVelocity, n, this.mEndY, this.mStartY);
            final double mCurrY = mStartY + n * updateVelocity;
            this.mCurrY = mCurrY;
            this.mVelocity = updateVelocity;
            if (this.isAtEquilibrium(mCurrY, mOriginStartY, mEndY)) {
                mLastStep = true;
                this.mCurrY = this.mEndY;
            } else {
                this.mStartY = this.mCurrY;
            }
        }
        else {
            final double updateVelocity2 = this.mSpringOperator.updateVelocity(this.mVelocity, n, this.mEndX, this.mStartX);
            final double mCurrX = this.mStartX + n * updateVelocity2;
            this.mCurrX = mCurrX;
            this.mVelocity = updateVelocity2;
            if (this.isAtEquilibrium(mCurrX, this.mOriginStartX, this.mEndX)) {
                this.mLastStep = true;
                this.mCurrX = this.mEndX;
            }
            else {
                this.mStartX = this.mCurrX;
            }
        }
        return true;
    }

    public final void forceStop() {
        this.mFinished = true;
        this.mFirstStep = 0;
    }

    public final int getCurrX() {
        return (int)this.mCurrX;
    }

    public final int getCurrY() {
        return (int)this.mCurrY;
    }

    public boolean isAtEquilibrium(double d, double d2, double d3) {
        if (d2 < d3 && d > d3) {
            return true;
        }
        if (d2 <= d3 || d >= d3) {
            return (d2 == d3 && Math.signum(this.mOriginVelocity) != Math.signum(d)) || Math.abs(d - d3) < 1.0d;
        }
        return true;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public void scrollByFling(final float n, final float n2, final float n3, final float n4, final float n5, final int mOrientation, final boolean b) {
        this.mFinished = false;
        this.mLastStep = false;
        final double n6 = n;
        this.mStartX = n6;
        this.mOriginStartX = n6;
        this.mEndX = n2;
        final double n7 = n3;
        this.mStartY = n7;
        this.mOriginStartY = n7;
        this.mCurrY = (int)n7;
        this.mEndY = n4;
        final double a = n5;
        this.mOriginVelocity = a;
        this.mVelocity = a;
        if (Math.abs(a) > 5000.0 && !b) {
            this.mSpringOperator = new SpringOperator(1.0f, 0.55f);
        }
        else {
            this.mSpringOperator = new SpringOperator(1.0f, 0.4f);
        }
        this.mOrientation = mOrientation;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public void setFirstStep(final int mFirstStep) {
        this.mFirstStep = mFirstStep;
    }
}
