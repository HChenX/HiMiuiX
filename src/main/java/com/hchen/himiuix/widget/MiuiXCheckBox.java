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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class MiuiXCheckBox extends CheckBox {
    private OnCheckStateChangeListener mOnCheckStateChangeListener;

    public MiuiXCheckBox(Context context) {
        this(context, null);
    }

    public MiuiXCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiXCheckBox)) {
            if (typedArray.hasValue(R.styleable.MiuiXCheckBox_android_button)) {
                Drawable drawable = typedArray.getDrawable(R.styleable.MiuiXCheckBox_android_button);
                setButtonDrawable(drawable);
            } else {
                setButtonDrawable(R.drawable.btn_checkbox);
            }
        }

        setClickable(true);
        setBackground(null);
        setHapticFeedbackEnabled(false);
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() == checked) return;
        if (mOnCheckStateChangeListener == null) {
            super.setChecked(checked);
            return;
        }

        if (mOnCheckStateChangeListener.onCheckChange(this, checked)) {
            super.setChecked(checked);
        }
    }

    public void setOnCheckStateChangeListener(OnCheckStateChangeListener onCheckStateChangeListener) {
        mOnCheckStateChangeListener = onCheckStateChangeListener;
    }
}
