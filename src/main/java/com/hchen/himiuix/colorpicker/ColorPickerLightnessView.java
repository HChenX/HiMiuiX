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

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorPickerLightnessView extends ColorBaseSeekBar implements ColorPickerHueView.OnColorHueChangedListener {
    public ColorPickerLightnessView(@NonNull Context context) {
        this(context, null);
    }

    public ColorPickerLightnessView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerLightnessView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPickerLightnessView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        colorPickerTag = ColorPickerTag.TAG_LIGHTNESS;
        colors = new int[]{
            Color.HSVToColor(new float[]{0, 1, 0}),
            Color.HSVToColor(new float[]{0, 1, 1})
        };
        setMax(10000);
        setProgress(0);
        super.init();
    }

    public void updateColorPickerLightnessValue(int lightness) {
        setProgress(lightness);
        if (colorPickerData != null)
            colorPickerData.lightness = lightness;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (colorPickerData != null)
            colorPickerData.lightness = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(float changed) {
        colors = new int[]{
            Color.HSVToColor(new float[]{changed, 1, 0}),
            Color.HSVToColor(new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
