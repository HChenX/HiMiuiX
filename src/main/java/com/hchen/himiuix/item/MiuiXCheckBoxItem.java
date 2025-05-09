package com.hchen.himiuix.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;
import com.hchen.himiuix.widget.MiuiXCheckBox;

public class MiuiXCheckBoxItem extends MiuiXItem {
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
    }

    @Override
    protected void updateIndicatorVisibility() {
        findViewById(R.id.miuix_item_custom_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_color_indicator).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_switch).setVisibility(View.GONE);
        findViewById(R.id.miuix_item_checkbox).setVisibility(View.VISIBLE);
        findViewById(R.id.miuix_item_radiobutton).setVisibility(View.GONE);
    }

    @Override
    protected void loadItemView() {
        super.loadItemView();
        checkBox = findViewById(R.id.miuix_item_checkbox);
    }

    @Override
    protected void applyClickListener() {
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public MiuiXCheckBox getCheckBox() {
        return checkBox;
    }
}
