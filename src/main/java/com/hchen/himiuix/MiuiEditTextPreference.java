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
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

public class MiuiEditTextPreference extends MiuiPreference {
    private ConstraintLayout mLayout;
    private EditText mEditTextView;
    private TextView mTipTextView;
    private ImageView mImageView;
    private CharSequence mHint;
    private Drawable mDrawable;
    private TextWatcher mWatcher;
    private int mInputType = -1;
    private View.OnClickListener mImageClickListener;

    public MiuiEditTextPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.miuix_preference_edit);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiEditTextPreference, defStyleAttr, defStyleRes)) {
            mHint = TypedArrayUtils.getString(array, R.styleable.MiuiEditTextPreference_hint, R.styleable.MiuiEditTextPreference_android_hint);
            mInputType = array.getInt(R.styleable.MiuiEditTextPreference_android_inputType, -1);
        }
    }

    public void setHint(@StringRes int hintRes) {
        setHint(getContext().getText(hintRes));
    }

    public void setHint(CharSequence hint) {
        this.mHint = hint;
        notifyChanged();
    }

    public CharSequence getHint() {
        return mHint;
    }

    public void setTextWatcher(TextWatcher watcher) {
        this.mWatcher = watcher;
        notifyChanged();
    }

    public void setImage(@DrawableRes int drawableRes) {
        setImage(AppCompatResources.getDrawable(getContext(), drawableRes));
    }

    public void setImage(Drawable drawable) {
        this.mDrawable = drawable;
        setIcon(drawable);
    }

    public Drawable getImage() {
        return mDrawable;
    }

    public void setInputType(int type) {
        this.mInputType = type;
        notifyChanged();
    }

    public int getInputType() {
        return mInputType;
    }

    public void setImageClickListener(View.OnClickListener clickListener) {
        mImageClickListener = clickListener;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mLayout = holder.itemView.findViewById(R.id.edit_layout);
        mEditTextView = holder.itemView.findViewById(R.id.edit_text);
        mTipTextView = holder.itemView.findViewById(R.id.edit_tip);
        mImageView = holder.itemView.findViewById(R.id.edit_image);
        updateBackground(holder.itemView, -1);

        mTipTextView.setVisibility(View.GONE);
        if (getTitle() != null) {
            mTipTextView.setVisibility(View.VISIBLE);
            mTipTextView.setText(getTitle());
        }
        if (mHint != null) mEditTextView.setHint(mHint);
        else mEditTextView.setHint("");

        mImageView.setVisibility(View.GONE);
        mImageView.setOnClickListener(null);
        if (getIcon() != null) {
            getIcon().setAlpha(isEnabled() ? 255 : 125);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageDrawable(getIcon());
        }

        mEditTextView.setEnabled(isEnabled());
        if (mWatcher != null)
            mEditTextView.removeTextChangedListener(mWatcher);
        if (isEnabled()) {
            mTipTextView.setAlpha(1f);
            mEditTextView.setAlpha(1f);
            if (mImageView.getVisibility() != View.GONE)
                mImageView.setOnClickListener(mImageClickListener);
            if (mWatcher != null)
                mEditTextView.addTextChangedListener(mWatcher);
            if (mInputType != -1) mEditTextView.setInputType(mInputType);
            else mEditTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            mTipTextView.setAlpha(0.5f);
            mEditTextView.setAlpha(0.5f);
        }
    }
}
