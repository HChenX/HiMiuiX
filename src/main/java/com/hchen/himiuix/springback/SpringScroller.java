package com.hchen.himiuix.springback;

import android.view.animation.AnimationUtils;

public class SpringScroller {
    private static final float MAX_DELTA_TIME = 0.016F;
    private static final float VALUE_THRESHOLD = 1.0F;
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
    private long mStartTime;
    private double mStartX;
    private double mStartY;
    private double mVelocity;

    public SpringScroller() {
    }

    public void scrollByFling(float f, float f2, float f3, float f4, float f5, int i, boolean z) {
        mFinished = false;
        mLastStep = false;
        mStartX = f;
        mOriginStartX = f;
        mEndX = f2;
        mStartY = f3;
        mOriginStartY = f3;
        mCurrY = ((int) mStartY);
        mEndY = f4;
        mOriginVelocity = f5;
        mVelocity = f5;
        if (!(Math.abs(mVelocity) <= 5000.0) && !z) {
            mSpringOperator = new SpringOperator(1.0F, 0.55F);
        } else {
            mSpringOperator = new SpringOperator(1.0F, 0.4F);
        }

        mOrientation = i;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public boolean computeScrollOffset() {
        if (mSpringOperator != null && !mFinished) {
            if (mFirstStep != 0) {
                if (mOrientation == 1) {
                    mCurrX =  mFirstStep;
                    mStartX =  mFirstStep;
                } else {
                    mCurrY =  mFirstStep;
                    mStartY =  mFirstStep;
                }

                mFirstStep = 0;
                return true;
            } else if (mLastStep) {
                mFinished = true;
                return true;
            } else {
                long mCurrentTime = AnimationUtils.currentAnimationTimeMillis();
                float min = Math.min((float) (mCurrentTime - mStartTime) / 1000.0F, 0.016F);
                if (min == 0.0F) {
                    min = 0.016F;
                }

                mStartTime = mCurrentTime;
                double updateVelocity;
                if (mOrientation == 2) {
                    updateVelocity = mSpringOperator.updateVelocity(mVelocity, min, mEndY, mStartY);
                    mCurrY = mStartY + (double) min * updateVelocity;
                    mVelocity = updateVelocity;
                    if (isAtEquilibrium(mCurrY, mOriginStartY, mEndY)) {
                        mLastStep = true;
                        mCurrY = mEndY;
                    } else {
                        mStartY = mCurrY;
                    }
                } else {
                    updateVelocity = mSpringOperator.updateVelocity(mVelocity, min, mEndX, mStartX);
                    mCurrX = mStartX + (double) min * updateVelocity;
                    mVelocity = updateVelocity;
                    if (isAtEquilibrium(mCurrX, mOriginStartX, mEndX)) {
                        mLastStep = true;
                        mCurrX = mEndX;
                    } else {
                        mStartX = mCurrX;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isAtEquilibrium(double d, double d2, double d3) {
        if (!(d2 >= d3) && !(d <= d3)) {
            return true;
        } else {
            int i = Double.compare(d2, d3);
            if (i > 0 && !(d >= d3)) {
                return true;
            } else {
                return i == 0 && Math.signum(mOriginVelocity) != Math.signum(d) || Math.abs(d - d3) < 1.0;
            }
        }
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

    public void setFirstStep(int step) {
        mFirstStep = step;
    }
}
