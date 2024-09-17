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
    private ConstraintLayout layout;
    private ConstraintLayout customLayout;
    private TextView tittleView;
    private TextView summaryView;
    private ImageView imageView;
    private float tittleSize;
    private float summarySize;
    private int backgroundColor;
    private int tittleColor;
    private int summaryColor;
    private boolean iconArrowRight;
    private boolean iconCancel;
    private int iconArrowRightColor;
    private int iconCancelColor;
    private View customView;
    private CustomViewCallBack customViewCallBack;

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
        customView = null;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiCardPreference,
                defStyleAttr, defStyleRes)) {
            backgroundColor = array.getColor(R.styleable.MiuiCardPreference_backgroundColor, context.getColor(R.color.card_background));
            tittleSize = array.getDimension(R.styleable.MiuiCardPreference_tittleSize, 20);
            summarySize = array.getDimension(R.styleable.MiuiCardPreference_summarySize, 16);
            tittleColor = array.getColor(R.styleable.MiuiCardPreference_tittleColor, context.getColor(R.color.tittle));
            summaryColor = array.getColor(R.styleable.MiuiCardPreference_summaryColor, context.getColor(R.color.summary));
            iconArrowRight = array.getBoolean(R.styleable.MiuiCardPreference_iconArrowRight, false);
            iconCancel = array.getBoolean(R.styleable.MiuiCardPreference_iconCancel, false);
            iconArrowRightColor = array.getColor(R.styleable.MiuiCardPreference_iconArrowRightColor, context.getColor(R.color.arrow_right));
            iconCancelColor = array.getColor(R.styleable.MiuiCardPreference_iconCancelColor, context.getColor(R.color.cancel_background));
            int customViewId = array.getResourceId(R.styleable.MiuiCardPreference_customView, 0);
            if (customViewId != 0) {
                customView = LayoutInflater.from(getContext()).inflate(customViewId, customLayout, false);
            }
        }
    }

    public void setTittleSize(float size) {
        tittleSize = size;
    }

    public void setSummarySize(float size) {
        summarySize = size;
    }

    public void setTittleColor(int color) {
        tittleColor = color;
    }

    public void setSummaryColor(int color) {
        summaryColor = color;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    public void setIconArrowRight(boolean arrowRight) {
        iconArrowRight = arrowRight;
    }

    public void setIconCancel(boolean cancel) {
        iconCancel = cancel;
    }

    public void setIconArrowRightColor(int color) {
        iconArrowRightColor = color;
    }

    public void setIconCancelColor(int color) {
        iconCancelColor = color;
    }

    public void setCustomViewId(@LayoutRes int viewId) {
        customView = LayoutInflater.from(getContext()).inflate(viewId, customLayout, false);
    }

    public void setCustomView(View v) {
        customView = v;
    }

    public void removeCustomView() {
        customView = null;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        layout = (ConstraintLayout) holder.itemView;
        customLayout = layout.findViewById(R.id.card_custom_view);
        tittleView = layout.findViewById(R.id.card_tittle);
        summaryView = layout.findViewById(R.id.card_summary);
        imageView = layout.findViewById(R.id.card_image);

        tittleView.setVisibility(View.GONE);
        summaryView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        if (getTitle() != null) {
            tittleView.setTextSize(tittleSize);
            tittleView.setTextColor(tittleColor);
            tittleView.setText(getTitle());
            tittleView.setVisibility(View.VISIBLE);
        }
        if (getSummary() != null) {
            summaryView.setTextSize(summarySize);
            summaryView.setTextColor(summaryColor);
            summaryView.setText(getSummary());
            summaryView.setVisibility(View.VISIBLE);
        }
        loadCustomLayout();
        Drawable drawable = layout.getBackground();
        drawable.setTint(backgroundColor);
        layout.setBackground(drawable);
        setIcon();
    }

    private void loadCustomLayout() {
        if (customView == null) {
            customLayout.setVisibility(View.GONE);
        } else {
            customLayout.setVisibility(View.VISIBLE);
            ViewGroup viewGroup = (ViewGroup) customView.getParent();
            if (viewGroup != customLayout) {
                if (viewGroup != null)
                    viewGroup.removeView(customView);
                customLayout.addView(customView);
            }
            if (customViewCallBack != null)
                customViewCallBack.onCustomViewCreate(customView);
        }
    }

    private void setIcon() {
        if (iconArrowRight) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_preference_arrow_right);
            assert drawable != null;
            drawable.setTint(iconArrowRightColor);
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        } else if (iconCancel) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.miuix_button_cancel);
            assert drawable != null;
            drawable.setTint(iconCancelColor);
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        } else if (getIcon() != null) {
            imageView.setImageDrawable(getIcon());
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public void customViewCallBack(CustomViewCallBack customViewCallBack) {
        this.customViewCallBack = customViewCallBack;
    }

    public void setIconClickListener(View.OnClickListener clickListener) {
        if (imageView.getVisibility() == View.VISIBLE) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onClick(v);
                    v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                }
            });
        }
    }

    public interface CustomViewCallBack {
        void onCustomViewCreate(View view);
    }
}
