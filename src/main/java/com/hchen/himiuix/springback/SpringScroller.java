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

public class SpringScroller {
    private double mCurrX;
    private double mCurrY;
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

    public void scrollByFling(float f, float f2, float f3, float f4, float f5, int i, boolean z) {
        mFinished = false;
        mLastStep = false;
        mStartX = f;
        mOriginStartX = f;
        mEndX = f2;
        mStartY = f3;
        mOriginStartY = f3;
        mCurrY = (int) (double) f3;
        mEndY = f4;
        double d3 = f5;
        mOriginVelocity = d3;
        mVelocity = d3;
        if (Math.abs(d3) <= 5000.0d || z) {
            mSpringOperator = new SpringOperator(1.0f, 0.4f);
        } else {
            mSpringOperator = new SpringOperator(1.0f, 0.55f);
        }
        mOrientation = i;
        mStartTimeNanos = AnimationUtils.currentAnimationTimeNanos();
    }

    public boolean computeScrollOffset() {
        if (mSpringOperator == null || mFinished) {
            return false;
        }
        int i = mFirstStep;
        if (i != 0) {
            if (mOrientation == 1) {
                mCurrX = i;
                mStartX = i;
            } else {
                mCurrY = i;
                mStartY = i;
            }
            mFirstStep = 0;
            return true;
        }
        if (mLastStep) {
            mFinished = true;
            return true;
        }
        long mCurrentTimeNanos = AnimationUtils.currentAnimationTimeNanos();
        double min = Math.min((mCurrentTimeNanos - mStartTimeNanos) / 1.0E9d, 0.01600000075995922d);
        double d = min != 0.0d ? min : 0.01600000075995922d;
        mStartTimeNanos = mCurrentTimeNanos;
        if (mOrientation == 2) {
            double updateVelocity = mSpringOperator.updateVelocity(mVelocity, d, mEndY, mStartY);
            double d2 = mStartY + (d * updateVelocity);
            mCurrY = d2;
            mVelocity = updateVelocity;
            if (isAtEquilibrium(d2, mOriginStartY, mEndY)) {
                mLastStep = true;
                mCurrY = mEndY;
            } else {
                mStartY = mCurrY;
            }
        } else {
            double updateVelocity2 = mSpringOperator.updateVelocity(mVelocity, d, mEndX, mStartX);
            double d3 = mStartX + (d * updateVelocity2);
            mCurrX = d3;
            mVelocity = updateVelocity2;
            if (isAtEquilibrium(d3, mOriginStartX, mEndX)) {
                mLastStep = true;
                mCurrX = mEndX;
            } else {
                mStartX = mCurrX;
            }
        }
        return true;
    }

    public boolean isAtEquilibrium(double d, double d2, double d3) {
        if (d2 < d3 && d > d3) {
            return true;
        }
        if (d2 <= d3 || d >= d3) {
            return (d2 == d3 && Math.signum(mOriginVelocity) != Math.signum(d)) || Math.abs(d - d3) < 1.0d;
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

    public void setFirstStep(int i) {
        mFirstStep = i;
    }

    private static class SpringOperator {
        private final double damping;
        private final double tension;

        private SpringOperator(float f, float f2) {
            tension = Math.pow(6.283185307179586d / (double) f2, 2.0d);
            damping = (f * 12.566370614359172d) / (double) f2;
        }

        private double updateVelocity(double d, double d2, double d3, double d4) {
            return (d * (1.0d - (damping * d2))) + ((float) (tension * (d3 - d4) * d2));
        }
    }

    public static class AnimationUtils extends android.view.animation.AnimationUtils {
        private static final ThreadLocal<AnimationNanoState> sAnimationNanoState = ThreadLocal.withInitial(AnimationNanoState::new);

        private static class AnimationNanoState {
            long lastReportedTimeNanos;

            private AnimationNanoState() {
            }
        }

        public static long currentAnimationTimeNanos() {
            AnimationNanoState animationNanoState = sAnimationNanoState.get();
            long nanoTime = System.nanoTime();
            if (animationNanoState != null) {
                animationNanoState.lastReportedTimeNanos = nanoTime;
            }
            return nanoTime;
        }
    }
}