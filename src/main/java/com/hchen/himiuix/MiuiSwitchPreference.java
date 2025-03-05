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

import com.hchen.himiuix.widget.MiuiSwitch;

public class MiuiSwitchPreference extends MiuiPreference {
    private MiuiSwitch mMiuiSwitch;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean isChecked;
    private boolean mDisableDependentsState;
    private boolean isInitialState = true;
    private final MiuiSwitch.OnSwitchStateChangeListener mOnSwitchStateChangeListener = newValue -> {
        boolean result = callChangeListener(newValue);
        if (result)
            setChecked(newValue, true);

        return result;
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

    @SuppressLint("RestrictedApi")
    public MiuiSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.miuix_switch);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiSwitchPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOn, R.styleable.MiuiSwitchPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiSwitchPreference_summaryOff, R.styleable.MiuiSwitchPreference_android_summaryOff);
            mDisableDependentsState = TypedArrayUtils.getBoolean(array, R.styleable.MiuiSwitchPreference_disableDependentsState,
                R.styleable.MiuiSwitchPreference_android_disableDependentsState, false);
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    private void setChecked(boolean checked, boolean fromUser) {
        final boolean changed = isChecked != checked;
        if (changed || isInitialState) {
            isChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());
            if (!isInitialState) {
                if (!fromUser && mMiuiSwitch != null) {
                    mMiuiSwitch.setChecked(isChecked, false);
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
        boolean shouldDisable = mDisableDependentsState == isChecked;
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
        setChecked(getPersistedBoolean((Boolean) defaultValue), false);
        isInitialState = false;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        isInitialState = false;
        super.onBindViewHolder(holder);

        mMiuiSwitch = holder.itemView.findViewById(R.id.checkbox_container);
        mMiuiSwitch.setOnSwitchStateChangeListener(mOnSwitchStateChangeListener);
        mMiuiSwitch.setChecked(isChecked, false);
        updateSummaryIfNeed();
    }

    @Override
    boolean shouldShowSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
    }

    @Override
    protected void onClick(View view) {
        if (mMiuiSwitch == null) return;
        if (mMiuiSwitch.isAnimationShowing()) return;

        if (callChangeListener(!isChecked())) {
            mMiuiSwitch.setChecked(!isChecked());
            setChecked(!isChecked(), true);

            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
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
        setChecked(savedState.isChecked, false);
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
