package com.hchen.himiuix;

import android.content.Context;
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
    View view;
    TextView textView;
    int noTipHeight;
    int haveTipHeight;

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference_Category);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.miuix_category);
        setSelectable(false);
        noTipHeight = MiuiXUtils.sp2px(context, 50);
        haveTipHeight = MiuiXUtils.sp2px(context, 63);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        layout = (ConstraintLayout) holder.itemView;
        view = layout.findViewById(R.id.category_divider);
        textView = layout.findViewById(R.id.category_tip);

        textView.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
        setLayoutHeight(false);

        if (getTitle() != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getTitle());
            setLayoutHeight(true);
        }
    }

    private void setLayoutHeight(boolean haveTip) {
        ViewGroup.LayoutParams params =  layout.getLayoutParams();
        params.height = haveTip ? haveTipHeight : noTipHeight;
        layout.setLayoutParams(params);
    }
}
