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
    protected GradientDrawable gradientDrawable;
    protected Drawable backgroundImage;
    protected ColorPickerData colorPickerData;
    protected OnColorValueChangedListener valueChangedListener;
    protected ColorPickerTag colorPickerTag = ColorPickerTag.TAG_DEF;
    protected int[] colors;

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
        this(context, attrs,0);
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
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(colors);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
        gradientDrawable.setCornerRadius(MiuiXUtils.dp2px(getContext(), 15));
        gradientDrawable.setSize(-1, MiuiXUtils.dp2px(getContext(), 29));
        gradientDrawable.setStroke(0, 0);
        if (backgroundImage == null)
            setProgressDrawable(gradientDrawable);
        else
            setProgressDrawable(new LayerDrawable(new Drawable[]{backgroundImage, gradientDrawable}));
    }

    public void setColorPickerData(ColorPickerData colorPickerData) {
        this.colorPickerData = colorPickerData;
    }

    public void setColorPickerValueChangedListener(OnColorValueChangedListener valueChanged) {
        this.valueChangedListener = valueChanged;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (valueChangedListener != null) {
            if (fromUser)
                valueChangedListener.onColorValueChanged(colorPickerTag, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public interface OnColorValueChangedListener {
        void onColorValueChanged(ColorPickerTag tag, int value);
    }
}
