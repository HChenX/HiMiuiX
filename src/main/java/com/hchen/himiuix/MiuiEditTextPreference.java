package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
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
    private ConstraintLayout layout;
    private EditText editTextView;
    private TextView tipTextView;
    private ImageView imageView;
    private CharSequence hint;
    private Drawable drawable;
    private TextWatcher watcher;
    private int type = -1;
    private View.OnClickListener imageClickListener;

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
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiEditTextPreference, defStyleAttr, defStyleRes)) {
            hint = TypedArrayUtils.getString(array, R.styleable.MiuiEditTextPreference_hint, R.styleable.MiuiEditTextPreference_android_hint);
            type = array.getInt(R.styleable.MiuiEditTextPreference_android_inputType, -1);
        }
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setLayoutResource(R.layout.miuix_edit);
    }

    public void setHint(@StringRes int hintRes) {
        setHint(getContext().getText(hintRes));
    }

    public void setHint(CharSequence hint) {
        this.hint = hint;
        notifyChanged();
    }

    public CharSequence getHint() {
        return hint;
    }

    public void setTextWatcher(TextWatcher watcher) {
        this.watcher = watcher;
        notifyChanged();
    }

    public void setImage(@DrawableRes int drawableRes) {
        setImage(AppCompatResources.getDrawable(getContext(), drawableRes));
    }

    public void setImage(Drawable drawable) {
        this.drawable = drawable;
        setIcon(drawable);
        notifyChanged();
    }

    public Drawable getImage() {
        return drawable;
    }

    public void setInputType(int type) {
        this.type = type;
    }

    public int getInputType() {
        return type;
    }

    public void onImageClickListener(View.OnClickListener clickListener) {
        imageClickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        layout = holder.itemView.findViewById(R.id.edit_layout);
        editTextView = holder.itemView.findViewById(R.id.edit_text_id);
        tipTextView = holder.itemView.findViewById(R.id.edit_tip);
        imageView = holder.itemView.findViewById(R.id.edit_image);

        tipTextView.setVisibility(View.GONE);
        if (getTitle() != null) {
            tipTextView.setVisibility(View.VISIBLE);
            tipTextView.setText(getTitle());
        }
        if (hint != null) editTextView.setHint(hint);
        else editTextView.setHint("请输入");

        imageView.setVisibility(View.GONE);
        imageView.setOnClickListener(null);
        if (getIcon() != null) {
            getIcon().setAlpha(isEnabled() ? 255 : 125);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(getIcon());
        }

        editTextView.setEnabled(isEnabled());
        editTextView.setOnFocusChangeListener(null);
        if (watcher != null) 
            editTextView.removeTextChangedListener(watcher);
        editTextView.clearFocus();
        if (isEnabled()) {
            tipTextView.setTextColor(getContext().getColor(R.color.tittle));
            editTextView.setHintTextColor(getContext().getColor(R.color.summary));
            if (imageView.getVisibility() != View.GONE)
                imageView.setOnClickListener(imageClickListener);
            if (watcher != null)
                editTextView.addTextChangedListener(watcher);

            if (type != -1) editTextView.setInputType(type);
            else editTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            editTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        layout.setBackgroundResource(R.drawable.focused_border_input_box);
                    } else {
                        layout.setBackgroundResource(R.drawable.nofocused_border_input_box);
                        hideInputIfNeed();
                    }
                }
            });
        } else {
            tipTextView.setTextColor(getContext().getColor(R.color.tittle_d));
            editTextView.setHintTextColor(getContext().getColor(R.color.summary_d));
        }
    }

    private boolean isInputVisible() {
        return editTextView.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
    }

    private void hideInputIfNeed() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible()) imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
    }
}
