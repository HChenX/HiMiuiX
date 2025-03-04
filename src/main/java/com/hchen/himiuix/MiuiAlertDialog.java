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

import static com.hchen.himiuix.MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory.BUTTON_NEGATIVE;
import static com.hchen.himiuix.MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory.BUTTON_NEUTRAL;
import static com.hchen.himiuix.MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory.BUTTON_POSITIVE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiAlertDialog {
    private final MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory mBaseFactory;

    public MiuiAlertDialog(@NonNull Context context) {
        this(context, R.style.MiuiAlertDialog);
    }

    public MiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
        this(context, themeResId, false);
    }

    protected MiuiAlertDialog(@NonNull Context context, boolean enableDropDownMode) {
        this(context, R.style.MiuiAlertDialog, enableDropDownMode);
    }

    protected MiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId, boolean enableDropDownMode) {
        mBaseFactory = new MiuiAlertDialogFactory(context, themeResId, enableDropDownMode).init();
    }

    public Window getWindow() {
        return mBaseFactory.mWindow;
    }

    protected MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory getBaseFactory() {
        return mBaseFactory;
    }

    public MiuiAlertDialog setTitle(@StringRes int titleId) {
        return setTitle(mBaseFactory.mContext.getText(titleId));
    }

    public MiuiAlertDialog setTitle(CharSequence title) {
        mBaseFactory.mTitle = title;
        return this;
    }

    public MiuiAlertDialog setMessage(@StringRes int messageId) {
        return setMessage(mBaseFactory.mContext.getText(messageId));
    }

    public MiuiAlertDialog setMessage(CharSequence message) {
        mBaseFactory.mMessage = message;
        return this;
    }

    public MiuiAlertDialog setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setPositiveButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public MiuiAlertDialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUsePositiveButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_POSITIVE, new Pair<>(text, listener));
        return this;
    }

    public MiuiAlertDialog setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNegativeButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public MiuiAlertDialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUseNegativeButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_NEGATIVE, new Pair<>(text, listener));
        return this;
    }

    public MiuiAlertDialog setNeutralButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNeutralButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public MiuiAlertDialog setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUseNeutralButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_NEUTRAL, new Pair<>(text, listener));
        return this;
    }

    public MiuiAlertDialog setEnableEditTextView(boolean enable) {
        mBaseFactory.isEnableEditText = enable;
        return this;
    }

    public MiuiAlertDialog setEditTextHint(@StringRes int hintId) {
        return setEditTextHint(mBaseFactory.mContext.getText(hintId));
    }

    public MiuiAlertDialog setEditTextHint(CharSequence hint) {
        mBaseFactory.mEditTextHint = hint;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(@StringRes int tipId) {
        return setEditTextTip(mBaseFactory.mContext.getText(tipId));
    }

    public MiuiAlertDialog setEditTextTip(CharSequence tip) {
        mBaseFactory.mEditTextTip = tip;
        return this;
    }

    public MiuiAlertDialog setEditTextIcon(@DrawableRes int iconId) {
        return setEditTextIcon(AppCompatResources.getDrawable(mBaseFactory.mContext, iconId));
    }

    public MiuiAlertDialog setEditTextIcon(Drawable icon) {
        mBaseFactory.mEditTextImage = icon;
        return this;
    }

    public MiuiAlertDialog setEditTextAutoKeyboard(boolean autoKeyboard) {
        mBaseFactory.mEditTextAutoKeyboard = autoKeyboard;
        return this;
    }

    public MiuiAlertDialog setEditTextInputType(int type) {
        mBaseFactory.mEditTextInputType = type;
        return this;
    }

    public MiuiAlertDialog setEditText(@StringRes int defTextId, DialogInterface.TextWatcher textWatcher) {
        return setEditText(mBaseFactory.mContext.getText(defTextId), textWatcher);
    }

    public MiuiAlertDialog setEditText(CharSequence defText, DialogInterface.TextWatcher textWatcher) {
        mBaseFactory.mDefEditText = defText;
        mBaseFactory.mTextWatcher = textWatcher;
        return this;
    }

    public MiuiAlertDialog setEnableListSelectView(boolean enable) {
        mBaseFactory.isEnableListSelect = enable;
        return this;
    }

    public MiuiAlertDialog setEnableListSpringBack(boolean enable) {
        mBaseFactory.isEnableListSpringBack = enable;
        return this;
    }

    public MiuiAlertDialog setEnableMultiSelect(boolean enable) {
        mBaseFactory.isEnableMultiSelect = enable;
        return this;
    }

    public MiuiAlertDialog setItems(@ArrayRes int itemsId, DialogInterface.OnItemsClickListener itemsChangeListener) {
        return setItems(mBaseFactory.mContext.getResources().getTextArray(itemsId), itemsChangeListener);
    }

    public MiuiAlertDialog setItems(CharSequence[] items, DialogInterface.OnItemsClickListener itemsChangeListener) {
        return setItems(new ArrayList<>(Arrays.asList(items)), itemsChangeListener);
    }

    public MiuiAlertDialog setItems(ArrayList<CharSequence> items, DialogInterface.OnItemsClickListener itemsChangeListener) {
        mBaseFactory.mItems = items;
        mBaseFactory.mItemsClickListener = itemsChangeListener;
        return this;
    }

    public MiuiAlertDialog setEnableCustomView(boolean enable) {
        mBaseFactory.isEnableCustomView = enable;
        return this;
    }

    public MiuiAlertDialog setCustomView(@LayoutRes int viewId, DialogInterface.OnBindView onBindView) {
        return setCustomView(LayoutInflater.from(mBaseFactory.mContext).inflate(viewId, mBaseFactory.mCustomLayout, false), onBindView);
    }

    public MiuiAlertDialog setCustomView(View view, DialogInterface.OnBindView onBindView) {
        mBaseFactory.mCustomView = view;
        mBaseFactory.mOnBindView = onBindView;
        return this;
    }

    public MiuiAlertDialog setHapticFeedbackEnabled(boolean enabled) {
        mBaseFactory.isEnableHapticFeedback = enabled;
        return this;
    }

    public MiuiAlertDialog setWindowAnimations(@StyleRes int windowAnimationsId) {
        mBaseFactory.mWindowAnimations = windowAnimationsId;
        return this;
    }

    public MiuiAlertDialog setOnShowListener(DialogInterface.OnShowListener onShowListener) {
        mBaseFactory.mOnShowListener = onShowListener;
        return this;
    }

    public MiuiAlertDialog setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mBaseFactory.mOnCancelListener = onCancelListener;
        return this;
    }

    public MiuiAlertDialog setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mBaseFactory.mOnDismissListener = onDismissListener;
        return this;
    }

    public MiuiAlertDialog setCancelable(boolean cancelable) {
        mBaseFactory.isCancelable = cancelable;
        return this;
    }

    public MiuiAlertDialog setCanceledOnTouchOutside(boolean cancel) {
        mBaseFactory.isCanceledOnTouchOutside = cancel;
        return this;
    }

    public MiuiAlertDialog setAutoDismiss(boolean autoDismiss) {
        mBaseFactory.isAutoDismiss = autoDismiss;
        return this;
    }

    public boolean isShowing() {
        return mBaseFactory.isShowing();
    }

    public void create() {
        mBaseFactory.create();
    }

    public void show() {
        mBaseFactory.show();
    }

    public void dismiss() {
        mBaseFactory.dismiss();
    }
}
