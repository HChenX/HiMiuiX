package com.hchen.himiuix;

import static com.hchen.himiuix.MiuiXUtils.sp2px;

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
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;

/** @noinspection FieldCanBeLocal */
public class MiuiPreference extends Preference {
    String TAG = "MiuiPreference";
    ConstraintLayout mainLayout;
    ConstraintLayout textConstraint;
    ConstraintLayout onlyTextConstraint;
    Context context;
    ImageView icon;
    TextView tittle;
    TextView onlyTittle;
    TextView summary;
    TextView tipView;
    ImageView arrowRight;
    String tip = null;
    boolean loadArrowRight;
    private int mViewId = 0;
    private String mDependencyKey;
    private ArrayList<MiuiPreference> mDependents = new ArrayList<>();
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onClick(View v) {
            customClick(v);
        }
    };

    public MiuiPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = getContext();
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_preference);
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiPreference,
                defStyleAttr, defStyleRes)) {
            tip = typedArray.getString(R.styleable.MiuiPreference_tip);
        }
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    @Override
    public void onAttached() {
        registerDependency();
    }

    @Override
    protected void notifyChanged() {
        super.notifyChanged();
        onDependencyChange();
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
            setVisible(preference.shouldDisableDependents());
            onDependencyChanged(this, preference.shouldDisableDependents());
            preference.mDependents.add(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey
                    + "\" not found for preference \"" + getKey() + "\" (title: \"" + getTitle() + "\"");
        }
    }

    private void onDependencyChange() {
        if (mDependents.isEmpty()) return;
        for (MiuiPreference preference : mDependents) {
            preference.setVisible(shouldDisableDependents());
        }
    }

    private void unregisterDependency() {
        mDependencyKey = getDependency();
        if (mDependencyKey == null) return;
        MiuiPreference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null)
            preference.mDependents.remove(this);
    }

    int textHeight = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mainLayout = (ConstraintLayout) holder.itemView;
        mainLayout.setOnClickListener(null);
        mainLayout.setOnTouchListener(null);
        mainLayout.setBackground(null);
        mainLayout.setOnClickListener(mClickListener);
        mainLayout.setId(mViewId);

        icon = (ImageView) holder.findViewById(R.id.prefs_icon);
        tittle = (TextView) holder.findViewById(R.id.prefs_text);
        onlyTittle = (TextView) holder.findViewById(R.id.prefs_only_text);
        summary = (TextView) holder.findViewById(R.id.prefs_summary);
        tipView = (TextView) holder.findViewById(R.id.pref_tip);
        arrowRight = (ImageView) holder.findViewById(R.id.pref_arrow_right);
        textConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_text_constraint);
        onlyTextConstraint = (ConstraintLayout) holder.findViewById(R.id.pref_only_text_constraint);

        if (needSummary()) {
            setVisibility(true);
            tittle.setText(getTitle());
            summary.setText(getSummary());
            summary.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    summary.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (textHeight == -1) textHeight = summary.getHeight();

                    int lineHeight = summary.getLineHeight();
                    int lineCount = summary.getLineCount();
                    int totalHeight = lineHeight * lineCount;

                    if (totalHeight > textHeight) {
                        ViewGroup.LayoutParams params = textConstraint.getLayoutParams();
                        params.height = MiuiXUtils.sp2px(context, MiuiXUtils.px2sp(context, (float) (totalHeight + textHeight / 1.85)));
                        textConstraint.setLayoutParams(params);
                    }
                }
            });
        } else {
            setVisibility(false);
            onlyTittle.setText(getTitle());
        }
        if (loadArrowRight) loadArrowRight();
        loadIcon(getIcon());
        loadTipView();
        setColor();
        if (isEnabled())
            mainLayout.setOnTouchListener(MiuiPreference.this::onTouch);
        mainLayout.setClickable(isSelectable());
        mainLayout.setFocusable(isSelectable());
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }

    @SuppressLint("RestrictedApi")
    private void customClick(View v) {
        performClick(v);
        if (!isEnabled() || !isSelectable())
            return;
        onClick(v);
    }

    protected void onClick(View view) {
    }

    protected boolean needSummary() {
        return getSummary() != null;
    }

    private void setColor() {
        int tc, sc;
        if (isEnabled()) {
            tc = context.getColor(R.color.tittle);
            sc = context.getColor(R.color.summary);
        } else {
            tc = context.getColor(R.color.tittle_d);
            sc = context.getColor(R.color.summary_d);
        }
        tittle.setTextColor(tc);
        onlyTittle.setTextColor(tc);
        summary.setTextColor(sc);
        if (tipView != null)
            tipView.setTextColor(sc);
    }

    private void loadTipView() {
        if (tipView != null)
            if (tip == null) {
                tipView.setVisibility(View.GONE);
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setText(tip);
            }
    }

    private void loadArrowRight() {
        if (arrowRight == null)
            return;
        arrowRight.setVisibility(View.GONE);
        if (getFragment() != null || getOnPreferenceChangeListener() != null ||
                getOnPreferenceClickListener() != null || getIntent() != null) {
            arrowRight.setVisibility(View.VISIBLE);
            if (!isEnabled())
                arrowRight.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                        R.drawable.ic_preference_arrow_right_disable, context.getTheme()));
            else arrowRight.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.ic_preference_arrow_right, context.getTheme()));
        }
    }

    private void loadIcon(Drawable drawable) {
        if (icon != null)
            if (drawable != null) {
                icon.setVisibility(View.VISIBLE);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                icon.setImageDrawable(drawable);
            } else {
                icon.setVisibility(View.INVISIBLE);
                icon.setPadding(sp2px(context, 25), 0, sp2px(context, 5), 0);
            }
    }

    private void setVisibility(boolean b) {
        if (b) {
            tittle.setVisibility(View.VISIBLE);
            summary.setVisibility(View.VISIBLE);
            onlyTittle.setVisibility(View.GONE);
            textConstraint.setVisibility(View.VISIBLE);
            onlyTextConstraint.setVisibility(View.GONE);
        } else {
            tittle.setVisibility(View.GONE);
            summary.setVisibility(View.GONE);
            onlyTittle.setVisibility(View.VISIBLE);
            textConstraint.setVisibility(View.GONE);
            onlyTextConstraint.setVisibility(View.VISIBLE);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.touch_down);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundResource(R.color.touch_up);
        }
        return false;
    }
}
