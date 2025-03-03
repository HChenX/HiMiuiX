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

import android.animation.ValueAnimator;
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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
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

public class MiuiColorPickerPreference extends MiuiPreference implements ColorBaseSeekBar.OnColorValueChanged {
    private MiuiAlertDialog mAlertDialog;
    private int mColor;
    private boolean isInitialTime = true;

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

    private NestedScrollView nestedScrollView;
    private ColorPickerHueView hueView;
    private ColorPickerSaturationView saturationView;
    private ColorPickerLightnessView lightnessView;
    private ColorPickerAlphaView alphaView;
    private EditText editTextView = null;
    private ColorPickerData colorPickerData;
    private View showColorView;

    @Override
    protected void onClick(View view) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) return;

        colorPickerData = new ColorPickerData();
        mAlertDialog = new MiuiAlertDialog(getContext());
        mAlertDialog.setTitle(getTitle());
        mAlertDialog.setMessage(getSummary());
        mAlertDialog.setHapticFeedbackEnabled(true);
        mAlertDialog.setEnableCustomView(true);
        mAlertDialog.setCustomView(R.layout.miuix_color_picker, new DialogInterface.OnBindView() {

            @Override
            public void onBindView(View view) {
                nestedScrollView = view.findViewById(R.id.color_scroll_view);
                hueView = view.findViewById(R.id.color_hue_view);
                saturationView = view.findViewById(R.id.color_saturation_view);
                lightnessView = view.findViewById(R.id.color_lightness_view);
                alphaView = view.findViewById(R.id.color_alpha_view);
                TextView editTipView = view.findViewById(R.id.edit_tip);
                showColorView = view.findViewById(R.id.color_show_view);
                editTextView = view.findViewById(R.id.edit_text);

                hueView.setColorPickerData(colorPickerData);
                saturationView.setColorPickerData(colorPickerData);
                lightnessView.setColorPickerData(colorPickerData);
                alphaView.setColorPickerData(colorPickerData);

                hueView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                saturationView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                lightnessView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);
                alphaView.setColorPickerValueChangedListener(MiuiColorPickerPreference.this);

                hueView.registerHueChangeListener(new ColorPickerHueView.OnColorHueChanged[]{
                    saturationView, lightnessView, alphaView
                });

                colorToHsv(mColor, Color.alpha(mColor));
                setShowColorView(mColor);
                setEditText(argbTo16(mColor));

                view.setOnClickListener(v -> editTextView.clearFocus());
                editTipView.setVisibility(View.VISIBLE);
                editTipView.setText("#");
                editTextView.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
                editTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                editTextView.addTextChangedListener(new TextWatcherAdapter() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!editTextView.hasFocus()) return;
                        if (s.length() == 8) {
                            long alpha = Long.parseLong(s.subSequence(0, 2).toString(), 16);
                            int argb = Color.parseColor("#" + s);
                            colorToHsv(argb, (int) alpha);
                            setShowColorView(argb);
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
                            oldHeight = nestedScrollView.getHeight();
                        }

                        int targetHeight = oldHeight - Math.max(surplus, 0);
                        animateHeightChange(nestedScrollView, nestedScrollView.getHeight(), targetHeight);
                    }
                } else if (oldHeight != -1) {
                    animateHeightChange(nestedScrollView, nestedScrollView.getHeight(), oldHeight);
                    oldHeight = -1;
                }
                return insets;
            }
        });
        mAlertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTextView.getText().length() < 8) {
                    Toast.makeText(getContext(), "Color 值应是 8 位！", Toast.LENGTH_SHORT).show();
                } else {
                    innerSetValue(colorPickerData.HSVToColor(), true);
                    dialog.dismiss();
                }
            }
        });
        mAlertDialog.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        mAlertDialog.show();
    }

    private void animateHeightChange(View view, int startHeight, int endHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        animator.start();
    }

    @Override
    public void changed(ColorBaseSeekBar.ColorPickerTag tag, int value) {
        switch (tag) {
            case TAG_DEF:
                return;
            case TAG_HUE:
                colorPickerData.hue = value;
                break;
            case TAG_SATURATION:
                colorPickerData.saturation = value;
                break;
            case TAG_LIGHTNESS:
                colorPickerData.lightness = value;
                break;
            case TAG_ALPHA:
                colorPickerData.alpha = value;
                break;
        }
        if (editTextView != null) editTextView.clearFocus();
        setShowColorView(colorPickerData.HSVToColor());
        setEditText(argbTo16(colorPickerData.HSVToColor()));
    }

    private void setShowColorView(int argb) {
        if (showColorView == null) return;
        Drawable drawable = showColorView.getBackground();
        drawable.setTint(argb);
        showColorView.setBackground(drawable);
    }

    private void setEditText(String text) {
        if (editTextView == null) return;
        editTextView.setText(text);
    }

    private String argbTo16(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format("%02X%02X%02X%02X", alpha, red, green, blue);
    }

    private void colorToHsv(int rgb, int alpha) {
        if (hueView == null || saturationView == null
            || lightnessView == null || alphaView == null) return;

        float[] hsv = new float[3];
        Color.colorToHSV(rgb, hsv);
        hueView.updateColorPickerHueState(Math.round(hsv[0] * 100));
        saturationView.updateColorPickerSaturationState(Math.round(hsv[1] * 10000));
        lightnessView.updateColorPickerLightnessState(Math.round(hsv[2] * 10000));
        alphaView.updateColorPickerAlphaState(alpha);
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
