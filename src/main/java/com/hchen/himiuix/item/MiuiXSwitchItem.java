package com.hchen.himiuix.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.hchen.himiuix.R;
import com.hchen.himiuix.widget.MiuiXSwitch;

public class MiuiXSwitchItem extends MiuiXItem {
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
    }

    @Override
    protected void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_radiobutton).setVisibility(View.GONE);
    }

    @Override
    protected void loadItemView() {
        super.loadItemView();
        miuiXSwitch = findViewById(R.id.miuix_item_switch);
    }

    @Override
    protected void applyClickListener() {
        MiuiXSwitch miuiXSwitch = findViewById(R.id.miuix_item_switch);

        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!miuiXSwitch.isAnimationShowing()) {
                    miuiXSwitch.setChecked(!miuiXSwitch.isChecked());
                }
            }
        });
    }

    public void setChecked(boolean checked) {
        miuiXSwitch.setChecked(checked);
    }

    public boolean isChecked() {
        return miuiXSwitch.isChecked();
    }

    public MiuiXSwitch getMiuiXSwitch() {
        return miuiXSwitch;
    }
}
