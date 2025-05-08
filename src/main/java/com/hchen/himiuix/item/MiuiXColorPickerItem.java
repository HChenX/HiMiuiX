package com.hchen.himiuix.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.R;
import com.hchen.himiuix.colorpicker.ColorPickerDialog;
import com.hchen.himiuix.colorpicker.ColorSelectView;

public class MiuiXColorPickerItem extends FrameLayout {
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView iconView;
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

        boolean reverseLayout;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiXColorPickerItem, defStyleAttr, defStyleRes)) {
            reverseLayout = array.getBoolean(R.styleable.MiuiXColorPickerItem_reverseLayout, false);
        }
        LayoutInflater.from(context).inflate(
            reverseLayout ? R.layout.miuix_item_reverse : R.layout.miuix_item,
            this,
            true
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        PressHelper.init(this, true);
        updateIndicatorVisibility();
        loadView();
        applyClickListener();
    }

    private void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
    }

    private void loadView() {
        titleView = findViewById(R.id.miuix_item_title);
        summaryView = findViewById(R.id.miuix_item_summary);
        tipView = findViewById(R.id.miuix_item_tip);
        iconView = findViewById(R.id.miuix_item_icon);
        colorSelectView = findViewById(R.id.miuix_item_color_indicator);
    }

    private void applyClickListener() {
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
                    .setTitle(titleView.getText())
                    .setMessage(summaryView.getText());

                colorPickerAlertDialog.show();
            }
        });
    }

    public MiuiXColorPickerItem setTitle(CharSequence title) {
        titleView.setText(title);
        return this;
    }

    public MiuiXColorPickerItem setTitle(@StringRes int titleId) {
        return setTitle(getResources().getString(titleId));
    }

    public MiuiXColorPickerItem setSummary(CharSequence summary) {
        summaryView.setText(summary);
        return this;
    }

    public MiuiXColorPickerItem setSummary(@StringRes int summaryId) {
        return setSummary(getResources().getString(summaryId));
    }

    public MiuiXColorPickerItem setTip(CharSequence tip) {
        tipView.setText(tip);
        return this;
    }

    public MiuiXColorPickerItem setTip(@StringRes int tipId) {
        return setTip(getResources().getString(tipId));
    }

    public MiuiXColorPickerItem setColorValue(@ColorInt int colorValue) {
        this.colorValue = colorValue;
        colorSelectView.setColorValue(colorValue);
        return this;
    }

    public MiuiXColorPickerItem setColorValueChangedListener(@NonNull ColorPickerDialog.OnColorValueResultListener onColorValueResultListener) {
        this.onColorValueResultListener = onColorValueResultListener;
        return this;
    }

    public TextView getTitleView() {
        return titleView;
    }

    public TextView getSummaryView() {
        return summaryView;
    }

    public TextView getTipView() {
        return tipView;
    }

    public ImageView getIconView() {
        return iconView;
    }
}
