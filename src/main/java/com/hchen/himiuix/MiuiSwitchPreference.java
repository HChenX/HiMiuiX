package com.hchen.himiuix;

import static com.hchen.himiuix.MiuiXUtils.sp2px;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.TransitionDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

public class MiuiSwitchPreference extends MiuiPreference {
    private ConstraintLayout switchBackgroundLayout;
    private View thumbView;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mChecked;
    private boolean mDisableDependentsState;
    private boolean isInitialTime = true;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (animating) return;
            final boolean newValue = !isChecked();
            if (callChangeListener(newValue)) {
                setChecked(newValue);
                updateSwitchState(false);
                animateThumbIfNeed(true, isChecked());
                if (mSummaryOn != null && isChecked()) getSummaryView().setText(mSummaryOn);
                if (mSummaryOff != null && !isChecked()) getSummaryView().setText(mSummaryOff);
            }
            if (v != null)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    };
    private final View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                onTouchListener.animZoom(v);
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                onTouchListener.animRevert(v);
            } else return false;
            return true;
        }
    };
    private final OnCustomTouchListener onTouchListener = new OnCustomTouchListener() {
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
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    int[] outLocation = new int[2];
                    switchBackgroundLayout.getLocationOnScreen(outLocation);
                    switchViewX = outLocation[0];
                    maxMoveX = switchBackgroundLayout.getWidth() - thumbView.getWidth() - MiuiXUtils.sp2px(getContext(), 4.5F);
                    minMoveX = MiuiXUtils.sp2px(getContext(), 4.5F);
                    animZoom(v);
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
                        v.animate().x(finalX)
                                .setInterpolator(new OvershootInterpolator(1.0f))
                                .setDuration(200).start();
                        if (newCheckedState != isChecked()) {
                            onClickListener.onClick(null);
                        }
                    } else
                        onClickListener.onClick(v);
                    animRevert(v);
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

        public void animZoom(View view) {
            view.animate().scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start();
        }

        public void animRevert(View view) {
            view.animate().scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
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
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_switch);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiSwitchPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOn, R.styleable.MiuiSwitchPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOff, R.styleable.MiuiSwitchPreference_android_summaryOff);
            mDisableDependentsState = TypedArrayUtils.getBoolean(array, R.styleable.MiuiSwitchPreference_disableDependentsState,
                    R.styleable.MiuiSwitchPreference_android_disableDependentsState, false);
        }
    }

    public void setSummaryOn(CharSequence mSummaryOn) {
        this.mSummaryOn = mSummaryOn;
    }

    public void setSummaryOn(@StringRes int mSummaryOn) {
        setSummaryOn(getContext().getString(mSummaryOn));
    }

    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOff(CharSequence mSummaryOff) {
        this.mSummaryOff = mSummaryOff;
    }

    public void setSummaryOff(@StringRes int mSummaryOff) {
        setSummaryOff(getContext().getString(mSummaryOff));
    }

    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }

    @Override
    protected void onClick(View view) {
        onClickListener.onClick(view);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        final boolean changed = mChecked != checked;
        if (changed) {
            mChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());
            if (!isInitialTime) {
                notifyChanged();
            }
        }
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        mDisableDependentsState = disableDependentsState;
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
        setChecked(getPersistedBoolean((Boolean) defaultValue));
        isInitialTime = false;
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
        setChecked(savedState.mChecked);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        switchBackgroundLayout = (ConstraintLayout) holder.findViewById(R.id.switch_container);
        thumbView = holder.findViewById(R.id.switch_thumb);
        thumbView.setOnTouchListener(null);
        thumbView.setOnHoverListener(null);

        switchBackgroundLayout.setOnClickListener(null);
        updateSwitchState(true);
        animateThumbIfNeed(false, isChecked());

        if (shouldShowSummary()) {
            if (mSummaryOff == null && mSummaryOn == null)
                getSummaryView().setText(getSummary()); // on 与 off 应当成对出现
            else {
                if (mSummaryOn != null && isChecked()) getSummaryView().setText(mSummaryOn);
                if (mSummaryOff != null && !isChecked()) getSummaryView().setText(mSummaryOff);
            }
        }
        if (isEnabled() && isSelectable()) {
            switchBackgroundLayout.setOnClickListener(onClickListener);
            thumbView.setOnHoverListener(onHoverListener);
            thumbView.setOnTouchListener(onTouchListener);
        }
    }

    @Override
    protected boolean shouldShowSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        return super.onHover(v, event);
    }

    private void updateSwitchState(boolean isInit) {
        if (isEnabled()) {
            if (isInit) {
                switchBackgroundLayout.setBackgroundResource(isChecked() ?
                        R.drawable.switch_background_on :
                        R.drawable.switch_background_off);
            } else {
                switchBackgroundLayout.setBackgroundResource(R.drawable.switch_transition_background);
                TransitionDrawable transitionDrawable = (TransitionDrawable) switchBackgroundLayout.getBackground();
                if (isChecked()) {
                    transitionDrawable.startTransition(200);  // 渐变到 on 状态
                } else {
                    transitionDrawable.resetTransition();  // 渐变到 off 状态
                }
            }
            thumbView.setBackgroundResource(R.drawable.thumb_background);
        } else {
            if (isChecked()) {
                switchBackgroundLayout.setBackgroundResource(R.drawable.switch_background_disable_on);
                thumbView.setBackgroundResource(R.drawable.thumb_disable_on_background);
            } else {
                switchBackgroundLayout.setBackgroundResource(R.drawable.switch_background_disable_off);
                thumbView.setBackgroundResource(R.drawable.thumb_disable_off_background);
            }
        }
    }

    private boolean animating = false;

    private void animateThumbIfNeed(boolean useAnimate, boolean toRight) {
        if (animating) return;
        int translationX = switchBackgroundLayout.getWidth() - thumbView.getWidth() - (2 * sp2px(getContext(), 4F));
        if (!useAnimate) {
            if (toRight) thumbView.setTranslationX(sp2px(getContext(), 23.3F));
            else thumbView.setTranslationX(0);
            return;
        }
        animating = true;
        int thumbPosition = toRight ? translationX : 0;

        thumbView.animate()
                .translationX(thumbPosition)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animating = false;
                    }
                });
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

    private interface OnCustomTouchListener extends View.OnTouchListener {
        @Override
        boolean onTouch(View v, MotionEvent event);

        void animZoom(View view);

        void animRevert(View view);
    }
}
