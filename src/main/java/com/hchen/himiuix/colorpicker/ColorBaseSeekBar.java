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
package com.hchen.himiuix.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class ColorBaseSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {
    public static String TAG = "MiuiPreference";
    protected GradientDrawable mGradientDrawable;
    protected Drawable mBackgroundImg;
    protected ColorPickerData mColorPickerData;
    protected OnColorValueChanged mValueChanged;
    protected ColorPickerTag mColorPickerTag = ColorPickerTag.TAG_DEF;
    protected int[] mColors;

    public enum ColorPickerTag {
        TAG_DEF,
        TAG_HUE,
        TAG_LIGHTNESS,
        TAG_SATURATION,
        TAG_ALPHA
    }

    public ColorBaseSeekBar(@NonNull Context context) {
        this(context, null);
    }

    public ColorBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiSeekBar);
    }

    public ColorBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorBaseSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        updateProgressBackground();
        setThumb(AppCompatResources.getDrawable(getContext(), R.drawable.color_picker_circle_with_hole));
        setThumbOffset(MiuiXUtils.dp2px(getContext(), -3));
        setOnSeekBarChangeListener(this);
    }

    public void updateProgressBackground() {
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.setColors(mColors);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mGradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
        mGradientDrawable.setCornerRadius(MiuiXUtils.dp2px(getContext(), 15));
        mGradientDrawable.setSize(-1, MiuiXUtils.dp2px(getContext(), 29));
        mGradientDrawable.setStroke(0, 0);
        if (mBackgroundImg == null)
            setProgressDrawable(mGradientDrawable);
        else
            setProgressDrawable(new LayerDrawable(new Drawable[]{mBackgroundImg, mGradientDrawable}));
    }

    public void setColorPickerData(ColorPickerData colorPickerData) {
        this.mColorPickerData = colorPickerData;
    }

    public void setColorPickerValueChangedListener(OnColorValueChanged valueChanged) {
        this.mValueChanged = valueChanged;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mValueChanged != null) {
            if (fromUser)
                mValueChanged.changed(mColorPickerTag, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public interface OnColorValueChanged {
        void changed(ColorPickerTag tag, int value);
    }
}
