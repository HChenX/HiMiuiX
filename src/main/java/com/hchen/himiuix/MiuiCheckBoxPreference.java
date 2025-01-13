package com.hchen.himiuix;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MiuiCheckBoxPreference extends MiuiPreference {
    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MiuiCheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiuiCheckBoxPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
    }
}
