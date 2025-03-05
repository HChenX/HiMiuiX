package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.widget.MiuiCheckBox;

public class MiuiCheckBoxPreference extends MiuiPreference {
    private MiuiCheckBox mMiuiCheckBox;
    private boolean isChecked = false;
    private boolean isInitialState = true;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mDisableDependentsState;
    private int mButtonLocation;
    private final MiuiCheckBox.OnCheckedStateChangeListener mOnCheckedStateChangeListener = (miuiCheckBox, newChecked) -> {
        if (callChangeListener(newChecked)) {
            setChecked(newChecked);
            getMainLayout().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            return true;
        }
        return false;
    };

    public MiuiCheckBoxPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiSwitchPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOn, R.styleable.MiuiSwitchPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOff, R.styleable.MiuiSwitchPreference_android_summaryOff);
            mDisableDependentsState = TypedArrayUtils.getBoolean(array, R.styleable.MiuiSwitchPreference_disableDependentsState,
                R.styleable.MiuiSwitchPreference_android_disableDependentsState, false);
        }

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiCheckBoxPreference, defStyleAttr, defStyleRes)) {
            mButtonLocation = array.getInt(R.styleable.MiuiCheckBoxPreference_buttonLocation, 1);
        }

        if (mButtonLocation == 0) {
            setLayoutResource(R.layout.miuix_checkbox_start);
        } else if (mButtonLocation == 1) {
            setLayoutResource(R.layout.miuix_checkbox_end);
        }
    }

    public void setSummaryOn(CharSequence summaryOn) {
        this.mSummaryOn = summaryOn;
        notifyChanged();
    }

    public void setSummaryOn(@StringRes int summaryOn) {
        setSummaryOn(getContext().getString(summaryOn));
    }

    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOff(CharSequence summaryOff) {
        this.mSummaryOff = summaryOff;
        notifyChanged();
    }

    public void setSummaryOff(@StringRes int summaryOff) {
        setSummaryOff(getContext().getString(summaryOff));
    }

    public CharSequence getSummaryOff() {
        return mSummaryOff;
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
        boolean shouldDisable = mDisableDependentsState == isChecked;
        return shouldDisable || super.shouldDisableDependents();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        final boolean changed = isChecked != checked;
        if (changed || isInitialState) {
            isChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());

            if (!isInitialState) {
                notifyChanged();
            }
        }
    }


    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        isInitialState = false;
        super.onBindViewHolder(holder);

        mMiuiCheckBox = holder.itemView.findViewById(R.id.checkbox_container);
        mMiuiCheckBox.setOnCheckedStateChangeListener(null);
        mMiuiCheckBox.setChecked(isChecked);

        if (isEnabled()) {
            mMiuiCheckBox.setOnCheckedStateChangeListener(mOnCheckedStateChangeListener);
        }
        updateSummaryIfNeed();
    }

    @Override
    protected void onClick(View view) {
        if (mMiuiCheckBox == null) return;

        mMiuiCheckBox.setChecked(!isChecked());
    }

    @Override
    boolean shouldShowSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
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
        isInitialState = false;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.isChecked = isChecked();
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
        setChecked(savedState.isChecked);
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

        boolean isChecked;

        public SavedState(Parcel source) {
            super(source);
            isChecked = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isChecked ? 1 : 0);
        }
    }
}
