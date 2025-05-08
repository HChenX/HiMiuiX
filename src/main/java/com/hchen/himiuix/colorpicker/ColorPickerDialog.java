package com.hchen.himiuix.colorpicker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;
import com.hchen.himiuix.adapter.TextWatcherAdapter;
import com.hchen.himiuix.widget.MiuiXEditText;

public class ColorPickerDialog implements ColorBaseSeekBar.OnColorValueChangedListener {
    private final Context context;
    private MiuiAlertDialog colorPickerDialog;
    private ColorPickerData colorPickerData;
    private ColorPickerHueView hueView;
    private ColorPickerSaturationView saturationView;
    private ColorPickerLightnessView lightnessView;
    private ColorPickerAlphaView alphaView;
    private MiuiXEditText miuiXEditText;
    private NestedScrollView nestedScrollView;
    private View colorShowView;
    private OnColorValueResultListener onColorValueResultListener;
    private int colorValue = Color.WHITE;

    public ColorPickerDialog(Context context) {
        this.context = context;
    }

    public MiuiAlertDialog createColorPickerDialog() {
        if (colorPickerDialog != null && colorPickerDialog.isShowing())
            return colorPickerDialog;

        colorPickerData = new ColorPickerData();
        colorPickerDialog = new MiuiAlertDialog(context)
            .setHapticFeedbackEnabled(true)
            .setEnableCustomView(true)
            .setCustomView(R.layout.miuix_color_picker, new DialogInterface.OnBindView() {
                @Override
                public void onBindView(ViewGroup root, View view) {
                    hueView = view.findViewById(R.id.color_hue_view);
                    saturationView = view.findViewById(R.id.color_saturation_view);
                    lightnessView = view.findViewById(R.id.color_lightness_view);
                    alphaView = view.findViewById(R.id.color_alpha_view);
                    miuiXEditText = view.findViewById(R.id.edit_layout);
                    nestedScrollView = view.findViewById(R.id.color_scroll_view);
                    colorShowView = view.findViewById(R.id.color_show_view);

                    hueView.setColorPickerData(colorPickerData);
                    saturationView.setColorPickerData(colorPickerData);
                    lightnessView.setColorPickerData(colorPickerData);
                    alphaView.setColorPickerData(colorPickerData);

                    hueView.setColorPickerValueChangedListener(ColorPickerDialog.this);
                    saturationView.setColorPickerValueChangedListener(ColorPickerDialog.this);
                    lightnessView.setColorPickerValueChangedListener(ColorPickerDialog.this);
                    alphaView.setColorPickerValueChangedListener(ColorPickerDialog.this);

                    hueView.registerHueChangeListener(saturationView, lightnessView, alphaView);

                    colorToHsv(colorValue, Color.alpha(colorValue));
                    updateColorShowView(colorValue);
                    setEditText(argbTo16(colorValue));

                    view.setOnClickListener(v -> miuiXEditText.getEditTextView().clearFocus());
                    miuiXEditText.getTipView().setVisibility(View.VISIBLE);
                    miuiXEditText.getTipView().setText("#");
                    miuiXEditText.getEditTextView().setKeyListener(DigitsKeyListener.getInstance("0123456789abcdefABCDEF"));
                    miuiXEditText.getEditTextView().setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                    miuiXEditText.getEditTextView().addTextChangedListener(new TextWatcherAdapter() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!miuiXEditText.getEditTextView().hasFocus()) return;

                            if (s.length() == 8) {
                                miuiXEditText.setErrorBorderState(false);
                                long alpha = Long.parseLong(s.subSequence(0, 2).toString(), 16);
                                int argb = Color.parseColor("#" + s);
                                colorToHsv(argb, (int) alpha);
                                updateColorShowView(argb);
                            } else {
                                miuiXEditText.setErrorBorderState(true);
                            }
                        }
                    });
                }
            })
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (miuiXEditText.getEditTextView().getText().length() < 8) {
                        Toast.makeText(context, "Color 值应是 8 位！", Toast.LENGTH_SHORT).show();
                    } else {
                        if (onColorValueResultListener != null)
                            onColorValueResultListener.onColorValue(colorPickerData.HSVToColor());
                        dialog.dismiss();
                    }
                }
            })
            .setNegativeButton("取消", null);

        final int[] dialogHeight = {0};
        int btBottom = ViewCompat.getRootWindowInsets(((Activity) context).getWindow().getDecorView()).getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
        colorPickerDialog.getWindow().getDecorView().post(() -> dialogHeight[0] = colorPickerDialog.getWindow().getDecorView().getHeight());
        ViewCompat.setOnApplyWindowInsetsListener(((Activity) context).getWindow().getDecorView(), new OnApplyWindowInsetsListener() {
            private int oldHeight = -1;

            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                int bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                Insets systemBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                int systemBarHeight = systemBar.top + (btBottom <= 0 ? MiuiXUtils.dp2px(context, 5) /* 无小白条时轻微顶起 */ : btBottom);
                if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                    if (bottom == 0) return insets;
                    int screenY = MiuiXUtils.getScreenSize(context).y;
                    int allHeight = bottom + dialogHeight[0] + systemBarHeight;
                    if (allHeight >= screenY) {
                        int surplus = allHeight - screenY;
                        if (oldHeight == -1) {
                            oldHeight = nestedScrollView.getHeight();
                        }

                        int targetHeight = oldHeight - Math.max(surplus, 0);
                        animateHeightChange(nestedScrollView, targetHeight, false);
                    }
                } else if (oldHeight != -1) {
                    animateHeightChange(nestedScrollView, oldHeight, true);
                    oldHeight = -1;
                }
                return insets;
            }
        });

        return colorPickerDialog;
    }

    public ColorPickerDialog setOnColorValueResultListener(OnColorValueResultListener onColorValueResultListener) {
        this.onColorValueResultListener = onColorValueResultListener;
        return this;
    }

    public ColorPickerDialog setColorValue(@ColorInt int colorValue) {
        this.colorValue = colorValue;
        return this;
    }

    private void animateHeightChange(View view, int endHeight, boolean restore) {
        if (restore) {
            if (miuiXEditText != null)
                miuiXEditText.getEditTextView().clearFocus();
        }

        TransitionManager.beginDelayedTransition((ViewGroup) view, new AutoTransition());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = endHeight;
        view.setLayoutParams(params);
    }

    @Override
    public void onColorValueChanged(ColorBaseSeekBar.ColorPickerTag tag, int value) {
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
        if (miuiXEditText != null) miuiXEditText.clearFocus();
        updateColorShowView(colorPickerData.HSVToColor());
        setEditText(argbTo16(colorPickerData.HSVToColor()));
    }

    private void updateColorShowView(int argb) {
        if (colorShowView == null) return;
        Drawable drawable = colorShowView.getBackground();
        drawable.setTint(argb);
        colorShowView.setBackground(drawable);
    }

    private void setEditText(String text) {
        if (miuiXEditText == null) return;
        miuiXEditText.getEditTextView().setText(text);
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
        hueView.updateColorPickerHueValue(Math.round(hsv[0] * 100));
        saturationView.updateColorPickerSaturationValue(Math.round(hsv[1] * 10000));
        lightnessView.updateColorPickerLightnessValue(Math.round(hsv[2] * 10000));
        alphaView.updateColorPickerAlphaValue(alpha);
    }

    public interface OnColorValueResultListener {
        void onColorValue(@ColorInt int color);
    }
}
