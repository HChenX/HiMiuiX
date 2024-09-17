package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiSeekBarPreference extends MiuiPreference {
    private SeekBar seekBarView;
    private TextView numberView;
    private boolean mTrackingTouch;
    private int mDisplayDividerValue;
    private CharSequence format;
    private int mSeekBarValue;
    private int minValue;
    private int maxValue;
    private int stepValue;
    private int stepCount;
    private boolean shouldStep;
    private boolean dialogEnabled;
    private boolean mShowSeekBarValue;
    private boolean isInitialTime = true;

    private final SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mTrackingTouch) {
                updateLabelValue(getStepAfterValueIfNeed(progress));
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
        super(context);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuiSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_seekbar);
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MiuiSeekBarPreference,
                defStyleAttr, defStyleRes)) {
            minValue = a.getInt(R.styleable.MiuiSeekBarPreference_minValue, 0);
            maxValue = a.getInt(R.styleable.MiuiSeekBarPreference_maxValue, 100);
            stepValue = a.getInt(R.styleable.MiuiSeekBarPreference_stepValue, 1);
            format = a.getString(R.styleable.MiuiSeekBarPreference_format);
            mShowSeekBarValue = a.getBoolean(R.styleable.MiuiSeekBarPreference_showSeekBarValue, false);
            mDisplayDividerValue = a.getInt(R.styleable.MiuiSeekBarPreference_displayDividerValue, -1);
            dialogEnabled = a.getBoolean(R.styleable.MiuiSeekBarPreference_dialogEnabled, false);
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
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;
        isStepNumber(value);
        if (value != mSeekBarValue) {
            if (callChangeListener(value) || isInitialTime) {
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
        return a.getInt(index, 0);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ConstraintLayout mainLayout = (ConstraintLayout) holder.itemView;
        seekBarView = mainLayout.findViewById(R.id.seekbar);
        numberView = mainLayout.findViewById(R.id.seekbar_number);
        Drawable seekBarDrawable = seekBarView.getProgressDrawable();

        numberView.setVisibility(mShowSeekBarValue ? View.VISIBLE : View.GONE);
        seekBarView.setOnSeekBarChangeListener(changeListener);
        seekBarView.setMax(shouldStep ? stepCount : maxValue);
        seekBarView.setMin(shouldStep ? 0 : minValue);

        seekBarView.setProgress(getStepBeforeIfNeed(mSeekBarValue));
        updateLabelValue(mSeekBarValue);
        seekBarView.setEnabled(isEnabled());
        if (isEnabled()) {
            seekBarDrawable.setAlpha(255);
            numberView.setTextColor(getContext().getColor(R.color.tittle));
        } else {
            seekBarDrawable.setAlpha(125);
            numberView.setTextColor(getContext().getColor(R.color.tittle_d));
        }
    }

    private View view;
    private MiuiAlertDialog miuiAlertDialog;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!dialogEnabled) {
            return super.onTouch(v, event);
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.touch_down);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundResource(R.color.touch_up);
        } else if (action == MotionEvent.ACTION_UP) {
            if (miuiAlertDialog != null && miuiAlertDialog.isShowing())
                return false;
            v.setBackgroundResource(R.color.touch_down);
            view = v;
            CharSequence def;
            if (mDisplayDividerValue != -1)
                def = String.valueOf((float) mSeekBarValue / (float) mDisplayDividerValue);
            else
                def = String.valueOf(mSeekBarValue);

            miuiAlertDialog = new MiuiAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setHapticFeedbackEnabled(true)
                    .setEditText(def, true, new DialogInterface.TextWatcher() {
                        @Override
                        public void onResult(CharSequence s) {
                            float f = Float.MIN_VALUE;
                            if (mDisplayDividerValue != -1)
                                f = Float.parseFloat((String) s) * mDisplayDividerValue;
                            int result = Integer.parseInt(f != Float.MIN_VALUE ? String.valueOf((int) f) : (String) s);
                            setValue(result);
                            setProgressIfNeed(getStepBeforeIfNeed(result));
                        }
                    })
                    .setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                    .setPositiveButton("确定", null)
                    .setNegativeButton("取消", null)
                    .setOnDismissListener(dialog1 ->
                            view.setBackgroundResource(R.color.touch_up));
            miuiAlertDialog.show();
        }
        return false;
    }

    private void calculateStep() {
        if (stepValue != 1) {
            stepCount = Math.round((float) (maxValue - minValue) / stepValue);
            if ((stepCount * stepValue) != (maxValue - minValue)) {
                maxValue = minValue + (stepCount * stepValue); // 确保跳跃为整数
            }
            shouldStep = true;
            return;
        }
        shouldStep = false;
    }

    private void isStepNumber(int value) {
        if (shouldStep) {
            ArrayList<Integer> steps = new ArrayList<>();
            for (int i = 0; i <= stepCount; i++) {
                steps.add(minValue + (i * stepValue));
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
            return minValue + (progress * stepValue);
        }
        return progress;
    }

    // 从值解析出跳跃步数；作为 seekbar 使用的阶数
    private int getStepBeforeIfNeed(int value) {
        if (shouldStep) {
            return (value - minValue) / stepValue;
        }
        return value;
    }

    private void setProgressIfNeed(int value) {
        if (shouldStep) {
            if (value < 0) value = 0;
            if (value > stepCount) value = stepCount;
        } else {
            if (value < minValue) value = minValue;
            if (value > maxValue) value = maxValue;
        }
        seekBarView.setProgress(value);
    }

    private void updateLabelValue(int value) {
        if (numberView == null) return;
        if (numberView.getVisibility() == View.VISIBLE) {
            String s = String.valueOf(value);
            if (mDisplayDividerValue != -1) {
                s = String.valueOf(((float) value / mDisplayDividerValue));
            }
            String t = s + (format != null ? format : "");
            numberView.setText(t);
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
        savedState.mMin = minValue;
        savedState.mMax = maxValue;
        savedState.stepCount = stepCount;
        savedState.stepValue = stepValue;
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
        minValue = savedState.mMin;
        maxValue = savedState.mMax;
        stepValue = savedState.stepValue;
        stepCount = savedState.stepCount;
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
        int stepValue;
        int stepCount;
        boolean shouldStep;

        public SavedState(Parcel source) {
            super(source);

            mSeekbarValue = source.readInt();
            mMin = source.readInt();
            mMax = source.readInt();
            stepValue = source.readInt();
            stepCount = source.readInt();
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
            dest.writeInt(stepValue);
            dest.writeInt(stepCount);
            dest.writeBoolean(shouldStep);
        }
    }
}
