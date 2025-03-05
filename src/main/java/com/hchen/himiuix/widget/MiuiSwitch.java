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
package com.hchen.himiuix.widget;

import static com.hchen.himiuix.MiuiXUtils.dp2px;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

public class MiuiSwitch extends ConstraintLayout {
    private static final String TAG = "MiuiPreference";
    private View mThumbView;
    private ViewPropertyAnimator mThumbViewAnimator;
    private OnSwitchStateChangeListener mOnSwitchStateChangeListener;
    private boolean isChecked = false;
    private boolean isAnimationShowing = false;
    private final int ANIMATION_DURATION = 320;
    private final float ANIMATION_TENSION = 1.2f;
    private final float ANIMATION_START_END_OFFSET = 4.2f;
    private final float THUMB_END_X = 20.5f;
    private TransitionDrawable offToOnTransition;
    private TransitionDrawable onToOffTransition;
    private static final int ANIMATION_NEXT = 3;
    private static final int ANIMATION_DONE = 4;
    private final Handler animationHandler = new Handler(Looper.getMainLooper()) {
        private boolean shouldNextAnimation = false;
        private boolean nextValue = false;
        private boolean show = false;

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case ANIMATION_NEXT -> {
                    shouldNextAnimation = true;
                    Object[] objs = (Object[]) msg.obj;
                    nextValue = (boolean) objs[0];
                    show = (boolean) objs[1];
                }
                case ANIMATION_DONE -> {
                    if (shouldNextAnimation) {
                        showThumbAnimationIfNeed(show, nextValue);
                        shouldNextAnimation = false;
                    }
                }
            }
        }
    };

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAnimationShowing)
                return;

            final boolean newValue = !isChecked();
            if (mOnSwitchStateChangeListener == null)
                setChecked(newValue);
            else if (mOnSwitchStateChangeListener.onSwitchStateChange(newValue)) {
                setChecked(newValue);
            }

            if (v != null) v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    };

    /*
     * 监听鼠标动作
     * */
    private final View.OnHoverListener mHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                mSwitchAnimationAction.animZoom();
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                mSwitchAnimationAction.animRevert();
            } else return false;
            return true;
        }
    };

    /*
     * 控制开关动画，如按钮触摸移动动画。
     * */
    private final OnTouchAnimationListener mSwitchAnimationAction = new OnTouchAnimationListener() {
        private float switchViewX;
        private boolean shouldHaptic;
        private float maxMoveX;
        private float minMoveX;
        private boolean isMoved;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isAnimationShowing) return true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoved = false;
                    int[] outLocation = new int[2];
                    getLocationInWindow(outLocation);
                    switchViewX = outLocation[0];
                    maxMoveX = getWidth() - mThumbView.getWidth() - dp2px(getContext(), ANIMATION_START_END_OFFSET);
                    minMoveX = dp2px(getContext(), ANIMATION_START_END_OFFSET);
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    animZoom();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isMoved = true;
                    float moveX = event.getRawX() - switchViewX;
                    if (moveX >= maxMoveX) {
                        moveX = maxMoveX;
                        hapticFeedbackIfNeed(v);
                    } else if (moveX <= minMoveX) {
                        moveX = minMoveX;
                        hapticFeedbackIfNeed(v);
                    } else if (moveX > minMoveX && moveX < maxMoveX) {
                        shouldHaptic = true;
                    }
                    v.setX(moveX);
                    break;
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL:
                    animRevert();
                    if (isMoved) {
                        float finalX = v.getX();
                        boolean newCheckedState;
                        if (finalX < (minMoveX + maxMoveX) / 2) {
                            finalX = minMoveX;
                            newCheckedState = false;
                        } else {
                            finalX = maxMoveX;
                            newCheckedState = true;
                        }
                        mThumbViewAnimator.x(finalX)
                            .setInterpolator(new AnticipateOvershootInterpolator(ANIMATION_TENSION))
                            .setDuration(ANIMATION_DURATION);
                        if (newCheckedState != isChecked()) {
                            mClickListener.onClick(null);
                        } else mThumbViewAnimator.start();
                    } else
                        mClickListener.onClick(v);

                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void hapticFeedbackIfNeed(View v) {
            if (shouldHaptic)
                v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            shouldHaptic = false;
        }

        public void animZoom() {
            mThumbViewAnimator.scaleX(1.1f).scaleY(1.1f);
        }

        public void animRevert() {
            mThumbViewAnimator.scaleX(1f).scaleY(1f);
        }
    };

    public MiuiSwitch(@NonNull Context context) {
        this(context, null);
    }

    public MiuiSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setId(R.id.checkbox_container);
        ConstraintLayout.LayoutParams params = new LayoutParams(
            MiuiXUtils.dp2px(getContext(), 49),
            MiuiXUtils.dp2px(getContext(), 28)
        );
        setLayoutParams(params);
        setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.switch_background_off));

        View view = new View(getContext());
        view.setId(R.id.switch_thumb);
        params = new LayoutParams(
            MiuiXUtils.dp2px(getContext(), 20f),
            MiuiXUtils.dp2px(getContext(), 20f)
        );
        params.setMargins(
            MiuiXUtils.dp2px(getContext(), 4.2f),
            MiuiXUtils.dp2px(getContext(), 4.2f),
            MiuiXUtils.dp2px(getContext(), 4.2f),
            MiuiXUtils.dp2px(getContext(), 4.2f)
        );
        view.setLayoutParams(params);
        view.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.thumb_background));
        addView(view);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        constraintSet.connect(view.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(view.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        constraintSet.applyTo(this);

        mThumbView = view;
        mThumbViewAnimator = mThumbView.animate();
        Drawable[] switchOffToOnDrawables = MiuiXUtils.getDrawables(
            getContext(),
            R.drawable.switch_background_off,
            R.drawable.switch_background_on
        );
        Drawable[] switchOnToOffDrawables = MiuiXUtils.getDrawables(
            getContext(),
            R.drawable.switch_background_on,
            R.drawable.switch_background_off
        );
        offToOnTransition = new TransitionDrawable(switchOffToOnDrawables);
        onToOffTransition = new TransitionDrawable(switchOnToOffDrawables);

        initListener();
    }

    private void initListener() {
        setOnClickListener(mClickListener);
        mThumbView.setOnTouchListener(mSwitchAnimationAction);
        mThumbView.setOnHoverListener(mHoverListener);
    }

    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public void setChecked(boolean checked, boolean show) {
        final boolean changed = isChecked != checked;
        if (changed) {
            isChecked = checked;
            if (isAnimationShowing)
                animationHandler.sendMessage(
                    animationHandler.obtainMessage(
                        ANIMATION_NEXT,
                        new Object[]{isChecked, show}
                    )
                );
            else
                showThumbAnimationIfNeed(show, isChecked);
        }
    }

    public boolean isAnimationShowing() {
        return isAnimationShowing;
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (mThumbView == null) return;
        mThumbView.setEnabled(enabled);
        updateSwitchState(false);
    }

    public void setOnSwitchStateChangeListener(OnSwitchStateChangeListener onSwitchStateChangeListener) {
        mOnSwitchStateChangeListener = onSwitchStateChangeListener;
    }

    private void showThumbAnimationIfNeed(boolean show, boolean toRight) {
        if (isAnimationShowing) return;
        isAnimationShowing = true;

        updateSwitchState(show);
        int translationX = dp2px(getContext(), THUMB_END_X);
        if (!show) {
            if (toRight) mThumbView.setTranslationX(translationX);
            else mThumbView.setTranslationX(0);
            isAnimationShowing = false;
            return;
        }
        int thumbPosition = toRight ? translationX : 0;

        mThumbViewAnimator
            .translationX(thumbPosition)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(new AnticipateOvershootInterpolator(ANIMATION_TENSION))
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationHandler.sendEmptyMessage(ANIMATION_DONE);
                    isAnimationShowing = false;
                }
            })
            .start();
    }

    private void updateSwitchState(boolean useAnimation) {
        if (isEnabled()) {
            if (useAnimation) {
                if (isChecked()) {
                    setBackground(offToOnTransition);
                    offToOnTransition.startTransition(ANIMATION_DURATION);
                } else {
                    setBackground(onToOffTransition);
                    onToOffTransition.startTransition(ANIMATION_DURATION);
                }
            } else {
                setBackgroundResource(isChecked() ?
                    R.drawable.switch_background_on :
                    R.drawable.switch_background_off);
            }
            mThumbView.setBackgroundResource(R.drawable.thumb_background);
        } else {
            if (isChecked()) {
                setBackgroundResource(R.drawable.switch_background_disable_on);
                mThumbView.setBackgroundResource(R.drawable.thumb_disable_on_background);
            } else {
                setBackgroundResource(R.drawable.switch_background_disable_off);
                mThumbView.setBackgroundResource(R.drawable.thumb_disable_off_background);
            }
        }
    }

    public interface OnSwitchStateChangeListener {
        boolean onSwitchStateChange(boolean newValue);
    }

    private interface OnTouchAnimationListener extends View.OnTouchListener {
        @Override
        boolean onTouch(View v, MotionEvent event);

        void animZoom();

        void animRevert();
    }
}
