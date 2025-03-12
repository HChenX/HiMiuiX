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

public class ColorPickerLightnessView extends ColorBaseSeekBar implements ColorPickerHueView.OnColorHueChanged {
    public ColorPickerLightnessView(@NonNull Context context) {
        super(context);
    }

    public ColorPickerLightnessView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerLightnessView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorPickerLightnessView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        mColorPickerTag = ColorPickerTag.TAG_LIGHTNESS;
        mColors = new int[]{
                Color.HSVToColor(new float[]{0, 1, 0}),
                Color.HSVToColor(new float[]{0, 1, 1})
        };
        setMax(10000);
        setProgress(0);
        super.init();
    }

    public void updateColorPickerLightnessState(int lightness) {
        setProgress(lightness);
        if (mColorPickerData != null)
            mColorPickerData.lightness = lightness;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mColorPickerData != null)
            mColorPickerData.lightness = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(float changed) {
        mColors = new int[]{
                Color.HSVToColor(new float[]{changed, 1, 0}),
                Color.HSVToColor(new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
