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

public class NewMiuiAlertDialog {
    private final MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory mBaseFactory;

    public NewMiuiAlertDialog(@NonNull Context context) {
        this(context, R.style.MiuiAlertDialog);
    }

    public NewMiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
        this(context, themeResId, false);
    }

    protected NewMiuiAlertDialog(@NonNull Context context, boolean enableDropDownMode) {
        this(context, R.style.MiuiAlertDialog, enableDropDownMode);
    }

    protected NewMiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId, boolean enableDropDownMode) {
        mBaseFactory = new MiuiAlertDialogFactory(context, themeResId, enableDropDownMode).init();
    }

    public Window getWindow() {
        return mBaseFactory.mWindow;
    }

    protected MiuiAlertDialogFactory.MiuiAlertDialogBaseFactory getBaseFactory() {
        return mBaseFactory;
    }

    public NewMiuiAlertDialog setTitle(@StringRes int titleId) {
        return setTitle(mBaseFactory.mContext.getText(titleId));
    }

    public NewMiuiAlertDialog setTitle(CharSequence title) {
        mBaseFactory.mTitleView.setVisibility(View.VISIBLE);
        mBaseFactory.mTitleView.setText(title);
        return this;
    }

    public NewMiuiAlertDialog setMessage(@StringRes int messageId) {
        return setMessage(mBaseFactory.mContext.getText(messageId));
    }

    public NewMiuiAlertDialog setMessage(CharSequence message) {
        mBaseFactory.mMessageView.setVisibility(View.VISIBLE);
        mBaseFactory.mMessageView.setText(message);
        return this;
    }

    public NewMiuiAlertDialog setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setPositiveButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public NewMiuiAlertDialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUsePositiveButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_POSITIVE, new Pair<>(text, listener));
        return this;
    }

    public NewMiuiAlertDialog setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNegativeButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public NewMiuiAlertDialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUseNegativeButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_NEGATIVE, new Pair<>(text, listener));
        return this;
    }

    public NewMiuiAlertDialog setNeutralButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNeutralButton(mBaseFactory.mContext.getText(textId), listener);
    }

    public NewMiuiAlertDialog setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mBaseFactory.isUseNeutralButton = true;
        mBaseFactory.mButtonHashMap.put(BUTTON_NEUTRAL, new Pair<>(text, listener));
        return this;
    }

    public NewMiuiAlertDialog setEnableEditTextView(boolean enable) {
        mBaseFactory.isEnableEditText = enable;
        return this;
    }

    public NewMiuiAlertDialog setEditTextHint(@StringRes int hintId) {
        return setEditTextHint(mBaseFactory.mContext.getText(hintId));
    }

    public NewMiuiAlertDialog setEditTextHint(CharSequence hint) {
        mBaseFactory.mEditTextHint = hint;
        return this;
    }

    public NewMiuiAlertDialog setEditTextTip(@StringRes int tipId) {
        return setEditTextTip(mBaseFactory.mContext.getText(tipId));
    }

    public NewMiuiAlertDialog setEditTextTip(CharSequence tip) {
        mBaseFactory.mEditTextTip = tip;
        return this;
    }

    public NewMiuiAlertDialog setEditTextIcon(@DrawableRes int iconId) {
        return setEditTextIcon(AppCompatResources.getDrawable(mBaseFactory.mContext, iconId));
    }

    public NewMiuiAlertDialog setEditTextIcon(Drawable icon) {
        mBaseFactory.mEditTextImage = icon;
        return this;
    }

    public NewMiuiAlertDialog setEditTextAutoKeyboard(boolean autoKeyboard) {
        mBaseFactory.mEditTextAutoKeyboard = autoKeyboard;
        return this;
    }
    
    public NewMiuiAlertDialog setEditTextInputType(int type){
        mBaseFactory.mEditTextInputType = type;
        return this;
    }

    public NewMiuiAlertDialog setEditText(@StringRes int defTextId, DialogInterface.TextWatcher textWatcher) {
        return setEditText(mBaseFactory.mContext.getText(defTextId), textWatcher);
    }

    public NewMiuiAlertDialog setEditText(CharSequence defText, DialogInterface.TextWatcher textWatcher) {
        mBaseFactory.mDefEditText = defText;
        mBaseFactory.mTextWatcher = textWatcher;
        return this;
    }

    public NewMiuiAlertDialog setEnableListSelectView(boolean enable) {
        mBaseFactory.isEnableListSelect = enable;
        return this;
    }

    public NewMiuiAlertDialog setEnableListSpringBack(boolean enable) {
        mBaseFactory.isEnableListSpringBack = enable;
        return this;
    }

    public NewMiuiAlertDialog setEnableMultiSelect(boolean enable) {
        mBaseFactory.isEnableMultiSelect = enable;
        return this;
    }

    public NewMiuiAlertDialog setItems(@ArrayRes int itemsId, DialogInterface.OnItemsClickListener itemsChangeListener) {
        return setItems(mBaseFactory.mContext.getResources().getTextArray(itemsId), itemsChangeListener);
    }

    public NewMiuiAlertDialog setItems(CharSequence[] items, DialogInterface.OnItemsClickListener itemsChangeListener) {
        return setItems(new ArrayList<>(Arrays.asList(items)), itemsChangeListener);
    }

    public NewMiuiAlertDialog setItems(ArrayList<CharSequence> items, DialogInterface.OnItemsClickListener itemsChangeListener) {
        mBaseFactory.mItems = items;
        mBaseFactory.mItemsClickListener = itemsChangeListener;
        return this;
    }

    public NewMiuiAlertDialog setEnableCustomView(boolean enable) {
        mBaseFactory.isEnableCustomView = enable;
        return this;
    }

    public NewMiuiAlertDialog setCustomView(@LayoutRes int viewId, DialogInterface.OnBindView onBindView) {
        return setCustomView(LayoutInflater.from(mBaseFactory.mContext).inflate(viewId, mBaseFactory.mCustomLayout, false), onBindView);
    }

    public NewMiuiAlertDialog setCustomView(View view, DialogInterface.OnBindView onBindView) {
        mBaseFactory.mCustomView = view;
        mBaseFactory.mOnBindView = onBindView;
        return this;
    }

    public NewMiuiAlertDialog setHapticFeedbackEnabled(boolean enabled) {
        mBaseFactory.isEnableHapticFeedback = enabled;
        return this;
    }

    public NewMiuiAlertDialog setWindowAnimations(@StyleRes int resId) {
        mBaseFactory.mWindow.setWindowAnimations(resId);
        return this;
    }

    public NewMiuiAlertDialog setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mBaseFactory.mDialog.setOnDismissListener(dialog -> onDismissListener.onDismiss(mBaseFactory));
        return this;
    }

    public NewMiuiAlertDialog setCancelable(boolean cancelable) {
        mBaseFactory.mDialog.setCancelable(cancelable);
        return this;
    }

    public NewMiuiAlertDialog setCanceledOnTouchOutside(boolean cancel) {
        mBaseFactory.mDialog.setCanceledOnTouchOutside(cancel);
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
