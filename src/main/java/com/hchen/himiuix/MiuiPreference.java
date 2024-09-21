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
    private ConstraintLayout mainLayout;
    private ConstraintLayout textConstraint;
    private ConstraintLayout onlyTextConstraint;
    private ColorSelectView colorSelectView;
    private ImageView iconView;
    private View startView;
    private TextView tittleView;
    private TextView onlyTittleView;
    private TextView summaryView;
    private TextView tipView;
    private ImageView arrowRightView;
    private String tipText = null;
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
            tipText = typedArray.getString(R.styleable.MiuiPreference_tip);
        }
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    private int textHeight = -1;

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getPreferenceManager().setSharedPreferencesName(getContext().getString(R.string.prefs_name));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mainLayout = (ConstraintLayout) holder.itemView;
        mainLayout.setOnClickListener(null);
        mainLayout.setOnTouchListener(null);
        mainLayout.setBackground(null);
        mainLayout.setOnClickListener(mClickListener);
        mainLayout.setId(mViewId);

        startView = holder.findViewById(R.id.pref_start);
        iconView = (ImageView) holder.findViewById(R.id.prefs_icon);
        tittleView = (TextView) holder.findViewById(R.id.prefs_text);
        onlyTittleView = (TextView) holder.findViewById(R.id.prefs_only_text);
        summaryView = (TextView) holder.findViewById(R.id.prefs_summary);
        tipView = (TextView) holder.findViewById(R.id.pref_tip);
        arrowRightView = (ImageView) holder.findViewById(R.id.pref_arrow_right);
        textConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_text_constraint);
        onlyTextConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_only_text_constraint);
        colorSelectView = (ColorSelectView) holder.findViewById(R.id.pref_color_select);
        if (colorSelectView != null) colorSelectView.setVisibility(View.GONE);

        if (shouldShowSummary()) {
            setVisibility(true);
            tittleView.setText(getTitle());
            summaryView.setText(getSummary());
            summaryView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    summaryView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (textHeight == -1) textHeight = summaryView.getHeight();

                    int lineHeight = summaryView.getLineHeight();
                    int lineCount = summaryView.getLineCount();
                    int totalHeight = lineHeight * lineCount;

                    if (totalHeight > textHeight) {
                        ViewGroup.LayoutParams params = textConstraint.getLayoutParams();
                        params.height = MiuiXUtils.sp2px(getContext(), MiuiXUtils.px2sp(getContext(), (float) (totalHeight + textHeight / 1.85)));
                        textConstraint.setLayoutParams(params);
                    }
                }
            });
        } else {
            setVisibility(false);
            onlyTittleView.setText(getTitle());
        }
        loadArrowRight();
        loadIcon(getIcon());
        loadTipView();
        setColor();
        if (isEnabled()) {
            mainLayout.setOnTouchListener(MiuiPreference.this::onTouch);
            mainLayout.setOnHoverListener(MiuiPreference.this::onHover);
        }
        mainLayout.setClickable(isSelectable());
        mainLayout.setFocusable(isSelectable());
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        if (getShouldDisableView()) {
            setEnabledStateOnViews(mainLayout, isEnabled());
        } else {
            setEnabledStateOnViews(mainLayout, true);
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
        return mainLayout;
    }

    protected TextView getSummaryView() {
        return summaryView;
    }

    protected ImageView getArrowRightView() {
        return arrowRightView;
    }

    protected ColorSelectView getColorSelectView() {
        return colorSelectView;
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
        tittleView.setTextColor(tc);
        onlyTittleView.setTextColor(tc);
        summaryView.setTextColor(sc);
        if (tipView != null)
            tipView.setTextColor(sc);
    }

    private void loadTipView() {
        if (tipView != null) {
            if (tipText == null) {
                tipView.setVisibility(View.GONE);
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setText(tipText);
            }
        }
    }

    private void loadArrowRight() {
        if (arrowRightView == null) return;
        arrowRightView.setVisibility(View.GONE);
        if (disableArrowRightView()) return;
        if (getFragment() != null || getOnPreferenceChangeListener() != null ||
                getOnPreferenceClickListener() != null || getIntent() != null) {
            arrowRightView.setVisibility(View.VISIBLE);
            if (isEnabled())
                arrowRightView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(),
                        R.drawable.ic_preference_arrow_right, getContext().getTheme()));
            else
                arrowRightView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(),
                        R.drawable.ic_preference_arrow_right_disable, getContext().getTheme()));
        }
    }

    private void loadIcon(Drawable drawable) {
        if (iconView != null) {
            if (drawable != null) {
                drawable.setAlpha(isEnabled() ? 255 : 125);
                startView.setVisibility(View.GONE);
                iconView.setVisibility(View.VISIBLE);
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setImageDrawable(drawable);
            } else {
                iconView.setVisibility(View.GONE);
                startView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setVisibility(boolean b) {
        if (b) {
            tittleView.setVisibility(View.VISIBLE);
            summaryView.setVisibility(View.VISIBLE);
            textConstraint.setVisibility(View.VISIBLE);
            onlyTittleView.setVisibility(View.GONE);
            onlyTextConstraint.setVisibility(View.GONE);
        } else {
            tittleView.setVisibility(View.GONE);
            summaryView.setVisibility(View.GONE);
            textConstraint.setVisibility(View.GONE);
            onlyTittleView.setVisibility(View.VISIBLE);
            onlyTextConstraint.setVisibility(View.VISIBLE);
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
