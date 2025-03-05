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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceViewHolder;

public class MiuiCardPreference extends MiuiPreference {
    private ConstraintLayout mBackgroundLayout;
    private ConstraintLayout mCustomLayout;
    private ImageView mImageView;
    private int mBackgroundColor;
    private boolean mIconArrowRight;
    private boolean mIconCancel;
    private int mIconColor;
    private View mCustomView;
    private OnBindView onBindView;
    private View.OnClickListener mIconClickListener;

    public MiuiCardPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiCardPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.miuix_card);
        mCustomView = null;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiCardPreference,
            defStyleAttr, defStyleRes)) {
            mBackgroundColor = array.getColor(R.styleable.MiuiCardPreference_backgroundColor, context.getColor(R.color.card_background));
            mIconArrowRight = array.getBoolean(R.styleable.MiuiCardPreference_iconArrowRight, false);
            mIconCancel = array.getBoolean(R.styleable.MiuiCardPreference_iconCancel, false);
            mIconColor = array.getColor(R.styleable.MiuiCardPreference_iconColor, -1);
            int customViewId = array.getResourceId(R.styleable.MiuiCardPreference_customCardView, 0);
            if (customViewId != 0)
                mCustomView = LayoutInflater.from(getContext()).inflate(customViewId, mCustomLayout, false);
        }
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        notifyChanged();
    }

    public void setIconArrowRight(boolean arrowRight) {
        mIconArrowRight = arrowRight;
        notifyChanged();
    }

    public void setIconCancel(boolean cancel) {
        mIconCancel = cancel;
        notifyChanged();
    }

    public void setIconColor(int color) {
        mIconColor = color;
        notifyChanged();
    }

    public void setCustomViewId(@LayoutRes int viewId) {
        setCustomView(LayoutInflater.from(getContext()).inflate(viewId, mCustomLayout, false));
    }

    public void setCustomView(View v) {
        mCustomView = v;
        notifyChanged();
    }

    public void removeCustomView() {
        mCustomView = null;
        notifyChanged();
    }

    public void setCustomViewCallBack(OnBindView onBindView) {
        this.onBindView = onBindView;
        notifyChanged();
    }

    public void setIconClickListener(View.OnClickListener clickListener) {
        mIconClickListener = v -> {
            if (clickListener != null)
                clickListener.onClick(v);
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        };
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mBackgroundLayout = holder.itemView.findViewById(R.id.pref_card_bg);
        mCustomLayout = mBackgroundLayout.findViewById(R.id.card_custom_view);
        mImageView = mBackgroundLayout.findViewById(R.id.card_image);
        updateBackground(holder.itemView, -1);

        mImageView.setVisibility(View.GONE);
        mImageView.setOnClickListener(null);

        loadCustomLayout();
        Drawable drawable = mBackgroundLayout.getBackground();
        drawable.setTint(mBackgroundColor);
        mBackgroundLayout.setBackground(drawable);
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
        Drawable drawable = null;
        if (mIconArrowRight)
            drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_preference_arrow_right);
        else if (mIconCancel)
            drawable = AppCompatResources.getDrawable(getContext(), R.drawable.miuix_button_cancel);
        else if (getIcon() != null) drawable = getIcon();
        assert drawable != null;
        if (mIconColor != -1) drawable.setTint(mIconColor);
        mImageView.setImageDrawable(drawable);
        mImageView.setVisibility(View.VISIBLE);
        if (mImageView.getVisibility() == View.VISIBLE)
            mImageView.setOnClickListener(mIconClickListener);
    }

    public interface OnBindView {
        void onBindView(View view);
    }
}
