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

public class MiuiXEditText extends ConstraintLayout {
    private Context context;
    private TextView tipView;
    private EditText editTextView;
    private ImageView iconView;
    private LayoutParams params;
    private final int editLayoutHeight;
    private final Drawable editBackground;
    private final String hint;
    private boolean isErrorBorder = false;

    public MiuiXEditText(@NonNull Context context) {
        this(context, null);
    }

    public MiuiXEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiuiXEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiXEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiEditText)) {
            editLayoutHeight = (int) typedArray.getDimension(R.styleable.MiuiEditText_editHeight, MiuiXUtils.dp2px(getContext(), 50));
            hint = typedArray.getString(R.styleable.MiuiEditText_android_hint);
            if (typedArray.hasValue(R.styleable.MiuiEditText_background))
                editBackground = typedArray.getDrawable(R.styleable.MiuiEditText_background);
            else
                editBackground = AppCompatResources.getDrawable(context, R.drawable.ic_edit_bg);
        }
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        params = new LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(params);
        setBackground(editBackground);

        tipView = new TextView(context);
        editTextView = new EditText(context);
        iconView = new ImageView(context);
        loadEditTextTipView();
        loadEditTextView();
        loadEditTextImageView();
        applyLayout();
        updateEditTextBackground();
    }

    private void loadEditTextTipView() {
        tipView.setId(R.id.edit_tip);
        params = new LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            editLayoutHeight
        );
        params.setMarginStart(MiuiXUtils.dp2px(context, 15));
        tipView.setLayoutParams(params);
        tipView.setGravity(Gravity.CENTER);
        tipView.setSingleLine(true);
        tipView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tipView.setTextColor(context.getColor(R.color.edit_text));
        tipView.setTextSize(17);
        tipView.setVisibility(GONE);
        addView(tipView);
    }

    private void loadEditTextView() {
        editTextView.setId(R.id.edit_text);
        params = new LayoutParams(
            0,
            editLayoutHeight
        );
        params.setMarginStart(MiuiXUtils.dp2px(context, 15));
        params.setMarginEnd(MiuiXUtils.dp2px(context, 15));
        editTextView.setLayoutParams(params);
        editTextView.setBackground(null);
        editTextView.setClickable(true);
        editTextView.setFocusable(true);
        editTextView.setFocusableInTouchMode(true);
        editTextView.setSingleLine(true);
        editTextView.setHint(hint);
        editTextView.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        editTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editTextView.setHintTextColor(context.getColor(R.color.edit_hint));
        editTextView.setTextCursorDrawable(AppCompatResources.getDrawable(context, R.drawable.edit_cursor));
        addView(editTextView);
    }

    private void loadEditTextImageView() {
        iconView.setId(R.id.edit_image);
        params = new LayoutParams(
            editLayoutHeight,
            editLayoutHeight
        );
        iconView.setLayoutParams(params);
        iconView.setPadding(
            0,
            MiuiXUtils.dp2px(context, 8),
            0,
            MiuiXUtils.dp2px(context, 8)
        );
        iconView.setAdjustViewBounds(true);
        iconView.setScaleType(ImageView.ScaleType.CENTER);
        iconView.setVisibility(GONE);
        addView(iconView);
    }

    private void applyLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        constraintSet.connect(tipView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        constraintSet.connect(tipView.getId(), ConstraintSet.RIGHT, editTextView.getId(), ConstraintSet.LEFT);
        constraintSet.connect(tipView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(tipView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.connect(editTextView.getId(), ConstraintSet.LEFT, tipView.getId(), ConstraintSet.RIGHT);
        constraintSet.connect(editTextView.getId(), ConstraintSet.RIGHT, iconView.getId(), ConstraintSet.LEFT);
        constraintSet.connect(editTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(editTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.connect(iconView.getId(), ConstraintSet.LEFT, editTextView.getId(), ConstraintSet.RIGHT);
        constraintSet.connect(iconView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        constraintSet.connect(iconView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(iconView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.applyTo(this);
    }

    private void updateEditTextBackground() {
        editTextView.clearFocus();
        editTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateErrorBorderState();
                } else {
                    hideInputIfNeed();
                    MiuiXEditText.this.setBackgroundResource(R.drawable.nofocused_border_input_box);
                }
            }
        });
    }

    public EditText getEditTextView() {
        return editTextView;
    }

    public TextView getTipView() {
        return tipView;
    }

    public ImageView getIconView() {
        return iconView;
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
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    private boolean isInputVisible() {
        if (editTextView == null) return false;
        if (editTextView.getRootWindowInsets() == null) return false;
        return editTextView.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
    }
}
