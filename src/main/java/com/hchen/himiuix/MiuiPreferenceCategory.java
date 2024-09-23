/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * HiMiuiX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 HiMiuiX Contributions
 */
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
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

public class MiuiPreferenceCategory extends PreferenceGroup {
    String TAG = "MiuiPreference";
    private ConstraintLayout mLayout;
    private View mDividerView;
    private TextView mTextView;
    private int mNoTipHeight;
    private int mHaveTipHeight;
    private boolean shouldGoneDivider;

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference_Category);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_category);
        setSelectable(false);
        setPersistent(false);
        mNoTipHeight = MiuiXUtils.sp2px(context, 50);
        mHaveTipHeight = MiuiXUtils.sp2px(context, 63);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreferenceCategory,
                defStyleAttr, defStyleRes)) {
            shouldGoneDivider = array.getBoolean(R.styleable.MiuiPreferenceCategory_goneDivider, false);
        }
    }

    public void setGoneDivider(boolean goneDivider) {
        this.shouldGoneDivider = goneDivider;
    }

    public boolean isGoneDivider() {
        return shouldGoneDivider;
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return !super.isEnabled();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mLayout = (ConstraintLayout) holder.itemView;
        mDividerView = mLayout.findViewById(R.id.category_divider);
        mTextView = mLayout.findViewById(R.id.category_tip);

        mTextView.setVisibility(View.GONE);
        mDividerView.setVisibility(View.VISIBLE);
        setLayoutHeight(false);

        if (shouldGoneDivider) {
            mDividerView.setVisibility(View.GONE);
            mHaveTipHeight = MiuiXUtils.sp2px(getContext(), 33);
            mNoTipHeight = MiuiXUtils.sp2px(getContext(), 20);
            setLayoutHeight(false);
        }
        if (getTitle() != null) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(getTitle());
            setLayoutHeight(true);
        }
    }

    private void setLayoutHeight(boolean haveTip) {
        ViewGroup.LayoutParams params = mLayout.getLayoutParams();
        params.height = haveTip ? mHaveTipHeight : mNoTipHeight;
        mLayout.setLayoutParams(params);
    }
}
