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
        tag = ColorPickerTag.TAG_HUE;
        colors = new int[]{
                Color.HSVToColor(new float[]{0, 1, 1}),
                Color.HSVToColor(new float[]{60, 1, 1}),
                Color.HSVToColor(new float[]{120, 1, 1}),
                Color.HSVToColor(new float[]{180, 1, 1}),
                Color.HSVToColor(new float[]{240, 1, 1}),
                Color.HSVToColor(new float[]{300, 1, 1}),
                Color.HSVToColor(new float[]{360, 1, 1})
        };
        setMax(360);
        setProgress(0);
        super.init();
    }

    public void registerHueChangeListener(OnColorHueChanged[] onColorHueChangeds) {
        this.onColorHueChangeds = onColorHueChangeds;
    }

    public void updateColorPickerHueState(int hue) {
        setProgress(hue, true);
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
        Arrays.stream(onColorHueChangeds).forEach(new Consumer<OnColorHueChanged>() {
            @Override
            public void accept(OnColorHueChanged onColorHueChanged) {
                onColorHueChanged.onColorHueChanged(hue);
            }
        });
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (colorPickerData != null)
            colorPickerData.hue = seekBar.getProgress();
    }

    public interface OnColorHueChanged {
        void onColorHueChanged(int changed);
    }
}
