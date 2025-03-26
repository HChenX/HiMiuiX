/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.himiuix.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

public class MiuiEditText extends ConstraintLayout {
    private Context mContext;
    private TextView mEditTextTipView;
    private EditText mEditTextView;
    private ImageView mEditTextImageView;
    private LayoutParams params;
    private final int mEditHeight;
    private final Drawable mBackground;
    private final String mHint;
    private boolean isErrorBorder = false;

    public MiuiEditText(@NonNull Context context) {
        this(context, null);
    }

    public MiuiEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiEditText)) {
            mEditHeight = (int) typedArray.getDimension(R.styleable.MiuiEditText_editHeight, MiuiXUtils.dp2px(getContext(), 50));
            mHint = typedArray.getString(R.styleable.MiuiEditText_android_hint);
            if (typedArray.hasValue(R.styleable.MiuiEditText_background))
                mBackground = typedArray.getDrawable(R.styleable.MiuiEditText_background);
            else
                mBackground = AppCompatResources.getDrawable(context, R.drawable.ic_edit_bg);
        }
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        params = new LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(params);
        setBackground(mBackground);

        mEditTextTipView = new TextView(context);
        mEditTextView = new EditText(context);
        mEditTextImageView = new ImageView(context);
        loadEditTextTipView();
        loadEditTextView();
        loadEditTextImageView();
        makeMiuiEditTextLayout();
        updateEditTextBackground();
    }

    private void loadEditTextTipView() {
        mEditTextTipView.setId(R.id.edit_tip);
        params = new LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            mEditHeight
        );
        params.setMarginStart(MiuiXUtils.dp2px(mContext, 15));
        mEditTextTipView.setLayoutParams(params);
        mEditTextTipView.setGravity(Gravity.CENTER);
        mEditTextTipView.setSingleLine(true);
        mEditTextTipView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mEditTextTipView.setTextColor(mContext.getColor(R.color.edit_text));
        mEditTextTipView.setTextSize(17);
        mEditTextTipView.setVisibility(GONE);
        addView(mEditTextTipView);
    }

    private void loadEditTextView() {
        mEditTextView.setId(R.id.edit_text);
        params = new LayoutParams(
            0,
            mEditHeight
        );
        params.setMarginStart(MiuiXUtils.dp2px(mContext, 15));
        params.setMarginEnd(MiuiXUtils.dp2px(mContext, 15));
        mEditTextView.setLayoutParams(params);
        mEditTextView.setBackground(null);
        mEditTextView.setClickable(true);
        mEditTextView.setFocusable(true);
        mEditTextView.setFocusableInTouchMode(true);
        mEditTextView.setSingleLine(true);
        mEditTextView.setHint(mHint);
        mEditTextView.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        mEditTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditTextView.setHintTextColor(mContext.getColor(R.color.edit_hint));
        mEditTextView.setTextCursorDrawable(AppCompatResources.getDrawable(mContext, R.drawable.edit_cursor));
        addView(mEditTextView);
    }

    private void loadEditTextImageView() {
        mEditTextImageView.setId(R.id.edit_image);
        params = new LayoutParams(
            mEditHeight,
            mEditHeight
        );
        mEditTextImageView.setLayoutParams(params);
        mEditTextImageView.setPadding(
            0,
            MiuiXUtils.dp2px(mContext, 8),
            0,
            MiuiXUtils.dp2px(mContext, 8)
        );
        mEditTextImageView.setAdjustViewBounds(true);
        mEditTextImageView.setScaleType(ImageView.ScaleType.CENTER);
        mEditTextImageView.setVisibility(GONE);
        addView(mEditTextImageView);
    }

    private void makeMiuiEditTextLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        constraintSet.connect(mEditTextTipView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        constraintSet.connect(mEditTextTipView.getId(), ConstraintSet.RIGHT, mEditTextView.getId(), ConstraintSet.LEFT);
        constraintSet.connect(mEditTextTipView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mEditTextTipView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.connect(mEditTextView.getId(), ConstraintSet.LEFT, mEditTextTipView.getId(), ConstraintSet.RIGHT);
        constraintSet.connect(mEditTextView.getId(), ConstraintSet.RIGHT, mEditTextImageView.getId(), ConstraintSet.LEFT);
        constraintSet.connect(mEditTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mEditTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.connect(mEditTextImageView.getId(), ConstraintSet.LEFT, mEditTextView.getId(), ConstraintSet.RIGHT);
        constraintSet.connect(mEditTextImageView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        constraintSet.connect(mEditTextImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mEditTextImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.applyTo(this);
    }

    private void updateEditTextBackground() {
        mEditTextView.clearFocus();
        mEditTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateErrorBorderState();
                } else {
                    hideInputIfNeed();
                    MiuiEditText.this.setBackgroundResource(R.drawable.nofocused_border_input_box);
                }
            }
        });
    }

    public EditText getEditTextView() {
        return mEditTextView;
    }

    public void setErrorBorderState(boolean error) {
        if (isErrorBorder != error) {
            isErrorBorder = error;

            updateErrorBorderState();
        }
    }

    private void updateErrorBorderState() {
        if (isErrorBorder) setBackgroundResource(R.drawable.error_border_input_box);
        else setBackgroundResource(R.drawable.focused_border_input_box);
    }

    private void hideInputIfNeed() {
        if (isInputVisible()) {
            WindowInsetsController windowInsetsController = getWindowInsetsController();
            if (windowInsetsController != null)
                windowInsetsController.hide(WindowInsets.Type.ime());
            else {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    private boolean isInputVisible() {
        if (mEditTextView == null) return false;
        if (mEditTextView.getRootWindowInsets() == null) return false;
        return mEditTextView.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
    }
}
