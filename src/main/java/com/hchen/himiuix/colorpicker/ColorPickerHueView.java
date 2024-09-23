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

import java.util.Arrays;
import java.util.function.Consumer;

public class ColorPickerHueView extends ColorBaseSeekBar {
    private OnColorHueChanged[] onColorHueChangeds;

    public ColorPickerHueView(@NonNull Context context) {
        super(context);
    }

    public ColorPickerHueView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerHueView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorPickerHueView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        mColorPickerTag = ColorPickerTag.TAG_HUE;
        mColors = new int[]{
                Color.HSVToColor(new float[]{0, 1, 1}),
                Color.HSVToColor(new float[]{60, 1, 1}),
                Color.HSVToColor(new float[]{120, 1, 1}),
                Color.HSVToColor(new float[]{180, 1, 1}),
                Color.HSVToColor(new float[]{240, 1, 1}),
                Color.HSVToColor(new float[]{300, 1, 1}),
                Color.HSVToColor(new float[]{360, 1, 1})
        };
        setMax(36000);
        setMin(0);
        setProgress(100);
        super.init();
    }

    public void registerHueChangeListener(OnColorHueChanged[] onColorHueChangeds) {
        this.onColorHueChangeds = onColorHueChangeds;
    }

    public void updateColorPickerHueState(int hue) {
        setProgress(hue);
        callChanged(hue);
        if (mColorPickerData != null)
            mColorPickerData.hue = hue;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if (!fromUser) return;
        callChanged(progress);
    }

    private void callChanged(int hue) {
        Arrays.stream(onColorHueChangeds).forEach(new Consumer<OnColorHueChanged>() {
            @Override
            public void accept(OnColorHueChanged onColorHueChanged) {
                onColorHueChanged.onColorHueChanged((float) hue / 100);
            }
        });
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mColorPickerData != null)
            mColorPickerData.hue = seekBar.getProgress();
    }

    public interface OnColorHueChanged {
        void onColorHueChanged(float changed);
    }
}
