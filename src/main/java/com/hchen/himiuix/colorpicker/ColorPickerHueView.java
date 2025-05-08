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

import java.util.Arrays;
import java.util.function.Consumer;

public class ColorPickerHueView extends ColorBaseSeekBar {
    private OnColorHueChangedListener[] onColorHueChangedListeners;

    public ColorPickerHueView(@NonNull Context context) {
        this(context, null);
    }

    public ColorPickerHueView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerHueView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPickerHueView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        colorPickerTag = ColorPickerTag.TAG_HUE;
        colors = new int[]{
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

    public void registerHueChangeListener(OnColorHueChangedListener... onColorHueChangedListeners) {
        this.onColorHueChangedListeners = onColorHueChangedListeners;
    }

    public void updateColorPickerHueValue(int hue) {
        setProgress(hue);
        callChanged(hue);
        if (colorPickerData != null)
            colorPickerData.hue = hue;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        if (!fromUser) return;
        callChanged(progress);
    }

    private void callChanged(int hue) {
        Arrays.stream(onColorHueChangedListeners).forEach(new Consumer<OnColorHueChangedListener>() {
            @Override
            public void accept(OnColorHueChangedListener onColorHueChangedListener) {
                onColorHueChangedListener.onColorHueChanged((float) hue / 100);
            }
        });
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (colorPickerData != null)
            colorPickerData.hue = seekBar.getProgress();
    }

    public interface OnColorHueChangedListener {
        void onColorHueChanged(float changed);
    }
}
