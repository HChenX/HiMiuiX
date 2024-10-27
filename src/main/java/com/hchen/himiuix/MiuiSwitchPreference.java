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
package com.hchen.himiuix;

import static com.hchen.himiuix.MiuiXUtils.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

public class MiuiSwitchPreference extends MiuiPreference {
    private ConstraintLayout mSwitchBackgroundLayout;
    private View mThumbView;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mChecked;
    private boolean mDisableDependentsState;
    private boolean isInitialTime = true;
    private ViewPropertyAnimator mThumbViewAnimator;
    private final int ANIMATOR_DURATION = 320;
    private final float ANIMATOR_TENSION = 1.2f;
    private final float ANIMATION_START_END_OFFSET = 4.2f;
    private final float THUMB_END_X = 22.8f;
    private TransitionDrawable offToOnTransition;
    private TransitionDrawable onToOffTransition;
    private static final int HANDLER_ANIMATION_READY = 0;
    private static final int HANDLER_ANIMATION_START = 1;
    private static final int HANDLED_ANIMATION_DOING = 2;
    private static final int HANDLED_ANIMATION_NEXT = 3;
    private static final int HANDLED_ANIMATION_DONE = 4;
    private static final int HANDLER_NO_ANIMATION = 5;
    private final Handler animationHandler = new Handler(Looper.getMainLooper()) {
        private boolean shouldNextAnimation = false;
        private boolean nextValue = false;

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean newValue = msg.obj instanceof Boolean && (boolean) msg.obj;
            switch (msg.what) {
                case HANDLER_ANIMATION_READY -> {
                }
                case HANDLER_ANIMATION_START -> {
                    animationHandler.removeMessages(HANDLER_ANIMATION_READY);
                    animateThumbIfNeed(true, newValue);
                }
                case HANDLED_ANIMATION_DOING -> {
                    animationHandler.sendEmptyMessage(HANDLED_ANIMATION_DONE);
                }
                case HANDLED_ANIMATION_NEXT -> {
                    assert msg.obj instanceof Object[];
                    Object[] objs = (Object[]) msg.obj;
                    shouldNextAnimation = (boolean) objs[0];
                    nextValue = (boolean) objs[1];
                }
                case HANDLED_ANIMATION_DONE -> {
                    if (shouldNextAnimation)
                        animationHandler.sendMessage(SwitchStateMessage.create(HANDLER_ANIMATION_START, nextValue));
                    shouldNextAnimation = false;
                }
                case HANDLER_NO_ANIMATION -> {
                    animateThumbIfNeed(false, newValue);
                }
            }
        }
    };
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (animationHandler.hasMessages(HANDLED_ANIMATION_DOING))
                return;
            animationHandler.sendEmptyMessageDelayed(HANDLER_ANIMATION_READY, ANIMATOR_DURATION);
            final boolean newValue = !isChecked();
            if (callChangeListener(newValue)) {
                innerSetChecked(newValue, true);
                animationHandler.sendMessage(SwitchStateMessage.create(HANDLER_ANIMATION_START, newValue));
            } else animationHandler.removeMessages(HANDLER_ANIMATION_READY);
            if (v != null)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
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
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoved = false;
                    int[] outLocation = new int[2];
                    mSwitchBackgroundLayout.getLocationInWindow(outLocation);
                    switchViewX = outLocation[0];
                    maxMoveX = mSwitchBackgroundLayout.getWidth() - mThumbView.getWidth() - dp2px(getContext(), ANIMATION_START_END_OFFSET);
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
                                .setInterpolator(new AnticipateOvershootInterpolator(ANIMATOR_TENSION))
                                .setDuration(ANIMATOR_DURATION);
                        if (newCheckedState != isChecked()) {
                            mClickListener.onClick(null);
                        } else mThumbViewAnimator.start();
                    } else
                        mClickListener.onClick(v);
                    v.performClick();
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void hapticFeedbackIfNeed(View v) {
            if (shouldHaptic)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            shouldHaptic = false;
        }

        public void animZoom() {
            mThumbViewAnimator.scaleX(1.1f).scaleY(1.1f);
        }

        public void animRevert() {
            mThumbViewAnimator.scaleX(1f).scaleY(1f);
        }
    };

    public MiuiSwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    @SuppressLint("RestrictedApi")
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_switch);
        Drawable[] switchOffToOnDrawables = MiuiXUtils.getDrawables(getContext(), R.drawable.switch_background_off, R.drawable.switch_background_on);
        Drawable[] switchOnToOffDrawables = MiuiXUtils.getDrawables(getContext(), R.drawable.switch_background_on, R.drawable.switch_background_off);
        offToOnTransition = new TransitionDrawable(switchOffToOnDrawables);
        onToOffTransition = new TransitionDrawable(switchOnToOffDrawables);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiSwitchPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOn, R.styleable.MiuiSwitchPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOff, R.styleable.MiuiSwitchPreference_android_summaryOff);
            mDisableDependentsState = TypedArrayUtils.getBoolean(array, R.styleable.MiuiSwitchPreference_disableDependentsState,
                    R.styleable.MiuiSwitchPreference_android_disableDependentsState, false);
        }
    }

    public void setSummaryOn(CharSequence mSummaryOn) {
        this.mSummaryOn = mSummaryOn;
        notifyChanged();
    }

    public void setSummaryOn(@StringRes int mSummaryOn) {
        setSummaryOn(getContext().getString(mSummaryOn));
    }

    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOff(CharSequence mSummaryOff) {
        this.mSummaryOff = mSummaryOff;
        notifyChanged();
    }

    public void setSummaryOff(@StringRes int mSummaryOff) {
        setSummaryOff(getContext().getString(mSummaryOff));
    }

    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        innerSetChecked(checked, false);
    }

    private void innerSetChecked(boolean checked, boolean fromUser) {
        final boolean changed = mChecked != checked;
        if (changed || isInitialTime) {
            mChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());
            if (!isInitialTime) {
                if (!fromUser && (animationHandler.hasMessages(HANDLED_ANIMATION_DOING)
                        || animationHandler.hasMessages(HANDLER_ANIMATION_READY))) {
                    animationHandler.sendMessage(
                            SwitchStateMessage.create(HANDLED_ANIMATION_NEXT, new Object[]{true, checked}));
                }
                notifyChanged();
            }
        }
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        mDisableDependentsState = disableDependentsState;
        notifyChanged();
    }

    public boolean getDisableDependentsState() {
        return mDisableDependentsState;
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = mDisableDependentsState == mChecked;
        return shouldDisable || super.shouldDisableDependents();
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = false;
        innerSetChecked(getPersistedBoolean((Boolean) defaultValue), false);
        isInitialTime = false;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        isInitialTime = false;
        mSwitchBackgroundLayout = (ConstraintLayout) holder.findViewById(R.id.switch_container);
        mThumbView = holder.findViewById(R.id.switch_thumb);

        mThumbViewAnimator = mThumbView.animate();
        mThumbView.setOnTouchListener(null);
        mThumbView.setOnHoverListener(null);
        mSwitchBackgroundLayout.setOnClickListener(null);
        animationHandler.sendMessage(SwitchStateMessage.create(HANDLER_NO_ANIMATION, isChecked()));

        updateSummaryIfNeed();
        if (isEnabled() && isSelectable()) {
            mSwitchBackgroundLayout.setOnClickListener(mClickListener);
            mThumbView.setOnHoverListener(mHoverListener);
            mThumbView.setOnTouchListener(mSwitchAnimationAction);
        }
    }

    @Override
    protected boolean shouldShowSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
    }

    @Override
    protected void onMainLayoutClick(View view) {
        mClickListener.onClick(view);
    }

    @Override
    protected boolean onMainLayoutTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    protected boolean onMainLayoutHover(View v, MotionEvent event) {
        return super.onMainLayoutHover(v, event);
    }

    private void updateSummaryIfNeed() {
        if (shouldShowSummary()) {
            if (mSummaryOn == null && mSummaryOff == null) getSummaryView().setText(getSummary());
            else if (mSummaryOn != null && mSummaryOff == null) {
                if (isChecked()) getSummaryView().setText(mSummaryOn);
                else getSummaryView().setText(getSummary());
            } else if (mSummaryOn == null) {
                if (isChecked()) getSummaryView().setText(getSummary());
                else getSummaryView().setText(mSummaryOff);
            } else {
                if (isChecked()) getSummaryView().setText(mSummaryOn);
                else getSummaryView().setText(mSummaryOff);
            }
        }
    }

    private void animateThumbIfNeed(boolean useAnimation, boolean toRight) {
        if (animationHandler.hasMessages(HANDLED_ANIMATION_DOING))
            return;
        updateSwitchState(useAnimation);
        int translationX = dp2px(getContext(), THUMB_END_X);
        if (!useAnimation) {
            if (toRight) mThumbView.setTranslationX(translationX);
            else mThumbView.setTranslationX(0);
            return;
        }
        int thumbPosition = toRight ? translationX : 0;

        mThumbViewAnimator
                .translationX(thumbPosition)
                .setDuration(ANIMATOR_DURATION)
                .setInterpolator(new AnticipateOvershootInterpolator(ANIMATOR_TENSION))
                .start();
        animationHandler.sendEmptyMessageDelayed(HANDLED_ANIMATION_DOING, ANIMATOR_DURATION);
    }

    private void updateSwitchState(boolean useAnimation) {
        if (isEnabled()) {
            if (useAnimation) {
                if (isChecked()) {
                    mSwitchBackgroundLayout.setBackground(offToOnTransition);
                    offToOnTransition.startTransition(ANIMATOR_DURATION);
                } else {
                    mSwitchBackgroundLayout.setBackground(onToOffTransition);
                    onToOffTransition.startTransition(ANIMATOR_DURATION);
                }
            } else {
                mSwitchBackgroundLayout.setBackgroundResource(isChecked() ?
                        R.drawable.switch_background_on :
                        R.drawable.switch_background_off);
            }
            mThumbView.setBackgroundResource(R.drawable.thumb_background);
        } else {
            if (isChecked()) {
                mSwitchBackgroundLayout.setBackgroundResource(R.drawable.switch_background_disable_on);
                mThumbView.setBackgroundResource(R.drawable.thumb_disable_on_background);
            } else {
                mSwitchBackgroundLayout.setBackgroundResource(R.drawable.switch_background_disable_off);
                mThumbView.setBackgroundResource(R.drawable.thumb_disable_off_background);
            }
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.mChecked = isChecked();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        innerSetChecked(savedState.mChecked, false);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        boolean mChecked;

        public SavedState(Parcel source) {
            super(source);
            mChecked = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mChecked ? 1 : 0);
        }
    }

    private static class SwitchStateMessage {
        public static Message create(int what, Object value) {
            Message message = Message.obtain();
            message.what = what;
            message.obj = value;
            return message;
        }
    }

    private interface OnTouchAnimationListener extends View.OnTouchListener {
        @Override
        boolean onTouch(View v, MotionEvent event);

        void animZoom();

        void animRevert();
    }
}
