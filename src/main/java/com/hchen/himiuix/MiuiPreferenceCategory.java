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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import java.util.List;

public class MiuiPreferenceCategory extends PreferenceGroup {
    final static String TAG = "MiuiPreference";
    private ConstraintLayout mLayout;
    private View mDividerView;
    private TextView mTextView;
    private boolean shouldGoneDivider;

    public MiuiPreferenceCategory(@NonNull Context context) {
        this(context, null);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference_Category);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.miuix_category);
        setSelectable(false);
        setPersistent(false);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreferenceCategory,
            defStyleAttr, defStyleRes)) {
            shouldGoneDivider = array.getBoolean(R.styleable.MiuiPreferenceCategory_goneDivider, true);
        }
    }

    public void setGoneDivider(boolean goneDivider) {
        this.shouldGoneDivider = goneDivider;
        notifyChanged();
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
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mLayout = (ConstraintLayout) holder.itemView;
        mDividerView = mLayout.findViewById(R.id.category_divider);
        mTextView = mLayout.findViewById(R.id.category_tip);

        mTextView.setVisibility(View.GONE);
        mDividerView.setVisibility(View.VISIBLE);

        if (shouldGoneDivider) mDividerView.setVisibility(View.GONE);
        if (getTitle() != null) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(getTitle());
        }
    }

    @Override
    protected void notifyHierarchyChanged() {
        List<MiuiPreference> mPreferences = InvokeUtils.getField(this, "mPreferences");

        boolean isFirst = true;
        for (int i = 0; i < mPreferences.size() - 1; i++) {
            MiuiPreference miuiPreference = mPreferences.get(i);
            if (isFirst) {
                miuiPreference.updateBackground(-1, true, false);
                isFirst = false;
                continue;
            }
            miuiPreference.updateBackground(-1, false, false);
        }

        if (mPreferences.isEmpty()) return;
        mPreferences.get(mPreferences.size() - 1).updateBackground(-1, false, true);

        mPreferences.forEach(
            miuiPreference ->
                miuiPreference.updateCount(mPreferences.size())
        );
    }
}
