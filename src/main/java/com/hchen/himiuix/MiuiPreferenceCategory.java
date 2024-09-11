package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;

public class MiuiPreferenceCategory extends PreferenceGroup {
    String TAG = "MiuiPreference";
    ConstraintLayout layout;
    View dividerView;
    TextView textView;
    int noTipHeight;
    int haveTipHeight;
    boolean noDivider;

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference_Category);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
        setOrderingAsAdded(true);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_category);
        setSelectable(false);
        noTipHeight = MiuiXUtils.sp2px(context, 50);
        haveTipHeight = MiuiXUtils.sp2px(context, 63);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreferenceCategory,
                defStyleAttr, defStyleRes)) {
            noDivider = array.getBoolean(R.styleable.MiuiPreferenceCategory_noDivider, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        layout = (ConstraintLayout) holder.itemView;
        dividerView = layout.findViewById(R.id.category_divider);
        textView = layout.findViewById(R.id.category_tip);

        textView.setVisibility(View.GONE);
        dividerView.setVisibility(View.VISIBLE);
        setLayoutHeight(false);

        if (noDivider) {
            dividerView.setVisibility(View.GONE);
            haveTipHeight = MiuiXUtils.sp2px(getContext(), 33);
            noTipHeight = MiuiXUtils.sp2px(getContext(), 20);
            setLayoutHeight(false);
        }
        if (getTitle() != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getTitle());
            setLayoutHeight(true);
        }
    }

    private void setLayoutHeight(boolean haveTip) {
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = haveTip ? haveTipHeight : noTipHeight;
        layout.setLayoutParams(params);
    }
}
