package com.hchen.himiuix.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

public class MiuiXItemGroup extends LinearLayout {
    private String dividerTitle = null;

    public MiuiXItemGroup(Context context) {
        this(context, null);
    }

    public MiuiXItemGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXItemGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXItemGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiXItemGroup, defStyleAttr, defStyleRes)) {
            dividerTitle = array.getString(R.styleable.MiuiXItemGroup_dividerTitle);
        }
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.miuix_divider, this, true);

        updateDivider();
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
        updateGroupBackground();
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        boolean result = super.addViewInLayout(child, index, params);
        updateGroupBackground();
        return result;
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        boolean result = super.addViewInLayout(child, index, params, preventRequestLayout);
        updateGroupBackground();
        return result;
    }

    @Override
    public void bringChildToFront(View child) {
        super.bringChildToFront(child);
        updateGroupBackground();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateGroupBackground();
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        updateGroupBackground();
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        updateGroupBackground();
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateGroupBackground();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateGroupBackground();
    }

    @Nullable
    public String getDividerTitle() {
        return dividerTitle;
    }

    public MiuiXItemGroup setDividerTitle(@Nullable String dividerTitle) {
        this.dividerTitle = dividerTitle;
        updateDivider();
        return this;
    }

    private void updateDivider() {
        TextView textView = findViewById(R.id.miuix_divider_title);
        View view = findViewById(R.id.miuix_divider);
        if (dividerTitle == null) {
            textView.setVisibility(GONE);
            view.setVisibility(VISIBLE);
        } else {
            textView.setText(dividerTitle);
            textView.setVisibility(VISIBLE);
            view.setVisibility(GONE);
        }

        invalidate();
        requestLayout();
    }

    private void updateGroupBackground() {
        if (getChildCount() == 1) return;
        if (getChildCount() >= 2) {
            View firstItemView = null;
            View endItemView = getChildAt(getChildCount() - 1).findViewById(R.id.miuix_item);
            endItemView.setBackgroundResource(R.drawable.rounded_background_bottom_r_l);

            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i).findViewById(R.id.miuix_item) == null) continue;
                if (i == getChildCount() - 1) break;

                if (firstItemView == null) {
                    firstItemView = getChildAt(i);
                    firstItemView.findViewById(R.id.miuix_item).setBackgroundResource(R.drawable.rounded_background_top_r_l);
                    continue;
                }

                View v = getChildAt(i).findViewById(R.id.miuix_item);
                v.setBackgroundResource(R.drawable.not_rounded_background);
            }
        }

        invalidate();
        requestLayout();
    }
}
