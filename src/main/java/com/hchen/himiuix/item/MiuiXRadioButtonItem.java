package com.hchen.himiuix.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;
import com.hchen.himiuix.widget.MiuiXRadioButton;

public class MiuiXRadioButtonItem extends MiuiXItem {
    private MiuiXRadioButton miuiXRadioButton;

    public MiuiXRadioButtonItem(@NonNull Context context) {
        this(context, null);
    }

    public MiuiXRadioButtonItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXRadioButtonItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXRadioButtonItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_radiobutton).setVisibility(View.VISIBLE);
    }

    @Override
    protected void loadItemView() {
        super.loadItemView();
        miuiXRadioButton = findViewById(R.id.miuix_item_radiobutton);
    }

    @Override
    protected void applyClickListener() {
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!miuiXRadioButton.isChecked()) {
                    miuiXRadioButton.setChecked(!miuiXRadioButton.isChecked());
                }
            }
        });
    }

    public void setChecked(boolean checked) {
        miuiXRadioButton.setChecked(checked);
    }

    public boolean isChecked() {
        return miuiXRadioButton.isChecked();
    }

    public MiuiXRadioButton getMiuiXRadioButton() {
        return miuiXRadioButton;
    }
}
