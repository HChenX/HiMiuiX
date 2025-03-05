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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.widget.MiuiSeekBar;

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiSeekBarPreference extends MiuiPreference {
    private ConstraintLayout mMainLayout;
    private MiuiSeekBar mSeekBarView;
    private TextView mNumberView;
    private boolean mTrackingTouch;
    private int mDisplayDividerValue;
    private CharSequence mFormat;
    private int mDefValue;
    private int mSeekBarValue;
    private int mMinValue;
    private int mMaxValue;
    private int mStepValue;
    private int mStepCount;
    private boolean shouldStep;
    private boolean isDialogEnabled;
    private boolean mShowSeekBarValue;
    private boolean shouldShowDefTip = true;
    private String mDefValueString;
    private boolean isInitialTime = true;

    private final SeekBar.OnSeekBarChangeListener mChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mTrackingTouch) {
                int stepAfterValue = getStepAfterValueIfNeed(progress);
                ((MiuiSeekBar) seekBar).setShowDefaultPoint((stepAfterValue != mDefValue) && shouldShowDefTip);
                if (stepAfterValue == mMaxValue || stepAfterValue == mMinValue
                    || stepAfterValue == mDefValue) {
                    mMainLayout.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                }
                updateLabelValue(stepAfterValue);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = false;
            if (seekBar.getProgress() != mSeekBarValue) {
                setValue(getStepAfterValueIfNeed(seekBar.getProgress()));
            }
        }
    };

    public MiuiSeekBarPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.miuix_seekbar);
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MiuiSeekBarPreference,
            defStyleAttr, defStyleRes)) {
            mMinValue = a.getInt(R.styleable.MiuiSeekBarPreference_minValue, 0);
            mMaxValue = a.getInt(R.styleable.MiuiSeekBarPreference_maxValue, 100);
            mStepValue = a.getInt(R.styleable.MiuiSeekBarPreference_stepValue, 1);
            mFormat = a.getString(R.styleable.MiuiSeekBarPreference_format);
            mShowSeekBarValue = a.getBoolean(R.styleable.MiuiSeekBarPreference_showSeekBarValue, false);
            mDisplayDividerValue = a.getInt(R.styleable.MiuiSeekBarPreference_displayDividerValue, -1);
            isDialogEnabled = a.getBoolean(R.styleable.MiuiSeekBarPreference_dialogEnabled, false);
            shouldShowDefTip = a.getBoolean(R.styleable.MiuiSeekBarPreference_showDefTip, true);
            mDefValueString = a.getString(R.styleable.MiuiSeekBarPreference_defValueString);
            if (mDisplayDividerValue != -1) {
                double d = Math.log10(mDisplayDividerValue);
                if (d != Math.floor(d)) {
                    throw new RuntimeException("The delimiter input value must be 10 or a power of 10!");
                }
            }
            calculateStep();
        }
    }

    private void setValue(int value) {
        if (value < mMinValue) value = mMinValue;
        if (value > mMaxValue) value = mMaxValue;
        isStepNumber(value);
        if (value != mSeekBarValue) {
            if (isInitialTime || callChangeListener(value)) {
                mSeekBarValue = value;
                updateLabelValue(value);
                persistInt(value);
                if (!isInitialTime) {
                    notifyChanged();
                }
            }
        }
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue == null) defaultValue = 0;
        setValue(getPersistedInt((Integer) defaultValue));
        isInitialTime = false;
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return mDefValue = a.getInt(index, 0);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mMainLayout = (ConstraintLayout) holder.itemView;
        mSeekBarView = mMainLayout.findViewById(R.id.seekbar);
        mNumberView = mMainLayout.findViewById(R.id.seekbar_number);
        Drawable seekBarDrawable = mSeekBarView.getProgressDrawable();

        mNumberView.setVisibility(mShowSeekBarValue ? View.VISIBLE : View.GONE);
        mSeekBarView.isHapticFeedbackEnabled();
        mSeekBarView.setOnSeekBarChangeListener(mChangeListener);
        mSeekBarView.setMax(shouldStep ? mStepCount : mMaxValue);
        mSeekBarView.setMin(shouldStep ? 0 : mMinValue);

        mSeekBarView.setProgress(getStepBeforeIfNeed(mSeekBarValue));
        if (shouldStep) {
            mSeekBarView.setShouldStep(true);
            mSeekBarView.setDefStep(getStepBeforeIfNeed(mDefValue));
        } else mSeekBarView.setDefValue(mDefValue);
        if (mSeekBarValue != mDefValue)
            mSeekBarView.setShowDefaultPoint(shouldShowDefTip);

        mSeekBarView.setHapticFeedbackEnabled(false);
        mSeekBarView.setEnabled(isEnabled());
        updateLabelValue(mSeekBarValue);
        if (isEnabled()) {
            seekBarDrawable.setAlpha(255);
            mNumberView.setAlpha(1f);
        } else {
            seekBarDrawable.setAlpha(125);
            mNumberView.setAlpha(0.5f);
        }
    }

    private MiuiAlertDialog mDialog;

    @Override
    boolean onMainLayoutTouch(View v, MotionEvent event) {
        if (!isDialogEnabled) {
            return super.onMainLayoutTouch(v, event);
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            updateBackground(R.color.touch_down);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            updateBackground(R.color.touch_up);
        } else if (action == MotionEvent.ACTION_UP) {
            if (mDialog != null && mDialog.isShowing())
                return false;

            updateBackground(R.color.touch_down);

            CharSequence def =
                mDisplayDividerValue != -1 ?
                    String.valueOf((float) mSeekBarValue / (float) mDisplayDividerValue) :
                    String.valueOf(mSeekBarValue);

            mDialog = new MiuiAlertDialog(getContext())
                .setTitle(getTitle())
                .setMessage(getSummary())
                .setHapticFeedbackEnabled(true)
                .setEnableEditTextView(true)
                .setEditTextAutoKeyboard(true)
                .setEditTextInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER)
                .setEditText(def, new DialogInterface.TextWatcher() {
                    @Override
                    public void onResult(DialogInterface dialog, CharSequence s) {
                        float f = Float.MIN_VALUE;
                        if (mDisplayDividerValue != -1)
                            f = Float.parseFloat((String) s) * mDisplayDividerValue;

                        int result = Integer.parseInt(f != Float.MIN_VALUE ? String.valueOf((int) f) : (String) s);
                        setValue(result);
                        mSeekBarView.setShowDefaultPoint((result != mDefValue) && shouldShowDefTip);
                        setProgressIfNeed(getStepBeforeIfNeed(result));
                    }
                })
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .setOnDismissListener(dialog ->
                    updateBackground(R.color.touch_up));
            mDialog.show();
        }
        return false;
    }

    private void calculateStep() {
        if (mStepValue != 1) {
            mStepCount = Math.round((float) (mMaxValue - mMinValue) / mStepValue);
            if ((mStepCount * mStepValue) != (mMaxValue - mMinValue)) {
                mMaxValue = mMinValue + (mStepCount * mStepValue); // 确保跳跃为整数
            }
            shouldStep = true;
            return;
        }
        shouldStep = false;
    }

    private void isStepNumber(int value) {
        if (shouldStep) {
            ArrayList<Integer> steps = new ArrayList<>();
            for (int i = 0; i <= mStepCount; i++) {
                steps.add(mMinValue + (i * mStepValue));
            }
            if (steps.contains(value)) return;
            throw new RuntimeException("Default value incorrect!" +
                " It is not one of the available step values!" +
                " Available step values: " + (mDisplayDividerValue == -1 ?
                steps.toString() :
                Arrays.toString(steps.stream().map(integer ->
                    ((float) integer / (float) mDisplayDividerValue)).toArray())
            ));
        }
    }

    // 获取叠加跳跃后的值；是应该储存的最终值
    private int getStepAfterValueIfNeed(int progress) {
        if (shouldStep) {
            return mMinValue + (progress * mStepValue);
        }
        return progress;
    }

    // 从值解析出跳跃步数；作为 seekbar 使用的阶数
    private int getStepBeforeIfNeed(int value) {
        if (shouldStep) {
            return (value - mMinValue) / mStepValue;
        }
        return value;
    }

    private void setProgressIfNeed(int value) {
        if (shouldStep) {
            if (value < 0) value = 0;
            if (value > mStepCount) value = mStepCount;
        } else {
            if (value < mMinValue) value = mMinValue;
            if (value > mMaxValue) value = mMaxValue;
        }
        mSeekBarView.setProgress(value);
    }

    private void updateLabelValue(int value) {
        if (mNumberView == null) return;
        if (mNumberView.getVisibility() == View.VISIBLE) {
            if (mDefValueString != null && value == mDefValue) {
                mNumberView.setText(mDefValueString);
                return;
            }
            String s = String.valueOf(value);
            if (mDisplayDividerValue != -1) {
                s = String.valueOf(((float) value / mDisplayDividerValue));
            }
            String t = s + (mFormat != null ? mFormat : "");
            mNumberView.setText(t);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent()) {
            return parcelable;
        }

        final SavedState savedState = new SavedState(parcelable);
        savedState.mSeekbarValue = mSeekBarValue;
        savedState.mMin = mMinValue;
        savedState.mMax = mMaxValue;
        savedState.mStepCount = mStepCount;
        savedState.mStepValue = mStepValue;
        savedState.shouldStep = shouldStep;

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
        mSeekBarValue = savedState.mSeekbarValue;
        mMinValue = savedState.mMin;
        mMaxValue = savedState.mMax;
        mStepValue = savedState.mStepValue;
        mStepCount = savedState.mStepCount;
        shouldStep = savedState.shouldStep;
        notifyChanged();
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

        int mSeekbarValue;
        int mMin;
        int mMax;
        int mStepValue;
        int mStepCount;
        boolean shouldStep;

        public SavedState(Parcel source) {
            super(source);

            mSeekbarValue = source.readInt();
            mMin = source.readInt();
            mMax = source.readInt();
            mStepValue = source.readInt();
            mStepCount = source.readInt();
            shouldStep = source.readBoolean();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(mSeekbarValue);
            dest.writeInt(mMin);
            dest.writeInt(mMax);
            dest.writeInt(mStepValue);
            dest.writeInt(mStepCount);
            dest.writeBoolean(shouldStep);
        }
    }
}
