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

import android.view.animation.AnimationUtils;

public class SpringScroller {
    private static final float MAX_DELTA_TIME = 0.016f;
    private static final float VALUE_THRESHOLD = 1.0f;
    private double mCurrX;
    private double mCurrY;
    private long mCurrentTime;
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
    private long mStartTime;
    private double mStartX;
    private double mStartY;
    private double mVelocity;

    public boolean computeScrollOffset() {
        if (this.mSpringOperator == null || this.mFinished) {
            return false;
        }
        int i2 = this.mFirstStep;
        if (i2 != 0) {
            if (this.mOrientation == 1) {
                this.mCurrX = i2;
                this.mStartX = i2;
            } else {
                this.mCurrY = i2;
                this.mStartY = i2;
            }
            this.mFirstStep = 0;
            return true;
        }
        if (this.mLastStep) {
            this.mFinished = true;
            return true;
        }
        long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mCurrentTime = currentAnimationTimeMillis;
        float f2 = ((float) (currentAnimationTimeMillis - this.mStartTime)) / 1000.0f;
        float f3 = MAX_DELTA_TIME;
        float min = Math.min(f2, MAX_DELTA_TIME);
        if (min != 0.0f) {
            f3 = min;
        }
        this.mStartTime = this.mCurrentTime;
        if (this.mOrientation == 2) {
            double updateVelocity = this.mSpringOperator.updateVelocity(this.mVelocity, f3, this.mEndY, this.mStartY);
            double d2 = this.mStartY + (f3 * updateVelocity);
            this.mCurrY = d2;
            this.mVelocity = updateVelocity;
            if (isAtEquilibrium(d2, this.mOriginStartY, this.mEndY)) {
                this.mLastStep = true;
                this.mCurrY = this.mEndY;
            } else {
                this.mStartY = this.mCurrY;
            }
        } else {
            double updateVelocity2 = this.mSpringOperator.updateVelocity(this.mVelocity, f3, this.mEndX, this.mStartX);
            double d3 = this.mStartX + (f3 * updateVelocity2);
            this.mCurrX = d3;
            this.mVelocity = updateVelocity2;
            if (isAtEquilibrium(d3, this.mOriginStartX, this.mEndX)) {
                this.mLastStep = true;
                this.mCurrX = this.mEndX;
            } else {
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
        return (int) this.mCurrX;
    }

    public final int getCurrY() {
        return (int) this.mCurrY;
    }

    public boolean isAtEquilibrium(double d2, double d3, double d4) {
        if (d3 < d4 && d2 > d4) {
            return true;
        }
        if (d3 <= d4 || d2 >= d4) {
            return (d3 == d4 && Math.signum(this.mOriginVelocity) != Math.signum(d2)) || Math.abs(d2 - d4) < 1.0d;
        }
        return true;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public void scrollByFling(float f2, float f3, float f4, float f5, float f6, int i2, boolean z2) {
        this.mFinished = false;
        this.mLastStep = false;
        this.mStartX = f2;
        this.mOriginStartX = f2;
        this.mEndX = f3;
        this.mStartY = f4;
        this.mOriginStartY = f4;
        this.mCurrY = (int) (double) f4;
        this.mEndY = f5;
        this.mOriginVelocity = f6;
        this.mVelocity = f6;
        if (Math.abs((double) f6) > 5000.0d && !z2) {
            this.mSpringOperator = new SpringOperator(1.0f, 0.55f);
        } else {
            this.mSpringOperator = new SpringOperator(1.0f, 0.4f);
        }
        this.mOrientation = i2;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public void setFirstStep(int i2) {
        this.mFirstStep = i2;
    }
}
