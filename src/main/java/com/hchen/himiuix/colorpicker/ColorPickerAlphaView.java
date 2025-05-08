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
import androidx.appcompat.content.res.AppCompatResources;

import com.hchen.himiuix.R;

public class ColorPickerAlphaView extends ColorBaseSeekBar implements ColorPickerHueView.OnColorHueChangedListener {

    public ColorPickerAlphaView(@NonNull Context context) {
        this(context, null);
    }

    public ColorPickerAlphaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerAlphaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPickerAlphaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        backgroundImage = AppCompatResources.getDrawable(getContext(), R.drawable.color_picker_seekbar_alpha_bg);
        colorPickerTag = ColorPickerTag.TAG_ALPHA;
        colors = new int[]{
            Color.HSVToColor(0, new float[]{0, 1, 1}),
            Color.HSVToColor(255, new float[]{0, 1, 1}),
        };
        setMax(255);
        setProgress(255);
        super.init();
    }

    public void updateColorPickerAlphaValue(int alpha) {
        setProgress(alpha);
        if (colorPickerData != null)
            colorPickerData.alpha = alpha;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (colorPickerData != null)
            colorPickerData.alpha = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(float changed) {
        colors = new int[]{
            Color.HSVToColor(0, new float[]{changed, 1, 1}),
            Color.HSVToColor(255, new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
