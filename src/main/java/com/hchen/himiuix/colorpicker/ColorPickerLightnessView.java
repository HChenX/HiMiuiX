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
