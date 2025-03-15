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
package com.hchen.himiuix.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.hchen.himiuix.R;


@SuppressLint("AppCompatCustomView")
public class MiuiRadioButton extends RadioButton {
    private OnCheckedStateChangeListener mOnCheckedStateChangeListener;

    public MiuiRadioButton(Context context) {
        this(context, null);
    }

    public MiuiRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setBackground(null);
        setButtonDrawable(R.drawable.btn_radio_arrow);
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
        boolean onCheckedChange(MiuiRadioButton miuiRadioButton, boolean newChecked);
    }
}
