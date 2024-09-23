package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class MiuiTextView extends TextView {
    private boolean shouldFocusable;
    private boolean shouldSingeLineCenter;

    public MiuiTextView(Context context) {
        super(context);
        init(context, null);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MiuiTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MiuiTextView)) {
            shouldFocusable = typedArray.getBoolean(R.styleable.MiuiTextView_focusable, false);
            shouldSingeLineCenter = typedArray.getBoolean(R.styleable.MiuiTextView_singeLineCenter, false);
        }
    }

    @Override
    public boolean isFocused() {
        return shouldFocusable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLineCount() <= 1 && shouldSingeLineCenter) {
            setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}
