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

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.adapter.TextWatcherAdapter;
import com.hchen.himiuix.colorpicker.ColorBaseSeekBar;
import com.hchen.himiuix.colorpicker.ColorPickerAlphaView;
import com.hchen.himiuix.colorpicker.ColorPickerData;
import com.hchen.himiuix.colorpicker.ColorPickerHueView;
import com.hchen.himiuix.colorpicker.ColorPickerLightnessView;
import com.hchen.himiuix.colorpicker.ColorPickerSaturationView;
import com.hchen.himiuix.widget.MiuiEditText;

public class MiuiColorPickerPreference extends MiuiPreference implements ColorBaseSeekBar.OnColorValueChanged {
    private MiuiAlertDialog mAlertDialog;
    private boolean isInitialTime = true;
    private int mColor;

    public MiuiColorPickerPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiColorPickerPreference,
            defStyleAttr, defStyleRes)) {
            int defColor = array.getColor(R.styleable.MiuiColorPickerPreference_defaultColor, -1);
            setDefaultValue(defColor);
        }
    }

    public void setValue(int color) {
        innerSetValue(color, false);
    }

    private void innerSetValue(int color, boolean formUser) {
        if (mColor != color) {
            if (isInitialTime || (!formUser || callChangeListener(color))) {
                mColor = color;
                persistInt(color);
                if (!isInitialTime) {
                    notifyChanged();
                }
            }
        }
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue == null) defaultValue = -1;
        innerSetValue(getPersistedInt((Integer) defaultValue), false);
        isInitialTime = false;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        getColorSelectView().setGrayedOut(!isEnabled());
        getColorSelectView().setColor(mColor);
        isInitialTime = false;
    }

    private NestedScrollView mNestedScrollView;
    private ColorPickerHueView mHueView;
    private ColorPickerSaturationView mSaturationView;
    private ColorPickerLightnessView mLightnessView;
    private ColorPickerAlphaView mAlphaView;
    private MiuiEditText mMiuiEditText;
    private EditText mEditTextView = null;
    private ColorPickerData mColorPickerData;
    private View mShowColorView;

    @Override
    protected void onClick(View view) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) return;

        mColorPickerData = new ColorPickerData();
        mAlertDialog = new MiuiAlertDialog(getContext());
        mAlertDialog.setTitle(getTitle());
        mAlertDialog.setMessage(getSummary());
        mAlertDialog.setHapticFeedbackEnabled(true);
        mAlertDialog.setEnableCustomView(true);
        mAlertDialog.setCustomView(R.layout.miuix_color_picker, new DialogInterface.OnBindView() {

            @Override
            public void onBindView(ViewGroup root, View view) {
                mNestedScrollView = view.findViewById(R.id.color_scroll_view);
                mHueView = view.findViewById(R.id.color_hue_view);
                mSaturationView = view.findViewById(R.id.color_saturation_view);
                mLightnessView = view.findViewById(R.id.color_lightness_view);
                mAlphaView = view.findViewById(R.id.color_alpha_view);
                mMiuiEditText = view.findViewById(R.id.edit_layout);
                TextView editTipView = view.findViewById(R.id.edit_tip);
                mShowColorView = view.findViewById(R.id.color_show_view);
                mEditTextView = view.findViewById(R.id.edit_text);

                mHueView.setColorPickerData(mColorPickerData);
                mSaturationView.setColorPickerData(mColorPickerData);
                mLightnessView.setColorPickerData(mColorPickerData);
                mAlphaView.setColorPickerData(mColorPickerData);

                mHueView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                mSaturationView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                mLightnessView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                mAlphaView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);

                mHueView.registerHueChangeListener(new ColorPickerHueView.OnColorHueChanged[]{
                    mSaturationView, mLightnessView, mAlphaView
                });

                colorToHsv(mColor, Color.alpha(mColor));
                setShowColorView(mColor);
                setEditText(argbTo16(mColor));

                view.setOnClickListener(v -> mEditTextView.clearFocus());
                editTipView.setVisibility(View.VISIBLE);
                editTipView.setText("#");
                mEditTextView.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
                mEditTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                mEditTextView.addTextChangedListener(new TextWatcherAdapter() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!mEditTextView.hasFocus()) return;
                        if (s.length() == 8) {
                            mMiuiEditText.updateErrorBorderState(false);
                            long alpha = Long.parseLong(s.subSequence(0, 2).toString(), 16);
                            int argb = Color.parseColor("#" + s);
                            colorToHsv(argb, (int) alpha);
                            setShowColorView(argb);
                        } else {
                            mMiuiEditText.updateErrorBorderState(true);
                        }
                    }
                });
            }
        });
        final int[] dialogHeight = {0};
        int btBottom = ViewCompat.getRootWindowInsets(((Activity) getContext()).getWindow().getDecorView()).getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
        mAlertDialog.getWindow().getDecorView().post(() -> dialogHeight[0] = mAlertDialog.getWindow().getDecorView().getHeight());
        ViewCompat.setOnApplyWindowInsetsListener(((Activity) getContext()).getWindow().getDecorView(), new OnApplyWindowInsetsListener() {
            private int oldHeight = -1;

            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                int bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                Insets systemBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                int systemBarHeight = systemBar.top + (btBottom <= 0 ? MiuiXUtils.dp2px(getContext(), 5) /* 无小白条时轻微顶起 */ : btBottom);
                if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                    if (bottom == 0) return insets;
                    int screenY = MiuiXUtils.getScreenSize(getContext()).y;
                    int allHeight = bottom + dialogHeight[0] + systemBarHeight;
                    if (allHeight >= screenY) {
                        int surplus = allHeight - screenY;
                        if (oldHeight == -1) {
                            oldHeight = mNestedScrollView.getHeight();
                        }

                        int targetHeight = oldHeight - Math.max(surplus, 0);
                        animateHeightChange(mNestedScrollView, targetHeight, false);
                    }
                } else if (oldHeight != -1) {
                    animateHeightChange(mNestedScrollView, oldHeight, true);
                    oldHeight = -1;
                }
                return insets;
            }
        });
        mAlertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mEditTextView.getText().length() < 8) {
                    Toast.makeText(getContext(), "Color 值应是 8 位！", Toast.LENGTH_SHORT).show();

                } else {
                    innerSetValue(mColorPickerData.HSVToColor(), true);
                    dialog.dismiss();
                }
            }
        });
        mAlertDialog.setAutoDismiss(false);
        mAlertDialog.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        mAlertDialog.show();
    }

    private void animateHeightChange(View view, int endHeight, boolean restore) {
        if (restore) {
            if (mMiuiEditText != null)
                mMiuiEditText.clearEditTextFocus();
        }

        TransitionManager.beginDelayedTransition((ViewGroup) view, new AutoTransition());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = endHeight;
        view.setLayoutParams(params);
    }

    @Override
    public void changed(ColorBaseSeekBar.ColorPickerTag tag, int value) {
        switch (tag) {
            case TAG_DEF:
                return;
            case TAG_HUE:
                mColorPickerData.hue = value;
                break;
            case TAG_SATURATION:
                mColorPickerData.saturation = value;
                break;
            case TAG_LIGHTNESS:
                mColorPickerData.lightness = value;
                break;
            case TAG_ALPHA:
                mColorPickerData.alpha = value;
                break;
        }
        if (mEditTextView != null) mEditTextView.clearFocus();
        setShowColorView(mColorPickerData.HSVToColor());
        setEditText(argbTo16(mColorPickerData.HSVToColor()));
    }

    private void setShowColorView(int argb) {
        if (mShowColorView == null) return;
        Drawable drawable = mShowColorView.getBackground();
        drawable.setTint(argb);
        mShowColorView.setBackground(drawable);
    }

    private void setEditText(String text) {
        if (mEditTextView == null) return;
        mEditTextView.setText(text);
    }

    private String argbTo16(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format("%02X%02X%02X%02X", alpha, red, green, blue);
    }

    private void colorToHsv(int rgb, int alpha) {
        if (mHueView == null || mSaturationView == null
            || mLightnessView == null || mAlphaView == null) return;

        float[] hsv = new float[3];
        Color.colorToHSV(rgb, hsv);
        mHueView.updateColorPickerHueState(Math.round(hsv[0] * 100));
        mSaturationView.updateColorPickerSaturationState(Math.round(hsv[1] * 10000));
        mLightnessView.updateColorPickerLightnessState(Math.round(hsv[2] * 10000));
        mAlphaView.updateColorPickerAlphaState(alpha);
    }

    @Override
    boolean shouldShowColorSelectView() {
        return true;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent()) {
            return parcelable;
        }

        final SavedState savedState = new SavedState(parcelable);
        savedState.mColor = mColor;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mColor = savedState.mColor;
        notifyChanged();
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
            new Parcelable.Creator<SavedState>() {
                @Override
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                @Override
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };

        int mColor;

        public SavedState(Parcel source) {
            super(source);

            mColor = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(mColor);
        }
    }
}
