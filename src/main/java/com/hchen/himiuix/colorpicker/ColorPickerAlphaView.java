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
        backgroundImg = AppCompatResources.getDrawable(getContext(), R.drawable.color_picker_seekbar_alpha_bg);
        tag = ColorPickerTag.TAG_ALPHA;
        colors = new int[]{
                Color.HSVToColor(0, new float[]{0, 1, 1}),
                Color.HSVToColor(255, new float[]{0, 1, 1}),
        };
        setMax(255);
        setProgress(255);
        super.init();
    }

    public void updateColorPickerAlphaState(int alpha) {
        setProgress(alpha, true);
        if (colorPickerData != null)
            colorPickerData.alpha = alpha;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (colorPickerData != null)
            colorPickerData.alpha = seekBar.getProgress();
    }

    @Override
    public void onColorHueChanged(int changed) {
        colors = new int[]{
                Color.HSVToColor(0, new float[]{changed, 1, 1}),
                Color.HSVToColor(255, new float[]{changed, 1, 1}),
        };
        updateProgressBackground();
    }
}
