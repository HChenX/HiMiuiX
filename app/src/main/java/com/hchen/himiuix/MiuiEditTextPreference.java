package com.hchen.himiuix;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class MiuiEditTextPreference extends Preference {
    private static final String TAG = "MiuiEditTextPreference";

    public MiuiEditTextPreference(@NonNull Context context) {
        super(context);
        setLayoutResource(R.layout.miuix_edit);
    }

    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.miuix_edit);
    }

    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.miuix_edit);
    }

    public MiuiEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.miuix_edit);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        EditText editText = holder.itemView.findViewById(R.id.edit_text_id);
        ConstraintLayout layout = holder.itemView.findViewById(R.id.edit_layout);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    layout.setBackgroundResource(R.drawable.focused_border_input_box);
                } else
                    layout.setBackgroundResource(R.drawable.nofocused_border_input_box);
            }
        });
    }
}
