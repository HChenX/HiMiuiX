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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.colorpicker.ColorSelectView;

import java.util.ArrayList;

public class MiuiPreference extends Preference {
    static String TAG = "MiuiPreference";
    private ConstraintLayout mMainLayout;
    private ColorSelectView mColorSelectView;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;
    private TextView mTipView;
    private ImageView mArrowRightView;
    private String mTipText;
    private int mViewId = -1;
    private String mDependencyKey;
    private ArrayList<MiuiPreference> mDependents = null;
    private boolean isFirst;
    private boolean isLast;
    private int mCount = 1;
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

        setLayoutResource(R.layout.miuix_preference);
        try (
            TypedArray typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.MiuiPreference,
                defStyleAttr,
                defStyleRes
            )
        ) {
            mTipText = typedArray.getString(R.styleable.MiuiPreference_tip);
        }
    }

    public void setViewId(int viewId) {
        if (mMainLayout.getId() == viewId && mViewId == viewId)
            return;

        mViewId = viewId;
        notifyChanged();
    }

    public void setTipText(String tipText) {
        if (TextUtils.equals(mTipText, tipText))
            return;

        mTipText = tipText;
        notifyChanged();
    }

    public int getId() {
        return mViewId;
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
        // mMainLayout.setBackground(null);
        updateBackground(-1, isFirst, isLast);
        mMainLayout.setOnClickListener(mClickListener);
        if (mViewId != -1)
            mMainLayout.setId(mViewId);

        mIconView = (ImageView) holder.findViewById(R.id.pref_icon);
        mTitleView = (TextView) holder.findViewById(R.id.prefs_text);
        mSummaryView = (TextView) holder.findViewById(R.id.prefs_summary);
        mTipView = (TextView) holder.findViewById(R.id.pref_tip);
        mArrowRightView = (ImageView) holder.findViewById(R.id.pref_arrow_right);
        mColorSelectView = (ColorSelectView) holder.findViewById(R.id.pref_color_select);

        mTitleView.setText(getTitle());
        updateSummaryIfNeed();
        loadArrowRightIfNeed();
        loadColorSelectViewIfNeed();
        loadIconIfNeed(getIcon());
        loadTipViewIfNeed();
        updateTextColorIfNeed();

        if (isEnabled()) {
            mMainLayout.setOnTouchListener(MiuiPreference.this::onMainLayoutTouch);
            mMainLayout.setOnHoverListener(MiuiPreference.this::onMainLayoutHover);
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
        mSummaryView.setVisibility(GONE);
        if (shouldShowSummary()) {
            mSummaryView.setVisibility(VISIBLE);
            mSummaryView.setText(getSummary());
        }
    }

    private void updateTextColorIfNeed() {
        float titleAlpha, summaryAlpha;
        if (isEnabled()) {
            titleAlpha = 1f;
            summaryAlpha = 1f;
        } else {
            titleAlpha = 0.5f;
            summaryAlpha = 0.5f;
        }

        mTitleView.setAlpha(titleAlpha);
        mSummaryView.setAlpha(summaryAlpha);
        if (mTipView != null)
            mTipView.setAlpha(summaryAlpha);
    }

    private void loadTipViewIfNeed() {
        if (mTipView != null) {
            if (mTipText == null) {
                mTipView.setVisibility(GONE);
            } else {
                mTipView.setVisibility(VISIBLE);
                mTipView.setText(mTipText);
            }
        }
    }

    private void loadColorSelectViewIfNeed() {
        if (mColorSelectView == null) return;
        if (!shouldShowColorSelectView()) {
            mColorSelectView.setVisibility(GONE);
            return;
        }

        mColorSelectView.setVisibility(VISIBLE);
    }

    private void loadArrowRightIfNeed() {
        if (mArrowRightView == null) return;
        mArrowRightView.setVisibility(GONE);
        if (shouldDisableArrowRightView()) return;

        if (
            getFragment() != null ||
                getOnPreferenceChangeListener() != null ||
                getOnPreferenceClickListener() != null ||
                getIntent() != null
        ) {
            mArrowRightView.setVisibility(VISIBLE);
            if (isEnabled()) {
                mArrowRightView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        getContext().getResources(),
                        R.drawable.ic_preference_arrow_right,
                        getContext().getTheme()
                    )
                );
            } else {
                mArrowRightView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        getContext().getResources(),
                        R.drawable.ic_preference_arrow_right_disable,
                        getContext().getTheme()
                    )
                );
            }
        }
    }

    private void loadIconIfNeed(Drawable drawable) {
        if (mIconView != null) {
            if (drawable != null) {
                drawable.setAlpha(isEnabled() ? 255 : 125);
                mIconView.setVisibility(VISIBLE);
                mIconView.setImageDrawable(drawable);

                mMainLayout.setPadding(
                    MiuiXUtils.dp2px(getContext(), 10),
                    mMainLayout.getPaddingTop(),
                    mMainLayout.getPaddingEnd(),
                    mMainLayout.getPaddingBottom()
                );
            } else
                mIconView.setVisibility(GONE);
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

    ConstraintLayout getMainLayout() {
        return mMainLayout;
    }

    TextView getSummaryView() {
        return mSummaryView;
    }

    ImageView getArrowRightView() {
        return mArrowRightView;
    }

    ColorSelectView getColorSelectView() {
        return mColorSelectView;
    }

    boolean shouldShowSummary() {
        return getSummary() != null;
    }

    boolean shouldDisableArrowRightView() {
        return false;
    }

    boolean shouldShowColorSelectView() {
        return false;
    }

    protected void onClick(View view) {
    }

    void updateCount(int count) {
        mCount = count;
    }

    void updateBackground(@ColorRes int color) {
        updateBackground(mMainLayout, color);
    }

    void updateBackground(View layout, @ColorRes int color) {
        updateBackground(layout, color, isFirst, isLast);
    }

    void updateBackground(@ColorRes int color, boolean isFirst, boolean isLast) {
        updateBackground(mMainLayout, color, isFirst, isLast);
    }

    void updateBackground(View layout, @ColorRes int color, boolean isFirst, boolean isLast) {
        this.isFirst = isFirst;
        this.isLast = isLast;
        if (layout == null) return;

        GradientDrawable drawable = (GradientDrawable) (
            mCount == 1 ? ContextCompat.getDrawable(getContext(), R.drawable.rounded_background_r_l) :
                isFirst ? ContextCompat.getDrawable(getContext(), R.drawable.rounded_background_top_r_l) :
                    isLast ? ContextCompat.getDrawable(getContext(), R.drawable.rounded_background_bottom_r_l) :
                        ContextCompat.getDrawable(getContext(), R.drawable.not_rounded_background)
        );
        if (drawable == null) return;

        if (color != -1) {
            drawable.setColor(getContext().getColor(color));
        }
        layout.invalidate();
        layout.setBackground(drawable);
    }

    @Override
    public void onAttached() {
        registerDependency();
    }

    @Override
    public void onDetached() {
        unregisterDependency();
        InvokeUtils.setField(this, "mWasDetached", true);
    }

    @Override
    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    @Override
    public void setDependency(@Nullable String dependencyKey) {
        unregisterDependency();

        InvokeUtils.setField(this, "mDependencyKey", dependencyKey);
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

    boolean onMainLayoutTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {
            if (action == MotionEvent.ACTION_DOWN) {
                updateBackground(R.color.touch_down, isFirst, isLast);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                updateBackground(R.color.touch_up, isFirst, isLast);
            }
        }
        return false;
    }

    boolean onMainLayoutHover(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
            if (action == MotionEvent.ACTION_HOVER_MOVE) {
                updateBackground(R.color.touch_down, isFirst, isLast);
            } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
                updateBackground(R.color.touch_up, isFirst, isLast);
            }
        }
        return false;
    }
}
