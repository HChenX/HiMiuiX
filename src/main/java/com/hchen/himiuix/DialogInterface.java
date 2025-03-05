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

import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public interface DialogInterface {
    int BUTTON_POSITIVE = -1;
    int BUTTON_NEGATIVE = -2;
    int BUTTON_NEUTRAL = -3;

    void cancel();

    void dismiss();

    interface OnClickListener {
        void onClick(DialogInterface dialog, int which);
    }

    interface OnItemsClickListener {
        default void onClick(DialogInterface dialog, CharSequence item, int which) {
        }

        default void onResult(DialogInterface dialog, ArrayList<CharSequence> items, ArrayList<CharSequence> selectedItems) {
        }
    }

    interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }

    interface OnShowListener {
        void onShow(DialogInterface dialog);
    }

    interface OnCancelListener {
        void onCancel(DialogInterface dialog);
    }

    interface TextWatcher extends android.text.TextWatcher {
        default void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        default void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        default void afterTextChanged(Editable s) {
        }

        void onResult(DialogInterface dialog, CharSequence s);
    }

    interface OnBindView {
        void onBindView(ViewGroup root, View view);
    }
}
