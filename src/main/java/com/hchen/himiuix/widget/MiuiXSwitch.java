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
package com.hchen.himiuix.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
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

public class MiuiXSwitch extends ConstraintLayout {
    private static final String TAG = "MiuiPreference";
    private static final int ANIMATION_NEXT = 0;
    private static final int ANIMATION_DONE = 1;
    private final int ANIMATION_DURATION = 320;
    private final float ANIMATION_TENSION = 1.2f;
    private int THUMB_MARGINS;
    private View thumbView;
    private ViewPropertyAnimator thumbViewAnimator;
    private OnSwitchStateChangeListener onSwitchStateChangeListener;
    private boolean isChecked = false;
    private boolean isAnimationShowing = false;
    private TransitionDrawable offToOnTransition;
    private TransitionDrawable onToOffTransition;
    /*
     * 控制开关动画，如按钮触摸移动动画。
     * */
    private final OnTouchAnimationListener switchAnimationAction = new OnTouchAnimationListener() {
        private float switchViewX;
        private boolean shouldHaptic;
        private float maxMoveX;
        private float minMoveX;
        private boolean isMoved;

        @Override
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            if (isAnimationShowing) return true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoved = false;
                    int[] outLocation = new int[2];
                    getLocationInWindow(outLocation);
                    switchViewX = outLocation[0];
                    maxMoveX = getWidth() - thumbView.getWidth() - THUMB_MARGINS;
                    minMoveX = THUMB_MARGINS;
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
                        thumbViewAnimator.x(finalX)
                            .setInterpolator(new AnticipateOvershootInterpolator(ANIMATION_TENSION))
                            .setDuration(ANIMATION_DURATION);
                        if (newCheckedState != isChecked()) {
                            clickListener.onClick(null);
                        } else thumbViewAnimator.start();
                    } else
                        clickListener.onClick(v);

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
            thumbViewAnimator.scaleX(1.1f).scaleY(1.1f);
        }

        public void animRevert() {
            thumbViewAnimator.scaleX(1f).scaleY(1f);
        }
    };

    /*
     * 监听鼠标动作
     * */
    private final View.OnHoverListener hoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (isAnimationShowing) return true;

            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                switchAnimationAction.animZoom();
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                switchAnimationAction.animRevert();
            } else return false;
            return true;
        }
    };

    private final Handler animationHandler = new Handler(Looper.getMainLooper()) {
        private boolean shouldShowNextAnimation = false;
        private boolean nextValue = false;
        private boolean showAnimation = false;

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case ANIMATION_NEXT -> {
                    shouldShowNextAnimation = true;
                    Object[] objs = (Object[]) msg.obj;
                    nextValue = (boolean) objs[0];
                    showAnimation = (boolean) objs[1];
                }
                case ANIMATION_DONE -> {
                    if (shouldShowNextAnimation) {
                        showThumbAnimationIfNeed(nextValue, showAnimation);
                        shouldShowNextAnimation = false;
                    }
                }
            }
        }
    };

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAnimationShowing)
                return;

            final boolean newValue = !isChecked();
            if (onSwitchStateChangeListener == null)
                setChecked(newValue);
            else if (onSwitchStateChangeListener.onSwitchStateChange(newValue)) {
                setChecked(newValue);
            }

            if (v != null)
                v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    };

    public MiuiXSwitch(@NonNull Context context) {
        this(context, null);
    }

    public MiuiXSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ConstraintLayout.LayoutParams params = new LayoutParams(
            MiuiXUtils.dp2px(getContext(), 49),
            MiuiXUtils.dp2px(getContext(), 28)
        );
        setLayoutParams(params);
        setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.switch_background_off));

        View view = new View(getContext());
        view.setId(R.id.switch_thumb);

        THUMB_MARGINS = MiuiXUtils.dp2px(getContext(), 4.2f);
        params = new LayoutParams(
            MiuiXUtils.dp2px(getContext(), 20f),
            MiuiXUtils.dp2px(getContext(), 20f)
        );
        params.setMargins(
            THUMB_MARGINS,
            THUMB_MARGINS,
            THUMB_MARGINS,
            THUMB_MARGINS
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

        thumbView = view;
        thumbViewAnimator = thumbView.animate();
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
        setOnClickListener(clickListener);
        thumbView.setOnTouchListener(switchAnimationAction);
        thumbView.setOnHoverListener(hoverListener);
    }

    public boolean isAnimationShowing() {
        return isAnimationShowing;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public void setChecked(boolean checked, boolean showAnimation) {
        final boolean changed = isChecked != checked;
        if (changed) {
            isChecked = checked;
            if (isAnimationShowing)
                animationHandler.sendMessage(
                    animationHandler.obtainMessage(
                        ANIMATION_NEXT,
                        new Object[]{isChecked, showAnimation}
                    )
                );
            else
                showThumbAnimationIfNeed(isChecked, showAnimation);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (thumbView == null) return;
        thumbView.setEnabled(enabled);
        updateSwitchState(false);
    }

    public void setOnSwitchStateChangeListener(OnSwitchStateChangeListener onSwitchStateChangeListener) {
        this.onSwitchStateChangeListener = onSwitchStateChangeListener;
    }

    private void showThumbAnimationIfNeed(boolean toRight, boolean showAnimation) {
        if (isAnimationShowing) return;
        isAnimationShowing = true;

        updateSwitchState(showAnimation);
        int translationX = getWidth() - thumbView.getWidth() - (THUMB_MARGINS * 2);
        if (!showAnimation) {
            thumbView.post(() -> {
                if (toRight)
                    thumbView.setTranslationX(getWidth() - thumbView.getWidth() - (THUMB_MARGINS * 2));
                else thumbView.setTranslationX(0);
                isAnimationShowing = false;
            });
            return;
        }
        int thumbPosition = toRight ? translationX : 0;

        thumbViewAnimator
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
                setBackgroundResource(
                    isChecked() ?
                        R.drawable.switch_background_on :
                        R.drawable.switch_background_off
                );
            }
            thumbView.setBackgroundResource(R.drawable.thumb_background);
        } else {
            if (isChecked()) {
                setBackgroundResource(R.drawable.switch_background_disable_on);
                thumbView.setBackgroundResource(R.drawable.thumb_disable_on_background);
            } else {
                setBackgroundResource(R.drawable.switch_background_disable_off);
                thumbView.setBackgroundResource(R.drawable.thumb_disable_off_background);
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
