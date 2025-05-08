package com.hchen.himiuix.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hchen.himiuix.R;
import com.hchen.himiuix.widget.MiuiXSwitch;

public class MiuiXSwitchItem extends FrameLayout {
    private TextView titleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView iconView;
    private MiuiXSwitch miuiXSwitch;

    public MiuiXSwitchItem(Context context) {
        this(context, null);
    }

    public MiuiXSwitchItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXSwitchItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXSwitchItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        boolean reverseLayout;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiXSwitchItem, defStyleAttr, defStyleRes)) {
            reverseLayout = array.getBoolean(R.styleable.MiuiXSwitchItem_reverseLayout, false);
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

        PressHelper.init(this, false);
        updateIndicatorVisibility();
        applyClickListener();
        loadView();
    }

    private void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
    }

    private void loadView() {
        titleView = findViewById(R.id.miuix_item_title);
        summaryView = findViewById(R.id.miuix_item_summary);
        tipView = findViewById(R.id.miuix_item_tip);
        iconView = findViewById(R.id.miuix_item_icon);
        miuiXSwitch = findViewById(R.id.miuix_item_switch);
    }

    private void applyClickListener() {
        MiuiXSwitch miuiXSwitch = findViewById(R.id.miuix_item_switch);

        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!miuiXSwitch.isAnimationShowing()) {
                    miuiXSwitch.setChecked(!miuiXSwitch.isChecked());
                    v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                }
            }
        });
    }

    public MiuiXSwitchItem setTitle(CharSequence title) {
        titleView.setText(title);
        return this;
    }

    public MiuiXSwitchItem setTitle(@StringRes int titleId) {
        return setTitle(getResources().getString(titleId));
    }


    public MiuiXSwitchItem setSummary(CharSequence summary) {
        summaryView.setText(summary);
        return this;
    }

    public MiuiXSwitchItem setSummary(@StringRes int summaryId) {
        return setSummary(getResources().getString(summaryId));
    }

    public MiuiXSwitchItem setTip(CharSequence tip) {
        tipView.setText(tip);
        return this;
    }

    public MiuiXSwitchItem setTip(@StringRes int tipId) {
        return setTip(getResources().getString(tipId));
    }

    public MiuiXSwitchItem setChecked(boolean checked) {
        miuiXSwitch.setChecked(checked);
        return this;
    }

    public boolean isChecked() {
        return miuiXSwitch.isChecked();
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

    public MiuiXSwitch getMiuiXSwitch() {
        return miuiXSwitch;
    }
}
