package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceViewHolder;

public class MiuiCardPreference extends MiuiPreference {
    ConstraintLayout layout;
    ConstraintLayout textLayout;
    TextView tittleView;
    TextView summaryView;
    ImageView imageView;
    public float tittleSize;
    public float summarySize;
    public int backgroundColor;
    public int tittleColor;
    public int summaryColor;
    public boolean iconArrowRight;
    public boolean iconCancel;
    public int iconArrowRightColor;
    public int iconCancelColor;

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
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_card);
        setSelectable(false);
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
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        layout = (ConstraintLayout) holder.itemView;
        textLayout = layout.findViewById(R.id.card_text_view);
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
        Drawable drawable = layout.getBackground();
        drawable.setTint(backgroundColor);
        layout.setBackground(drawable);
        setIcon();
    }

    private void setIcon() {
        if (iconArrowRight) {
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_preference_arrow_right);
            assert drawable != null;
            drawable.setTint(iconArrowRightColor);
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        } else if (iconCancel) {
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.miuix_button_cancel);
            assert drawable != null;
            drawable.setTint(iconCancelColor);
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        } else if (getIcon() != null) {
            imageView.setImageDrawable(getIcon());
            imageView.setVisibility(View.VISIBLE);
        }
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
}
