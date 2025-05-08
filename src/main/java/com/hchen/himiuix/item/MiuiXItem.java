package com.hchen.himiuix.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.R;

public class MiuiXItem extends FrameLayout {
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView iconView;
    private ImageView customIndicatorView;

    public MiuiXItem(Context context) {
        this(context, null);
    }

    public MiuiXItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        boolean reverseLayout;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiXItem, defStyleAttr, defStyleRes)) {
            reverseLayout = array.getBoolean(R.styleable.MiuiXItem_reverseLayout, false);
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

        loadItemView();
        PressHelper.init(this, true);
        updateIndicatorVisibility();
        applyClickListener();
    }

    protected void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_radiobutton).setVisibility(View.GONE);
    }

    protected void loadItemView() {
        titleView = findViewById(R.id.miuix_item_title);
        summaryView = findViewById(R.id.miuix_item_summary);
        tipView = findViewById(R.id.miuix_item_tip);
        iconView = findViewById(R.id.miuix_item_icon);
        customIndicatorView = findViewById(R.id.miuix_item_custom_indicator);
    }

    protected void applyClickListener() {
    }

    public void setTitle(CharSequence title) {
        titleView.setText(title);
    }

    public void setTitle(@StringRes int titleId) {
        setTitle(getResources().getString(titleId));
    }


    public void setSummary(CharSequence summary) {
        summaryView.setText(summary);
    }

    public void setSummary(@StringRes int summaryId) {
        setSummary(getResources().getString(summaryId));
    }

    public void setTip(CharSequence tip) {
        tipView.setText(tip);
    }

    public void setTip(@StringRes int tipId) {
        setTip(getResources().getString(tipId));
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

    public ImageView getCustomIndicatorView() {
        return customIndicatorView;
    }
}
