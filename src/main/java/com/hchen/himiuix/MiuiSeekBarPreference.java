package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
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

public class MiuiSeekBarPreference extends MiuiPreference {
    private SeekBar seekBar;
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
    private int mSeekBarIncrement;
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
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            mSeekBarValue = value;
            updateLabelValue(value);
            persistInt(value);
            if (!isInitialTime) {
                callChangeListener(value);
                notifyChanged();
            }
            notifyDependencyChange(shouldDisableDependents());
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
        mainLayout = (ConstraintLayout) holder.itemView;
        seekBar = mainLayout.findViewById(R.id.seekbar);
        numberView = mainLayout.findViewById(R.id.seekbar_number);

        numberView.setVisibility(mShowSeekBarValue ? View.VISIBLE : View.GONE);
        seekBar.setOnSeekBarChangeListener(changeListener);
        seekBar.setMax(shouldStep ? stepCount : maxValue);
        seekBar.setMin(shouldStep ? 0 : minValue);

        if (mSeekBarIncrement != 0) {
            seekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else
            mSeekBarIncrement = seekBar.getKeyProgressIncrement();

        seekBar.setProgress(getStepBeforeIfNeed(mSeekBarValue));
        updateLabelValue(mSeekBarValue);
        seekBar.setEnabled(isEnabled());
    }

    private View view;

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
            v.setBackgroundResource(R.color.touch_down);
            view = v;
            new MiuiAlertDialog(getContext())
                    .setTitle(getTitle())
                    .setMessage(getSummary())
                    .setPositiveButton("确定", null)
                    .setHapticFeedbackEnabled(true)
                    .setEditText(String.valueOf(mSeekBarValue), true, new DialogInterface.TextWatcher() {
                        @Override
                        public void onResult(CharSequence s) {
                            int result = Integer.parseInt((String) s);
                            setValue(result);
                            setProgressIfNeed(result);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setOnDismissListener(dialog1 ->
                            view.setBackgroundResource(R.color.touch_up))
                    .show();
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
                    " Available step values: " + steps.toString());
        }
    }

    private int getStepAfterValueIfNeed(int progress) {
        if (shouldStep) {
            return minValue + (progress * stepValue);
        }
        return progress;
    }

    private int getStepBeforeIfNeed(int value) {
        if (shouldStep) {
            return (value - minValue) / stepValue;
        }
        return value;
    }

    private void setProgressIfNeed(int value) {
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;
        seekBar.setProgress(value);
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
}
