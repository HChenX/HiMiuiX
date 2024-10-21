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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.colorpicker.ColorSelectView;

import java.util.ArrayList;

public class MiuiPreference extends Preference {
    protected static String TAG = "MiuiPreference";
    private ConstraintLayout mMainLayout;
    private ColorSelectView mColorSelectView;
    private ImageView mIconView;
    private TextView mTittleView;
    private TextView mSummaryView;
    private TextView mTipView;
    private ImageView mArrowRightView;
    private String mTipText = null;
    private int mViewId = 0;
    private String mDependencyKey;
    private ArrayList<MiuiPreference> mDependents = null;
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        @SuppressLint("RestrictedApi")
        public void onClick(View v) {
            performClick(v);
            if (!isEnabled() || !isSelectable())
                return;
            MiuiPreference.this.onClick(v);
        }
    };

    public MiuiPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    // 一些初始化操作
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_preference);
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreference,
                defStyleAttr, defStyleRes)) {
            mTipText = typedArray.getString(R.styleable.MiuiPreference_tip);
        }
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
        notifyChanged();
    }

    public void setTipText(String tipText) {
        mTipText = tipText;
        notifyChanged();
    }

    public String getTipText() {
        return mTipText;
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mMainLayout = (ConstraintLayout) holder.itemView;
        mMainLayout.setOnTouchListener(null);
        mMainLayout.setOnHoverListener(null);
        mMainLayout.setBackground(null);
        mMainLayout.setOnClickListener(mClickListener);
        mMainLayout.setId(mViewId);

        mIconView = (ImageView) holder.findViewById(R.id.prefs_icon);
        mTittleView = (TextView) holder.findViewById(R.id.prefs_text);
        mSummaryView = (TextView) holder.findViewById(R.id.prefs_summary);
        mTipView = (TextView) holder.findViewById(R.id.pref_tip);
        mArrowRightView = (ImageView) holder.findViewById(R.id.pref_arrow_right);
        mColorSelectView = (ColorSelectView) holder.findViewById(R.id.pref_color_select);
        if (mColorSelectView != null) mColorSelectView.setVisibility(View.GONE);
        
        mTittleView.setText(getTitle());
        updateSummaryIfNeed();
        loadArrowRight();
        loadIcon(getIcon());
        loadTipView();
        setColor();

        if (isEnabled()) {
            mMainLayout.setOnTouchListener(MiuiPreference.this::onTouch);
            mMainLayout.setOnHoverListener(MiuiPreference.this::onHover);
        }
        mMainLayout.setClickable(isSelectable());
        mMainLayout.setFocusable(isSelectable());
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        if (getShouldDisableView()) {
            setEnabledStateOnViews(mMainLayout, isEnabled());
        } else {
            setEnabledStateOnViews(mMainLayout, true);
        }
    }

    private void updateSummaryIfNeed() {
        mSummaryView.setVisibility(View.GONE);
        if (shouldShowSummary()) {
            mSummaryView.setVisibility(View.VISIBLE);
            mSummaryView.setText(getSummary());
        }
    }

    private void setColor() {
        int titleColor, summaryOrTipColor;
        if (isEnabled()) {
            titleColor = getContext().getColor(R.color.tittle);
            summaryOrTipColor = getContext().getColor(R.color.summary);
        } else {
            titleColor = getContext().getColor(R.color.tittle_d);
            summaryOrTipColor = getContext().getColor(R.color.summary_d);
        }
        mTittleView.setTextColor(titleColor);
        mSummaryView.setTextColor(summaryOrTipColor);
        if (mTipView != null)
            mTipView.setTextColor(summaryOrTipColor);
    }

    private void loadTipView() {
        if (mTipView != null) {
            if (mTipText == null) {
                mTipView.setVisibility(View.GONE);
            } else {
                mTipView.setVisibility(View.VISIBLE);
                mTipView.setText(mTipText);
            }
        }
    }

    private void loadArrowRight() {
        if (mArrowRightView == null) return;
        mArrowRightView.setVisibility(View.GONE);
        if (shouldDisableArrowRightView()) return;
        if (getFragment() != null || getOnPreferenceChangeListener() != null ||
                getOnPreferenceClickListener() != null || getIntent() != null) {
            mArrowRightView.setVisibility(View.VISIBLE);
            if (isEnabled())
                mArrowRightView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(),
                        R.drawable.ic_preference_arrow_right, getContext().getTheme()));
            else
                mArrowRightView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(),
                        R.drawable.ic_preference_arrow_right_disable, getContext().getTheme()));
        }
    }

    private void loadIcon(Drawable drawable) {
        if (mIconView != null) {
            if (drawable != null) {
                drawable.setAlpha(isEnabled() ? 255 : 125);
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageDrawable(drawable);
            } else
                mIconView.setVisibility(View.GONE);
        }
    }

    private void setEnabledStateOnViews(@NonNull View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup vg) {
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    protected void onClick(View view) {
    }

    protected boolean shouldShowSummary() {
        return getSummary() != null;
    }

    protected ConstraintLayout getMiuiPrefMainLayout() {
        return mMainLayout;
    }

    protected TextView getSummaryView() {
        return mSummaryView;
    }

    protected ImageView getArrowRightView() {
        return mArrowRightView;
    }

    protected ColorSelectView getColorSelectView() {
        return mColorSelectView;
    }

    protected boolean shouldDisableArrowRightView() {
        return false;
    }

    @Override
    public void onAttached() {
        registerDependency();
    }

    @Override
    public void onDetached() {
        unregisterDependency();
        InvokeUtils.setField(this,
                "mWasDetached", true);
    }

    @Override
    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    @Override
    public void setDependency(@Nullable String dependencyKey) {
        unregisterDependency();
        InvokeUtils.setField(this,
                "mDependencyKey", dependencyKey);
        registerDependency();
    }

    private void registerDependency() {
        mDependencyKey = getDependency();
        if (mDependencyKey == null) return;
        MiuiPreference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null) {
            if (preference.mDependents == null) preference.mDependents = new ArrayList<>();
            setVisible(!preference.shouldDisableDependents());
            onDependencyChanged(this, preference.shouldDisableDependents());
            preference.mDependents.add(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey
                    + "\" not found for preference \"" + getKey() + "\" (title: \"" + getTitle() + "\"");
        }
    }

    @Override
    public void notifyDependencyChange(boolean disableDependents) {
        final ArrayList<MiuiPreference> dependents = mDependents;

        if (dependents == null) {
            return;
        }

        for (MiuiPreference preference : dependents) {
            preference.setVisible(!shouldDisableDependents());
            preference.onDependencyChanged(this, disableDependents);
        }
    }

    private void unregisterDependency() {
        mDependencyKey = getDependency();
        if (mDependencyKey == null) return;
        MiuiPreference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null)
            preference.mDependents.remove(this);
    }

    protected boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER)
            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.touch_down);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.setBackgroundResource(R.color.touch_up);
            }
        return false;
    }

    protected boolean onHover(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
            if (action == MotionEvent.ACTION_HOVER_MOVE) {
                v.setBackgroundResource(R.color.touch_down);
            } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
                v.setBackgroundResource(R.color.touch_up);
            }
        return false;
    }
}
