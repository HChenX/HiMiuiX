package com.hchen.himiuix.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.R;
import com.hchen.himiuix.widget.MiuiXCheckBox;

public class MiuiXCheckBoxItem extends FrameLayout {
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView iconView;
    private MiuiXCheckBox checkBox;

    public MiuiXCheckBoxItem(@NonNull Context context) {
        this(context, null);
    }

    public MiuiXCheckBoxItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXCheckBoxItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXCheckBoxItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        boolean reverseLayout;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiXCheckBoxItem, defStyleAttr, defStyleRes)) {
            reverseLayout = array.getBoolean(R.styleable.MiuiXCheckBoxItem_reverseLayout, false);
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
        applyClickListener();
    }

    private void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.VISIBLE);
    }

    private void loadView() {
        titleView = findViewById(R.id.miuix_item_title);
        summaryView = findViewById(R.id.miuix_item_summary);
        tipView = findViewById(R.id.miuix_item_tip);
        iconView = findViewById(R.id.miuix_item_icon);
        checkBox = findViewById(R.id.miuix_item_checkbox);
    }

    private void applyClickListener() {
        MiuiXCheckBox checkBox = findViewById(R.id.miuix_item_checkbox);

        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
    }

    public MiuiXCheckBoxItem setTitle(CharSequence title) {
        titleView.setText(title);
        return this;
    }

    public MiuiXCheckBoxItem setTitle(@StringRes int titleId) {
        return setTitle(getResources().getString(titleId));
    }


    public MiuiXCheckBoxItem setSummary(CharSequence summary) {
        summaryView.setText(summary);
        return this;
    }

    public MiuiXCheckBoxItem setSummary(@StringRes int summaryId) {
        return setSummary(getResources().getString(summaryId));
    }

    public MiuiXCheckBoxItem setTip(CharSequence tip) {
        tipView.setText(tip);
        return this;
    }

    public MiuiXCheckBoxItem setTip(@StringRes int tipId) {
        return setTip(getResources().getString(tipId));
    }

    public MiuiXCheckBoxItem setChecked(boolean checked) {
        checkBox.setChecked(checked);
        return this;
    }

    public boolean isChecked() {
        return checkBox.isChecked();
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

    public MiuiXCheckBox getCheckBox() {
        return checkBox;
    }
}
