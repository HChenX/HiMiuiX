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

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.hchen.himiuix.R;

public class ColorPickerAlphaView extends ColorBaseSeekBar implements ColorPickerHueView.OnColorHueChanged {

    public ColorPickerAlphaView(@NonNull Context context) {
        super(context);
    }

    public ColorPickerAlphaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerAlphaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorPickerAlphaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        mBackgroundImg = AppCompatResources.getDrawable(getContext(), R.drawable.color_picker_seekbar_alpha_bg);
        mColorPickerTag = ColorPickerTag.TAG_ALPHA;
        mColors = new int[]{
                Color.HSVToColor(0, new float[]{0, 1, 1}),
                Color.HSVToColor(255, new float[]{0, 1, 1}),
        };
        setMax(255);
        setProgress(255);
        super.init();
    }

    public void updateColorPickerAlphaState(int alpha) {
        setProgress(alpha);
        if (mColorPickerData != null)
            mColorPickerData.alpha = alpha;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mColorPickerData != null)
            mColorPickerData.alpha = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(float changed) {
        mColors = new int[]{
                Color.HSVToColor(0, new float[]{changed, 1, 1}),
                Color.HSVToColor(255, new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
