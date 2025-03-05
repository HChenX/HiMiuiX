package com.hchen.himiuix.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class MiuiCheckBox extends CheckBox {
    private OnCheckedStateChangeListener mOnCheckedStateChangeListener;

    public MiuiCheckBox(Context context) {
        this(context, null);
    }

    public MiuiCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setBackground(null);
        setButtonDrawable(R.drawable.btn_checkbox);
        setHapticFeedbackEnabled(false);
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() == checked) return;
        if (mOnCheckedStateChangeListener == null) {
            super.setChecked(checked);
            return;
        }

        if (mOnCheckedStateChangeListener.onCheckedChange(this, checked)) {
            super.setChecked(checked);
        }
    }

    public void setOnCheckedStateChangeListener(OnCheckedStateChangeListener onCheckedStateChangeListener) {
        mOnCheckedStateChangeListener = onCheckedStateChangeListener;
    }

    public interface OnCheckedStateChangeListener {
        boolean onCheckedChange(MiuiCheckBox miuiCheckBox, boolean newChecked);
    }
}
