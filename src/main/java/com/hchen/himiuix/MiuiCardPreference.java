package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceViewHolder;

public class MiuiCardPreference extends MiuiPreference {
    private ConstraintLayout mLayout;
    private ConstraintLayout mCustomLayout;
    private TextView mTittleView;
    private TextView mSummaryView;
    private ImageView mImageView;
    private float mTittleSize;
    private float mSummarySize;
    private int mBackgroundColor;
    private int TittleColor;
    private int mSummaryColor;
    private boolean mIconArrowRight;
    private boolean mIconCancel;
    private int mIconArrowRightColor;
    private int mIconCancelColor;
    private View mCustomView;
    private OnBindView onBindView;
    private View.OnClickListener mIconClickListener;

    public MiuiCardPreference(@NonNull Context context) {
        super(context);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_card);
        setSelectable(false);
        mCustomView = null;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiCardPreference,
                defStyleAttr, defStyleRes)) {
            mBackgroundColor = array.getColor(R.styleable.MiuiCardPreference_backgroundColor, context.getColor(R.color.card_background));
            mTittleSize = array.getDimension(R.styleable.MiuiCardPreference_tittleSize, 20);
            mSummarySize = array.getDimension(R.styleable.MiuiCardPreference_summarySize, 16);
            TittleColor = array.getColor(R.styleable.MiuiCardPreference_tittleColor, context.getColor(R.color.tittle));
            mSummaryColor = array.getColor(R.styleable.MiuiCardPreference_summaryColor, context.getColor(R.color.summary));
            mIconArrowRight = array.getBoolean(R.styleable.MiuiCardPreference_iconArrowRight, false);
            mIconCancel = array.getBoolean(R.styleable.MiuiCardPreference_iconCancel, false);
            mIconArrowRightColor = array.getColor(R.styleable.MiuiCardPreference_iconArrowRightColor, context.getColor(R.color.arrow_right));
            mIconCancelColor = array.getColor(R.styleable.MiuiCardPreference_iconCancelColor, context.getColor(R.color.cancel_background));
            int customViewId = array.getResourceId(R.styleable.MiuiCardPreference_customView, 0);
            if (customViewId != 0) {
                mCustomView = LayoutInflater.from(getContext()).inflate(customViewId, mCustomLayout, false);
            }
        }
    }

    public void setTittleSize(float size) {
        mTittleSize = size;
    }

    public void setSummarySize(float size) {
        mSummarySize = size;
    }

    public void setTittleColor(int color) {
        TittleColor = color;
    }

    public void setSummaryColor(int color) {
        mSummaryColor = color;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setIconArrowRight(boolean arrowRight) {
        mIconArrowRight = arrowRight;
    }

    public void setIconCancel(boolean cancel) {
        mIconCancel = cancel;
    }

    public void setIconArrowRightColor(int color) {
        mIconArrowRightColor = color;
    }

    public void setIconCancelColor(int color) {
        mIconCancelColor = color;
    }

    public void setCustomViewId(@LayoutRes int viewId) {
        mCustomView = LayoutInflater.from(getContext()).inflate(viewId, mCustomLayout, false);
    }

    public void setCustomView(View v) {
        mCustomView = v;
    }

    public void removeCustomView() {
        mCustomView = null;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mLayout = (ConstraintLayout) holder.itemView;
        mCustomLayout = mLayout.findViewById(R.id.card_custom_view);
        mTittleView = mLayout.findViewById(R.id.card_tittle);
        mSummaryView = mLayout.findViewById(R.id.card_summary);
        mImageView = mLayout.findViewById(R.id.card_image);

        mTittleView.setVisibility(View.GONE);
        mSummaryView.setVisibility(View.GONE);
        mImageView.setVisibility(View.GONE);
        mImageView.setOnClickListener(null);

        if (getTitle() != null) {
            mTittleView.setTextSize(mTittleSize);
            mTittleView.setTextColor(TittleColor);
            mTittleView.setText(getTitle());
            mTittleView.setVisibility(View.VISIBLE);
        }
        if (getSummary() != null) {
            mSummaryView.setTextSize(mSummarySize);
            mSummaryView.setTextColor(mSummaryColor);
            mSummaryView.setText(getSummary());
            mSummaryView.setVisibility(View.VISIBLE);
        }
        loadCustomLayout();
        Drawable drawable = mLayout.getBackground();
        drawable.setTint(mBackgroundColor);
        mLayout.setBackground(drawable);
        loadIcon();
    }

    private void loadCustomLayout() {
        if (mCustomView == null) {
            mCustomLayout.setVisibility(View.GONE);
        } else {
            mCustomLayout.setVisibility(View.VISIBLE);
            ViewGroup viewGroup = (ViewGroup) mCustomView.getParent();
            if (viewGroup != mCustomLayout) {
                if (viewGroup != null)
                    viewGroup.removeView(mCustomView);
                mCustomLayout.addView(mCustomView);
            }
            if (onBindView != null)
                onBindView.onBindView(mCustomView);
        }
    }

    private void loadIcon() {
        if (mIconArrowRight) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_preference_arrow_right);
            assert drawable != null;
            drawable.setTint(mIconArrowRightColor);
            mImageView.setImageDrawable(drawable);
            mImageView.setVisibility(View.VISIBLE);
        } else if (mIconCancel) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.miuix_button_cancel);
            assert drawable != null;
            drawable.setTint(mIconCancelColor);
            mImageView.setImageDrawable(drawable);
            mImageView.setVisibility(View.VISIBLE);
        } else if (getIcon() != null) {
            mImageView.setImageDrawable(getIcon());
            mImageView.setVisibility(View.VISIBLE);
        }
        if (mImageView.getVisibility() == View.VISIBLE) {
            mImageView.setOnClickListener(mIconClickListener);
        }
    }

    public void setCustomViewCallBack(OnBindView onBindView) {
        this.onBindView = onBindView;
    }

    public void setIconClickListener(View.OnClickListener clickListener) {
        mIconClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onClick(v);
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            }
        };
    }

    public interface OnBindView {
        void onBindView(View view);
    }
}
