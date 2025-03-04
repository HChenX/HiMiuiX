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
package com.hchen.himiuix.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class MiuiTextView extends TextView {
    private boolean shouldFocusable;
    private boolean shouldSingeLineCenter;

    public MiuiTextView(Context context) {
        this(context, null);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiTextView)) {
            shouldFocusable = typedArray.getBoolean(R.styleable.MiuiTextView_focusable, false);
            shouldSingeLineCenter = typedArray.getBoolean(R.styleable.MiuiTextView_singeLineCenter, false);
        }
    }

    @Override
    public boolean isFocused() {
        return shouldFocusable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLineCount() <= 1 && shouldSingeLineCenter) {
            setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}
