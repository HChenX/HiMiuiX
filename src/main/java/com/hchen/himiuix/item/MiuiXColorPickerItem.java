package com.hchen.himiuix.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.R;
import com.hchen.himiuix.colorpicker.ColorPickerDialog;
import com.hchen.himiuix.colorpicker.ColorSelectView;

public class MiuiXColorPickerItem extends MiuiXItem {
    private ColorSelectView colorSelectView;
    private MiuiAlertDialog colorPickerAlertDialog;
    private ColorPickerDialog.OnColorValueResultListener onColorValueResultListener;
    private Integer colorValue = null;

    public MiuiXColorPickerItem(@NonNull Context context) {
        this(context, null);
    }

    public MiuiXColorPickerItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXColorPickerItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXColorPickerItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_radiobutton).setVisibility(View.GONE);
    }

    @Override
    protected void loadItemView() {
        super.loadItemView();
        colorSelectView = findViewById(R.id.miuix_item_color_indicator);
    }

    @Override
    protected void applyClickListener() {
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPickerAlertDialog != null && colorPickerAlertDialog.isShowing()) return;

                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getContext());
                if (colorValue != null) colorPickerDialog.setColorValue(colorValue);
                colorPickerDialog.setOnColorValueResultListener(new ColorPickerDialog.OnColorValueResultListener() {
                    @Override
                    public void onColorValue(@ColorInt int color) {
                        setColorValue(color);
                        if (onColorValueResultListener != null)
                            onColorValueResultListener.onColorValue(color);
                    }
                });

                colorPickerAlertDialog = colorPickerDialog
                    .createColorPickerDialog()
                    .setTitle(getTitleView().getText())
                    .setMessage(getSummaryView().getText());

                colorPickerAlertDialog.show();
            }
        });
    }

    public void setColorValue(@ColorInt int colorValue) {
        this.colorValue = colorValue;
        colorSelectView.setColorValue(colorValue);
    }

    public void setColorValueChangedListener(@NonNull ColorPickerDialog.OnColorValueResultListener onColorValueResultListener) {
        this.onColorValueResultListener = onColorValueResultListener;
    }
}
