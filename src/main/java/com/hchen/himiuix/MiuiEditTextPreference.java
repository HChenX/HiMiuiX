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
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiEditTextPreference, defStyleAttr, defStyleRes)) {
            mHint = TypedArrayUtils.getString(array, R.styleable.MiuiEditTextPreference_hint, R.styleable.MiuiEditTextPreference_android_hint);
            mInputType = array.getInt(R.styleable.MiuiEditTextPreference_android_inputType, -1);
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
        notifyChanged();
    }

    public Drawable getImage() {
        return mDrawable;
    }

    public void setInputType(int type) {
        this.mInputType = type;
    }

    public int getInputType() {
        return mInputType;
    }

    public void onImageClickListener(View.OnClickListener clickListener) {
        mImageClickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mLayout = holder.itemView.findViewById(R.id.edit_layout);
        mEditTextView = holder.itemView.findViewById(R.id.edit_text_id);
        mTipTextView = holder.itemView.findViewById(R.id.edit_tip);
        mImageView = holder.itemView.findViewById(R.id.edit_image);

        mTipTextView.setVisibility(View.GONE);
        if (getTitle() != null) {
            mTipTextView.setVisibility(View.VISIBLE);
            mTipTextView.setText(getTitle());
        }
        if (mHint != null) mEditTextView.setHint(mHint);
        else mEditTextView.setHint("请输入");

        mImageView.setVisibility(View.GONE);
        mImageView.setOnClickListener(null);
        if (getIcon() != null) {
            getIcon().setAlpha(isEnabled() ? 255 : 125);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageDrawable(getIcon());
        }

        mEditTextView.setEnabled(isEnabled());
        mEditTextView.setOnFocusChangeListener(null);
        if (mWatcher != null)
            mEditTextView.removeTextChangedListener(mWatcher);
        mEditTextView.clearFocus();
        if (isEnabled()) {
            mTipTextView.setTextColor(getContext().getColor(R.color.tittle));
            mEditTextView.setHintTextColor(getContext().getColor(R.color.summary));
            if (mImageView.getVisibility() != View.GONE)
                mImageView.setOnClickListener(mImageClickListener);
            if (mWatcher != null)
                mEditTextView.addTextChangedListener(mWatcher);

            if (mInputType != -1) mEditTextView.setInputType(mInputType);
            else mEditTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            mEditTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mLayout.setBackgroundResource(R.drawable.focused_border_input_box);
                    } else {
                        mLayout.setBackgroundResource(R.drawable.nofocused_border_input_box);
                        hideInputIfNeed();
                    }
                }
            });
        } else {
            mTipTextView.setTextColor(getContext().getColor(R.color.tittle_d));
            mEditTextView.setHintTextColor(getContext().getColor(R.color.summary_d));
        }
    }

    private boolean isInputVisible() {
        return mEditTextView.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
    }

    private void hideInputIfNeed() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible()) imm.hideSoftInputFromWindow(mEditTextView.getWindowToken(), 0);
    }
}
