package com.hchen.himiuix;

import static com.hchen.himiuix.MiuiXUtils.sp2px;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

public class MiuiSwitchPreference extends MiuiPreference {
    String TAG = "MiuiSwitchPreference";
    private ConstraintLayout switchBackground;
    private View thumb;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mChecked;
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (animating) return;
            setChecked(!isChecked());
            updateSwitchState();
            animateThumbIfNeed(true, isChecked());
            if (mSummaryOn != null && isChecked()) summary.setText(mSummaryOn);
            if (mSummaryOff != null && !isChecked()) summary.setText(mSummaryOff);
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    };

    public MiuiSwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_switch);
        this.context = context;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiSwitchPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOn, R.styleable.MiuiSwitchPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOff, R.styleable.MiuiSwitchPreference_android_summaryOff);
        }
    }

    public void setSummaryOn(CharSequence mSummaryOn) {
        this.mSummaryOn = mSummaryOn;
    }

    public void setSummaryOn(@StringRes int mSummaryOn) {
        setSummaryOn(context.getString(mSummaryOn));
    }

    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOff(CharSequence mSummaryOff) {
        this.mSummaryOff = mSummaryOff;
    }

    public void setSummaryOff(@StringRes int mSummaryOff) {
        setSummaryOff(context.getString(mSummaryOff));
    }

    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }

    @Override
    protected void onClick(View view) {
        clickListener.onClick(view);
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
            notifyChanged();
        }
    }

    @Override
    public boolean shouldDisableDependents() {
        return super.shouldDisableDependents();
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue == null) defaultValue = false;
        setChecked(getPersistedBoolean((Boolean) defaultValue));
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

        switchBackground = (ConstraintLayout) holder.findViewById(R.id.switch_container);
        thumb = holder.findViewById(R.id.switch_thumb);

        switchBackground.setOnClickListener(null);
        updateSwitchState();
        animateThumbIfNeed(false, isChecked());

        if (needSetSummary()) {
            if (mSummaryOff == null && mSummaryOn == null)
                summary.setText(getSummary()); // on 与 off 应当成对出现
            else {
                if (mSummaryOn != null && isChecked()) summary.setText(mSummaryOn);
                if (mSummaryOff != null && !isChecked()) summary.setText(mSummaryOff);
            }
        }
        if (isEnabled() && isSelectable())
            switchBackground.setOnClickListener(clickListener);
    }

    @Override
    protected boolean needSetSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private void updateSwitchState() {
        if (isEnabled()) {
            switchBackground.setBackgroundResource(isChecked() ?
                    R.drawable.switch_background_on :
                    R.drawable.switch_background_off);
            thumb.setBackgroundResource(R.drawable.thumb_background);
        } else {
            if (isChecked()) {
                switchBackground.setBackgroundResource(R.drawable.switch_background_disable_on);
                thumb.setBackgroundResource(R.drawable.thumb_disable_on_background);
            } else {
                switchBackground.setBackgroundResource(R.drawable.switch_background_disable_off);
                thumb.setBackgroundResource(R.drawable.thumb_disable_off_background);
            }
        }
    }

    private boolean animating = false;

    private void animateThumbIfNeed(boolean useAnimate, boolean toRight) {
        if (animating) return;
        int translationX = switchBackground.getWidth() - thumb.getWidth() - (2 * sp2px(context, 3.7F));
        if (!useAnimate) {
            if (toRight) thumb.setTranslationX(sp2px(context, 22.4F));
            else thumb.setTranslationX(0);
            return;
        }
        animating = true;
        int thumbPosition = toRight ? translationX : 0;

        ObjectAnimator animator = ObjectAnimator.ofFloat(thumb, "translationX", thumb.getTranslationX(), thumbPosition);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating = false;
                animator.removeListener(this);
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateSwitchState();
        animateThumbIfNeed(false, isChecked());
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
}
