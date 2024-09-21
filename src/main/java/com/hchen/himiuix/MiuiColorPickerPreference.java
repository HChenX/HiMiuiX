package com.hchen.himiuix;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.hchen.himiuix.colorpicker.ColorBaseSeekBar;
import com.hchen.himiuix.colorpicker.ColorPickerAlphaView;
import com.hchen.himiuix.colorpicker.ColorPickerData;
import com.hchen.himiuix.colorpicker.ColorPickerHueView;
import com.hchen.himiuix.colorpicker.ColorPickerLightnessView;
import com.hchen.himiuix.colorpicker.ColorPickerSaturationView;

public class MiuiColorPickerPreference extends MiuiPreference implements ColorBaseSeekBar.OnColorValueChanged {
    private MiuiAlertDialog alertDialog;
    private int mColor;
    private boolean isInitialTime = true;

    public MiuiColorPickerPreference(@NonNull Context context) {
        super(context);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuiColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiColorPickerPreference,
                defStyleAttr, defStyleRes)) {
            int defColor = array.getColor(R.styleable.MiuiColorPickerPreference_defaultColor, -1);
            setDefaultValue(defColor);
        }
    }

    public void setValue(int color) {
        if (mColor != color) {
            if (isInitialTime || callChangeListener(color)) {
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
        setValue(getPersistedInt((Integer) defaultValue));
        isInitialTime = false;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        getColorSelectView().setVisibility(View.VISIBLE);
        getColorSelectView().setGrayedOut(!isEnabled());
        getColorSelectView().setColor(mColor);
    }

    private ColorPickerHueView hueView;
    private ColorPickerSaturationView saturationView;
    private ColorPickerLightnessView lightnessView;
    private ColorPickerAlphaView alphaView;
    private EditText editTextView = null;
    private ColorPickerData colorPickerData;
    private View showColorView;

    @Override
    protected void onClick(View view) {
        if (alertDialog != null && alertDialog.isShowing()) return;

        colorPickerData = new ColorPickerData();
        alertDialog = new MiuiAlertDialog(getContext());
        alertDialog.setTitle(getTitle());
        alertDialog.setMessage(getSummary());
        alertDialog.autoDismiss(false);
        alertDialog.setHapticFeedbackEnabled(true);
        alertDialog.setCustomView(R.layout.miuix_color_picker, new MiuiAlertDialog.OnBindView() {
            private boolean isEditFocus = false;

            @Override
            public void onBindView(View view) {
                hueView = view.findViewById(R.id.color_hue_view);
                saturationView = view.findViewById(R.id.color_saturation_view);
                lightnessView = view.findViewById(R.id.color_lightness_view);
                alphaView = view.findViewById(R.id.color_alpha_view);
                TextView editTipView = view.findViewById(R.id.edit_tip);
                showColorView = view.findViewById(R.id.color_show_view);
                editTextView = view.findViewById(R.id.edit_text_id);

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

                editTextView.setOnFocusChangeListener((v, hasFocus) -> isEditFocus = hasFocus);
                editTextView.setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
                editTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                editTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!isEditFocus) return;
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
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTextView.getText().length() < 8) {
                    Toast.makeText(getContext(), "Color 值应是 8 位！", Toast.LENGTH_SHORT).show();
                } else {
                    setValue(colorPickerData.HSVToColor());
                    dialog.dismiss();
                }
            }
        });
        alertDialog.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
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
        hueView.updateColorPickerHueState((int) hsv[0]);
        saturationView.updateColorPickerSaturationState(Math.round(hsv[1] * 100));
        lightnessView.updateColorPickerLightnessState(Math.round(hsv[2] * 100));
        alphaView.updateColorPickerAlphaState(alpha);
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
