package com.hchen.himiuix.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorPickerSaturationView extends ColorBaseSeekBar implements ColorPickerHueView.OnColorHueChanged {
    public ColorPickerSaturationView(@NonNull Context context) {
        super(context);
    }

    public ColorPickerSaturationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerSaturationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorPickerSaturationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        mColorPickerTag = ColorPickerTag.TAG_SATURATION;
        mColors = new int[]{
                Color.HSVToColor(new float[]{0, 0, 1}),
                Color.HSVToColor(new float[]{0, 1, 1})
        };
        setMax(10000);
        setProgress(0);
        super.init();
    }

    public void updateColorPickerSaturationState(int saturation) {
        setProgress(saturation);
        if (mColorPickerData != null)
            mColorPickerData.saturation = saturation;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mColorPickerData != null)
            mColorPickerData.saturation = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(float changed) {
        mColors = new int[]{
                Color.HSVToColor(new float[]{changed, 0, 1}),
                Color.HSVToColor(new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
