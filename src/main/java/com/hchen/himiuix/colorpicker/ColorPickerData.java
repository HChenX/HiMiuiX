package com.hchen.himiuix.colorpicker;

import android.graphics.Color;

public class ColorPickerData {
    public int hue = 0;
    public int saturation = 0;
    public int lightness = 100;
    public int alpha = 255;

    public int HSVToColor() {
        return Color.HSVToColor(alpha, new float[]{(float) hue / 100, (float) saturation / 10000, (float) lightness / 10000});
    }
}
