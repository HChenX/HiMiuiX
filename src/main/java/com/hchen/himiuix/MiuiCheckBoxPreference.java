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
package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.widget.MiuiXCheckBox;
import com.hchen.himiuix.widget.OnCheckStateChangeListener;

public class MiuiCheckBoxPreference extends MiuiPreference {
    private MiuiXCheckBox mMiuiXCheckBox;
    private boolean isChecked = false;
    private boolean isInitialState = true;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mDisableDependentsState;
    private int mButtonLocation;
    private final OnCheckStateChangeListener mOnCheckStateChangeListener = (button, newCheck) -> {
        if (callChangeListener(newCheck)) {
            setChecked(newCheck);
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

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiCheckBoxPreference, defStyleAttr, defStyleRes)) {
            mSummaryOn = TypedArrayUtils.getString(array, R.styleable.MiuiCheckBoxPreference_summaryOn, R.styleable.MiuiCheckBoxPreference_android_summaryOn);
            mSummaryOff = TypedArrayUtils.getString(array, R.styleable.MiuiCheckBoxPreference_summaryOff, R.styleable.MiuiCheckBoxPreference_android_summaryOff);
            mDisableDependentsState = TypedArrayUtils.getBoolean(array, R.styleable.MiuiCheckBoxPreference_disableDependentsState,
                R.styleable.MiuiCheckBoxPreference_android_disableDependentsState, false);
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

        mMiuiXCheckBox = holder.itemView.findViewById(R.id.checkbox_container);
        mMiuiXCheckBox.setOnCheckStateChangeListener(null);
        mMiuiXCheckBox.setChecked(isChecked);

        if (isEnabled()) {
            mMiuiXCheckBox.setOnCheckStateChangeListener(mOnCheckStateChangeListener);
        }
    }

    @Override
    protected void onClick() {
        if (mMiuiXCheckBox == null) return;

        mMiuiXCheckBox.performClick();
    }

    @Override
    boolean shouldShowSummary() {
        return getSummary() != null || mSummaryOn != null || mSummaryOff != null;
    }

    @Override
    CharSequence getSummaryText() {
        if (mSummaryOn == null && mSummaryOff == null) return getSummary();
        else if (mSummaryOn != null && mSummaryOff == null) {
            if (isChecked()) return mSummaryOn;
            else return getSummary();
        } else if (mSummaryOn == null && mSummaryOff != null) {
            if (isChecked()) return getSummary();
            else return mSummaryOff;
        } else {
            if (isChecked()) return mSummaryOn;
            else return mSummaryOff;
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
