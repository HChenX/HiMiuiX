package com.hchen.himiuix.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class ColorBaseSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {
    public static String TAG = "MiuiPreference";
    protected GradientDrawable gradientDrawable;
    protected Drawable backgroundImg;
    protected ColorPickerData colorPickerData;
    protected OnColorValueChanged valueChanged;
    protected ColorPickerTag tag = ColorPickerTag.TAG_DEF;
    protected int[] colors;

    public enum ColorPickerTag {
        TAG_DEF,
        TAG_HUE,
        TAG_LIGHTNESS,
        TAG_SATURATION,
        TAG_ALPHA
    }

    public ColorBaseSeekBar(@NonNull Context context) {
        this(context, null);
    }

    public ColorBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiSeekBar);
    }

    public ColorBaseSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorBaseSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        updateProgressBackground();
        setThumb(AppCompatResources.getDrawable(getContext(), R.drawable.color_picker_circle_with_hole));
        setThumbOffset(MiuiXUtils.sp2px(getContext(), -3));
        setOnSeekBarChangeListener(this);
    }

    public void updateProgressBackground() {
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(colors);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
        gradientDrawable.setCornerRadius(MiuiXUtils.sp2px(getContext(), 15));
        gradientDrawable.setSize(-1, MiuiXUtils.sp2px(getContext(), 32));
        gradientDrawable.setStroke(0, 0);
        if (backgroundImg == null)
            setProgressDrawable(gradientDrawable);
        else
            setProgressDrawable(new LayerDrawable(new Drawable[]{backgroundImg, gradientDrawable}));
    }

    public void setColorPickerData(ColorPickerData colorPickerData) {
        this.colorPickerData = colorPickerData;
    }

    public void setColorPickerValueChangedListener(OnColorValueChanged valueChanged) {
        this.valueChanged = valueChanged;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (valueChanged != null) {
            if (fromUser)
                valueChanged.changed(tag, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public interface OnColorValueChanged {
        void changed(ColorPickerTag tag, int value);
    }
}
