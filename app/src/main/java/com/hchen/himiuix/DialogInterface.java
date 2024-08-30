package com.hchen.himiuix;

import android.text.Editable;
import android.util.SparseBooleanArray;

import java.util.ArrayList;

public interface DialogInterface {
    void dismiss();

    interface OnClickListener {
        void onClick(DialogInterface dialog, int which);
    }

    interface OnItemsChangeListener {
        void onClick(DialogInterface dialogInterface, CharSequence item, int which);

        default void onResult(ArrayList<CharSequence> items, SparseBooleanArray booleanArray) {
        }
    }

    interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }

    interface TextWatcher {
        default void beforeTextChanged(CharSequence s, int start,
                                       int count, int after) {
        }

        default void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        default void afterTextChanged(Editable s) {
        }

        void onResult(CharSequence s);
    }
}
