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
import android.view.ViewTreeObserver;
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
    private ConstraintLayout mTextConstraint;
    private ConstraintLayout mOnlyTextConstraint;
    private ColorSelectView mColorSelectView;
    private ImageView mIconView;
    private View mStartView;
    private TextView mTittleView;
    private TextView mOnlyTittleView;
    private TextView mSummaryView;
    private TextView mTipView;
    private ImageView mArrowRightView;
    private String mTipText = null;
    private int mViewId = 0;
    private String mDependencyKey;
    private ArrayList<MiuiPreference> mDependents = null;
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            miuiPrefClick(v);
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
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_preference);
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreference,
                defStyleAttr, defStyleRes)) {
            mTipText = typedArray.getString(R.styleable.MiuiPreference_tip);
        }
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
    }

    private int mOldTextHeight = -1;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mMainLayout = (ConstraintLayout) holder.itemView;
        mMainLayout.setOnClickListener(null);
        mMainLayout.setOnTouchListener(null);
        mMainLayout.setBackground(null);
        mMainLayout.setOnClickListener(mClickListener);
        mMainLayout.setId(mViewId);

        mStartView = holder.findViewById(R.id.pref_start);
        mIconView = (ImageView) holder.findViewById(R.id.prefs_icon);
        mTittleView = (TextView) holder.findViewById(R.id.prefs_text);
        mOnlyTittleView = (TextView) holder.findViewById(R.id.prefs_only_text);
        mSummaryView = (TextView) holder.findViewById(R.id.prefs_summary);
        mTipView = (TextView) holder.findViewById(R.id.pref_tip);
        mArrowRightView = (ImageView) holder.findViewById(R.id.pref_arrow_right);
        mTextConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_text_constraint);
        mOnlyTextConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_only_text_constraint);
        mColorSelectView = (ColorSelectView) holder.findViewById(R.id.pref_color_select);
        if (mColorSelectView != null) mColorSelectView.setVisibility(View.GONE);

        if (shouldShowSummary()) {
            setVisibility(true);
            mTittleView.setText(getTitle());
            mSummaryView.setText(getSummary());
            mSummaryView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mSummaryView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (mOldTextHeight == -1) mOldTextHeight = mSummaryView.getHeight();

                    int lineHeight = mSummaryView.getLineHeight();
                    int lineCount = mSummaryView.getLineCount();
                    int totalHeight = lineHeight * lineCount;

                    if (totalHeight > mOldTextHeight) {
                        ViewGroup.LayoutParams params = mTextConstraint.getLayoutParams();
                        params.height = MiuiXUtils.sp2px(getContext(), MiuiXUtils.px2sp(getContext(), (float) (totalHeight + mOldTextHeight / 1.85)));
                        mTextConstraint.setLayoutParams(params);
                    }
                }
            });
        } else {
            setVisibility(false);
            mOnlyTittleView.setText(getTitle());
        }
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

    private void setEnabledStateOnViews(@NonNull View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup vg) {
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void miuiPrefClick(View v) {
        performClick(v);
        if (!isEnabled() || !isSelectable())
            return;
        onClick(v);
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

    protected boolean disableArrowRightView() {
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

    private void setColor() {
        int tc, sc;
        if (isEnabled()) {
            tc = getContext().getColor(R.color.tittle);
            sc = getContext().getColor(R.color.summary);
        } else {
            tc = getContext().getColor(R.color.tittle_d);
            sc = getContext().getColor(R.color.summary_d);
        }
        mTittleView.setTextColor(tc);
        mOnlyTittleView.setTextColor(tc);
        mSummaryView.setTextColor(sc);
        if (mTipView != null)
            mTipView.setTextColor(sc);
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
        if (disableArrowRightView()) return;
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
                mStartView.setVisibility(View.GONE);
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mIconView.setImageDrawable(drawable);
            } else {
                mIconView.setVisibility(View.GONE);
                mStartView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setVisibility(boolean b) {
        if (b) {
            mTittleView.setVisibility(View.VISIBLE);
            mSummaryView.setVisibility(View.VISIBLE);
            mTextConstraint.setVisibility(View.VISIBLE);
            mOnlyTittleView.setVisibility(View.GONE);
            mOnlyTextConstraint.setVisibility(View.GONE);
        } else {
            mTittleView.setVisibility(View.GONE);
            mSummaryView.setVisibility(View.GONE);
            mTextConstraint.setVisibility(View.GONE);
            mOnlyTittleView.setVisibility(View.VISIBLE);
            mOnlyTextConstraint.setVisibility(View.VISIBLE);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        // Log.i(TAG, "onTouch finger: " + (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER)
        //         + " action: " + action);
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER)
            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.touch_down);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.setBackgroundResource(R.color.touch_up);
            }
        return false;
    }

    public boolean onHover(View v, MotionEvent event) {
        int action = event.getAction();
        // Log.i(TAG, "onTouch mouse: " + (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
        //         + " action: " + action);
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
            if (action == MotionEvent.ACTION_HOVER_MOVE) {
                v.setBackgroundResource(R.color.touch_down);
            } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
                v.setBackgroundResource(R.color.touch_up);
            }
        return false;
    }
}
