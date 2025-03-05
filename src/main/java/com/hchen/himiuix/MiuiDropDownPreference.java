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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiDropDownPreference extends MiuiPreference {
    private MiuiAlertDialog mDialog;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence mDefValue;
    private boolean shouldShowOnSummary;
    private String mValue;
    private View mTouchView;
    private boolean isInitialTime = true;
    private final ArrayList<CharSequence> mEntriesList = new ArrayList<>();
    private MiuiAlertDialogFactory.MiuiAlertDialogDropDownFactory mDialogDropDownFactory;
    private final SparseBooleanArray mBooleanArray = new SparseBooleanArray();

    public MiuiDropDownPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint({"RestrictedApi", "PrivateResource"})
    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiDropDownPreference, defStyleAttr, defStyleRes)) {
            mEntries = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entries,
                R.styleable.MiuiDropDownPreference_android_entries);
            mEntryValues = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entryValues,
                R.styleable.MiuiDropDownPreference_android_entryValues);
            mDefValue = TypedArrayUtils.getString(array, R.styleable.MiuiDropDownPreference_defaultValue,
                R.styleable.MiuiDropDownPreference_android_defaultValue);
            shouldShowOnSummary = array.getBoolean(R.styleable.MiuiDropDownPreference_showOnSummary, false);
        }

        safeCheck();
        mEntriesList.addAll(Arrays.asList(mEntries));
        setDefaultValue(mDefValue);
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
        mEntriesList.clear();
        mEntriesList.addAll(Arrays.asList(entries));
        notifyChanged();
    }

    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
        notifyChanged();
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public void setValue(String value) {
        if (value == null) return;
        valueCheck(value);
        safeCheck();
        if (!value.equals(mValue)) {
            if (callChangeListener(value) || isInitialTime) {
                persistString(value);
                makeBooleanArray(value);
                mValue = value;
                if (shouldShowOnSummary)
                    setSummary(mEntriesList.get(Integer.parseInt(mValue)));
                if (!isInitialTime) {
                    notifyChanged();
                }
            }
        }
    }

    public String getValue() {
        return mValue;
    }

    @Nullable
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }

    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(mEntryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    protected void notifyChanged() {
        super.notifyChanged();
        if (mEntryValues.length != mEntries.length) return;
        if (mDialog != null && mDialogDropDownFactory.mListAdapter != null) {
            mDialogDropDownFactory.mBooleanArray = mBooleanArray;
            mDialogDropDownFactory.mListAdapter.notifyDataSetChanged();
        }
    }

    private void makeBooleanArray(@NonNull String mValue) {
        mBooleanArray.clear();
        for (int i = 0; i < mEntryValues.length; i++) {
            if (mEntryValues[i].equals(mValue)) {
                mBooleanArray.put(i, true);
                break;
            }
        }
    }

    // 安全检查
    private void safeCheck() {
        if (mEntries.length != mEntryValues.length) { // 元素数与索引数不相等
            throw new RuntimeException("MiuiDropDownPreference: The length of entries must be equal to the length of entryValues!");
        }
        if (!mEntryValues[0].equals("0")) { // 不是从零开始的索引
            throw new RuntimeException("MiuiDropDownPreference: EntryValues must start from scratch!");
        }
        for (int i = 0; i < mEntryValues.length; i++) { // 索引必须是连续的数字
            if (!mEntryValues[i].equals(Integer.toString(i))) {
                throw new RuntimeException("MiuiDropDownPreference: The entryValues must be continuous!");
            }
        }
    }

    private void valueCheck(String value) {
        for (CharSequence mEntryValue : mEntryValues) {
            if (mEntryValue.equals(value)) {
                return;
            }
        }
        throw new RuntimeException("MiuiDropDownPreference: The input value is not an existing set of available values! " +
            "Input: " + value + " Available values: " + Arrays.toString(mEntryValues));
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        getArrowRightView().setVisibility(View.VISIBLE);
        if (isEnabled()) {
            getArrowRightView().setImageDrawable(
                AppCompatResources.getDrawable(
                    getContext(),
                    R.drawable.ic_preference_arrow_up_down
                )
            );
        } else {
            getArrowRightView().setImageDrawable(
                AppCompatResources.getDrawable(
                    getContext(),
                    R.drawable.ic_preference_disable_arrow_up_down
                )
            );
        }
    }

    @Override
    boolean shouldDisableArrowRightView() {
        return true;
    }

    @Override
    boolean shouldShowSummary() {
        return getSummary() != null || shouldShowOnSummary;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
        isInitialTime = false;
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    boolean onMainLayoutTouch(View v, MotionEvent event) {
        if (!isEnabled()) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            updateBackground(R.color.touch_down);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            updateBackground(R.color.touch_up);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            safeCheck();
            if (mDialog != null && mDialog.isShowing()) return false;
            updateBackground(R.color.touch_down);
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            mTouchView = v;
            initDropDownDialog(event.getRawX(), event.getRawY());
        }
        return false;
    }

    private void initDropDownDialog(float x, float y) {
        mDialog = new MiuiAlertDialog(getContext(), true)
            .setHapticFeedbackEnabled(true)
            .setEnableMultiSelect(false)
            .setItems(mEntriesList, new DialogInterface.OnItemsClickListener() {
                @Override
                public void onClick(DialogInterface dialog, CharSequence item, int which) {
                    if (mDialogDropDownFactory.mBooleanArray.get(which)) {
                        return;
                    }
                    setValue(String.valueOf(which));
                }
            })
            .setOnDismissListener(dialog -> updateBackground(R.color.touch_up));

        mDialogDropDownFactory = (MiuiAlertDialogFactory.MiuiAlertDialogDropDownFactory) mDialog.getBaseFactory();
        mDialogDropDownFactory.mBooleanArray = mBooleanArray;
        mDialogDropDownFactory.setRootPreferenceView(getMainLayout());
        mDialogDropDownFactory.showDialogByTouchPosition(x, y);
        mDialog.show();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.mValue = mValue;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(((SavedState) state).getSuperState());
        setValue(savedState.mValue);
    }


    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
            new Parcelable.Creator<SavedState>() {
                @Override
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                @Override
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };

        String mValue;

        public SavedState(Parcel source) {
            super(source);
            mValue = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mValue);
        }
    }
}
