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

import static android.graphics.Typeface.NORMAL;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MiuiAlertDialog implements DialogInterface {
    private static final String TAG = "MiuiPreference";
    protected ConstraintLayout mMainDialog;
    TextView mTitleView;
    TextView mMessageView;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private Button mNeutralButton;
    LinearLayout mButtonLayout;
    View mEndView;
    RecyclerView mRecyclerView;
    private EditText mEditTextView;
    private TextView mEditTextTipView;
    private ImageView mEditImageView;
    private ConstraintLayout mEditLayout;
    private GradientDrawable mCustomRadius;
    boolean isDropDown = false;
    private float mRadiusValue = -1;
    private View mCustomView;
    private int mCustomViewId;
    private ConstraintLayout mCustomLayout;
    private OnBindView onBindView;
    private static final int ID_POSITIVE_BUTTON = 0;
    private static final int ID_NEGATIVE_BUTTON = 1;
    private static final int ID_NEUTRAL_BUTTON = 2;
    private boolean isSetPositiveButton;
    private boolean isSetNegativeButton;
    private boolean isSetNeutralButton;
    private boolean isMultiSelect = false;
    private boolean dismissNow;
    final ListAdapter mListAdapter;
    private final Context mContext;
    private final Dialog mDialog;
    private final Window mWindow;
    private boolean shouldShowEdit = false;
    private boolean hapticFeedbackEnabled;
    private TextWatcher mTextWatcher;
    private boolean shouldInput;
    private boolean isCreated;
    private boolean isAutoDismiss = true;
    private OnItemsChangeListener mItemsChangedListener;
    private WeakReference<Handler> mHandlerWeakReference = null;
    private ArrayList<CharSequence> mItems = new ArrayList<>();
    private final ArrayList<EditText> mEditTextViews = new ArrayList<>();
    private final HashMap<TypefaceObject, Typeface> mTypefaceHashMap = new HashMap<>();
    private final HashMap<TypefaceObject, Pair<Typeface, Integer>> mTypefaceStyleHashMap = new HashMap<>();

    public enum TypefaceObject {
        TYPEFACE_ALERT_TITLE,
        TYPEFACE_MESSAGE,
        TYPEFACE_POSITIVE_TEXT,
        TYPEFACE_NEGATIVE_TEXT,
        TYPEFACE_NEUTRAL_TEXT,
        TYPEFACE_EDIT,
        TYPEFACE_EDIT_TIP
    }

    public MiuiAlertDialog(@NonNull Context context) {
        this(context, 0);
    }

    public MiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
        this.mContext = context;
        mTypefaceHashMap.clear();
        mTypefaceStyleHashMap.clear();
        initView();
        if (themeResId == 0)
            themeResId = R.style.MiuiAlertDialog;

        mDialog = new Dialog(context, themeResId) {
            @Override
            public void dismiss() {
                if (!isShowing()) return;
                EditText edit = getVisibleEditText();
                if (edit != null) {
                    if (dismissNow) {
                        hideInputNow(edit);
                        dismissDialog();
                        dismissNow = false;
                        return;
                    }
                    hideInputIfNeed(edit, this::dismissDialog);
                } else dismissDialog();
            }

            public void dismissDialog() {
                if (mHandlerWeakReference != null) {
                    mHandlerWeakReference.clear();
                    mHandlerWeakReference = null;
                }
                if (mTextWatcher != null)
                    mEditTextView.removeTextChangedListener(mTextWatcher);
                super.dismiss();
            }
        };

        mWindow = mDialog.getWindow();
        assert mWindow != null;
        mWindow.setContentView(mMainDialog);
        mWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = mWindow.getAttributes();
        Point windowPoint = MiuiXUtils.getWindowSize(context);
        params.verticalMargin = (MiuiXUtils.dp2px(context, 16) * 1.0f) / windowPoint.y;
        params.width = MiuiXUtils.isVerticalScreen(context) ? (int) (windowPoint.x / 1.08) : (int) (windowPoint.x / 2.0);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindow.setAttributes(params);
        mWindow.setWindowAnimations(R.style.Animation_Dialog);

        mHandlerWeakReference = new WeakReference<>(new Handler(context.getApplicationContext().getMainLooper()));
        mListAdapter = new ListAdapter(this);
        if (context instanceof Activity activity) {
            activity.registerActivityLifecycleCallbacks(new ActivityLifecycle(this));
        }
    }

    private void initView() {
        mMainDialog = (ConstraintLayout) LayoutInflater.from(mContext).inflate(R.layout.miuix_dialog, null);
        mTitleView = mMainDialog.findViewById(R.id.alertTitle);
        mMessageView = mMainDialog.findViewById(android.R.id.message);
        mButtonLayout = mMainDialog.findViewById(R.id.button_view);
        mRecyclerView = mMainDialog.findViewById(R.id.list_view);
        mPositiveButton = mMainDialog.findViewById(android.R.id.button2);
        mNegativeButton = mMainDialog.findViewById(android.R.id.button1);
        mNeutralButton = mMainDialog.findViewById(android.R.id.button3);
        mEndView = mMainDialog.findViewById(R.id.end_view);
        mCustomLayout = mMainDialog.findViewById(R.id.dialog_custom);
        mEditTextView = mMainDialog.findViewById(R.id.edit_text_id);
        mEditLayout = mMainDialog.findViewById(R.id.edit_layout);
        mEditTextTipView = mMainDialog.findViewById(R.id.edit_tip);
        mEditImageView = mMainDialog.findViewById(R.id.edit_image);
        mEditLayout.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mEditTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditLayout.setBackgroundResource(R.drawable.focused_border_input_box);
                } else
                    mEditLayout.setBackgroundResource(R.drawable.nofocused_border_input_box);
            }
        });
    }

    private View.OnClickListener makeButtonClickAction(int id, DialogInterface.OnClickListener listener) {
        return v -> {
            if (hapticFeedbackEnabled)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            if (id == ID_POSITIVE_BUTTON) {
                if (mTextWatcher != null)
                    mTextWatcher.onResult(mEditTextView.getText().toString());
                if (mItemsChangedListener != null) {
                    ArrayList<CharSequence> result = new ArrayList<>();
                    for (int i = 0; i < mItems.size(); i++) {
                        if (mListAdapter.mBooleanArray.get(i)) {
                            result.add(mItems.get(i));
                        }
                    }
                    mItemsChangedListener.onResult(result, mItems, mListAdapter.mBooleanArray);
                }
            }
            if (listener != null) listener.onClick(this, id);
            if (mDialog.isShowing() && isAutoDismiss)
                dismiss();
        };
    }

    public Window getWindow() {
        return mWindow;
    }

    public MiuiAlertDialog setWindowAnimations(@StyleRes int resId) {
        mWindow.setWindowAnimations(resId);
        return this;
    }

    public MiuiAlertDialog setCornersRadius(int radius) {
        mCustomRadius = new GradientDrawable();
        mCustomRadius.setColor(mContext.getColor(R.color.white_or_black));
        mCustomRadius.setShape(GradientDrawable.RECTANGLE);
        mCustomRadius.setCornerRadius(radius);
        this.mRadiusValue = radius;
        return this;
    }

    public MiuiAlertDialog setCustomView(View view, OnBindView onBindView) {
        this.onBindView = onBindView;
        mCustomView = view;
        mCustomViewId = 0;
        return this;
    }

    public MiuiAlertDialog setCustomView(int viewId, OnBindView onBindView) {
        this.onBindView = onBindView;
        mCustomViewId = viewId;
        mCustomView = null;
        return this;
    }

    public MiuiAlertDialog setItems(@ArrayRes int items, OnItemsChangeListener listener) {
        return setItems(mContext.getResources().getTextArray(items), listener);
    }

    public MiuiAlertDialog setItems(CharSequence[] items, OnItemsChangeListener listener) {
        ArrayList<CharSequence> list = new ArrayList<>(Arrays.asList(items));
        return setItems(list, listener);
    }

    public MiuiAlertDialog setItems(ArrayList<CharSequence> items, OnItemsChangeListener listener) {
        this.mItems = items;
        mListAdapter.update(listener);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mListAdapter);
        mItemsChangedListener = listener;
        return this;
    }

    public MiuiAlertDialog isMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        return this;
    }

    public MiuiAlertDialog setTitle(@StringRes int titleResId) {
        return setTitle(mContext.getResources().getText(titleResId));
    }

    public MiuiAlertDialog setTitle(CharSequence title) {
        mTitleView.setText(title);
        mTitleView.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setTitleSize(float size) {
        mTitleView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setTextTypeface(MakeTypeface makeTypeface) {
        makeTypeface.onMakeTypeface(mTypefaceHashMap);
        makeTypeface.onMakeTypefaceStyle(mTypefaceStyleHashMap);
        return this;
    }

    public MiuiAlertDialog setMessage(@StringRes int messageResId) {
        return setMessage(mContext.getResources().getText(messageResId));
    }

    public MiuiAlertDialog setMessage(CharSequence message) {
        this.mMessageView.setText(message);
        this.mMessageView.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setMessageSize(float size) {
        mMessageView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setPositiveButton(mContext.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mPositiveButton.setText(text);
        mPositiveButton.setOnClickListener(makeButtonClickAction(ID_POSITIVE_BUTTON, listener));
        isSetPositiveButton = true;
        return this;
    }

    public MiuiAlertDialog setPositiveButtonTextSize(float size) {
        mPositiveButton.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNegativeButton(mContext.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mNegativeButton.setText(text);
        mNegativeButton.setOnClickListener(makeButtonClickAction(ID_NEGATIVE_BUTTON, listener));
        isSetNegativeButton = true;
        return this;
    }

    public MiuiAlertDialog setNegativeButtonTextSize(float size) {
        mNegativeButton.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setNeutralButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNeutralButton(mContext.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        mNeutralButton.setText(text);
        mNeutralButton.setVisibility(View.VISIBLE);
        mNeutralButton.setOnClickListener(makeButtonClickAction(ID_NEUTRAL_BUTTON, listener));
        isSetNeutralButton = true;
        return this;
    }

    public MiuiAlertDialog setNeutralButtonTextSize(float size) {
        mNeutralButton.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setContentView(@LayoutRes int layoutResID) {
        mWindow.setContentView(layoutResID);
        return this;
    }

    public MiuiAlertDialog setEditText(TextWatcher watcher) {
        return setEditText(null, watcher);
    }

    public MiuiAlertDialog setEditText(CharSequence defText, TextWatcher watcher) {
        return setEditText(defText, false, watcher);
    }

    public MiuiAlertDialog setEditText(CharSequence defText, boolean needInput, TextWatcher watcher) {
        if (defText != null) {
            mEditTextView.setText(defText);
            mEditTextView.setSelection(mEditTextView.getText().length());
        }
        this.shouldInput = needInput;
        if (watcher != null)
            mEditTextView.addTextChangedListener(watcher);
        mTextWatcher = watcher;
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextSize(float size) {
        mEditTextView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setInputType(int type) {
        mEditTextView.setInputType(type);
        return this;
    }

    public MiuiAlertDialog setEditHint(CharSequence text) {
        mEditTextView.setHint(text);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditHint(@StringRes int textResId) {
        mEditTextView.setHint(textResId);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(CharSequence textTip) {
        mEditTextTipView.setVisibility(View.VISIBLE);
        mEditTextTipView.setText(textTip);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(@StringRes int textTipResId) {
        return setEditTextTip(mContext.getText(textTipResId));
    }

    public MiuiAlertDialog setEditTextTipSize(float size) {
        mEditTextTipView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setEditTextImage(Drawable drawable) {
        mEditImageView.setVisibility(View.VISIBLE);
        mEditImageView.setImageDrawable(drawable);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextImage(@DrawableRes int drawable) {
        return setEditTextImage(AppCompatResources.getDrawable(mContext, drawable));
    }

    public MiuiAlertDialog setHapticFeedbackEnabled(boolean enabled) {
        hapticFeedbackEnabled = enabled;
        return this;
    }

    public MiuiAlertDialog setCancelable(boolean cancelable) {
        mDialog.setCancelable(cancelable);
        return this;
    }

    public MiuiAlertDialog setCanceledOnTouchOutside(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public MiuiAlertDialog setOnDismissListener(OnDismissListener dismissListener) {
        mDialog.setOnDismissListener(dialog -> dismissListener.onDismiss(MiuiAlertDialog.this));
        return this;
    }

    public MiuiAlertDialog autoDismiss(boolean autoDismiss) {
        this.isAutoDismiss = autoDismiss;
        return this;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public MiuiAlertDialog create() {
        if (isCreated) return this;
        if (isSetNeutralButton) {
            mButtonLayout.setOrientation(LinearLayout.VERTICAL);
            margin(mNegativeButton, MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 25), 0, 0);
            margin(mNeutralButton, MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 10), 0);
            margin(mPositiveButton, MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 10), 0);
        } else {
            if (!isSetPositiveButton && !isSetNegativeButton)
                mDialog.setCancelable(true); // 防止无法关闭 dialog
            if (!isSetPositiveButton || !isSetNegativeButton) {
                margin(mPositiveButton, MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 25), 0, 0);
                margin(mNegativeButton, MiuiXUtils.dp2px(mContext, 25), MiuiXUtils.dp2px(mContext, 25), 0, 0);
            }
        }
        if (mCustomView != null || mCustomViewId != 0) {
            setupCustomContent();
        } else {
            if (shouldShowEdit) {
                mEditLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE); // 不支持同时显示文本输入框和多选菜单 (至少是我不想写
                mRecyclerView.setAdapter(null);
            } else {
                RecyclerViewCornerRadius cornerRadius = new RecyclerViewCornerRadius(mRecyclerView);
                float radius = (this.mRadiusValue == -1) ? MiuiXUtils.dp2px(mContext, 32) : this.mRadiusValue;
                cornerRadius.setCornerRadius(radius);
                if (mItems != null && !isDropDown) {
                    cornerRadius.setCornerRadius(0);
                    ViewGroup.LayoutParams layout = mRecyclerView.getLayoutParams();
                    int height = (MiuiXUtils.dp2px(mContext, 56) * (mItems.size())) + MiuiXUtils.dp2px(mContext, 20);
                    int maxHeight = MiuiXUtils.isVerticalScreen(mContext) ? MiuiXUtils.getWindowSize(mContext).y / 3 : (int) (MiuiXUtils.getWindowSize(mContext).y / 2.5);
                    layout.height = Math.min(height, maxHeight);
                    mRecyclerView.setLayoutParams(layout);

                    if (isSetPositiveButton || isSetNegativeButton || isSetNeutralButton) {
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mButtonLayout.getLayoutParams();
                        layoutParams.topMargin = MiuiXUtils.dp2px(mContext, 20);
                        mButtonLayout.setLayoutParams(layoutParams);
                    } else {
                        mEndView.setVisibility(View.GONE);
                        mButtonLayout.setVisibility(View.GONE);
                        cornerRadius.setCornerRadius(-1, -1, radius, radius);
                    }

                    if (mTitleView.getVisibility() == View.GONE && mMessageView.getVisibility() == View.GONE) {
                        cornerRadius.setCornerRadius(radius, radius, -1, -1);
                    }
                }
                mRecyclerView.addItemDecoration(cornerRadius);
            }
        }
        if (!isSetNeutralButton) mNeutralButton.setVisibility(View.GONE);
        if (!isSetNegativeButton) mNegativeButton.setVisibility(View.GONE);
        if (!isSetPositiveButton) mPositiveButton.setVisibility(View.GONE);
        if (mCustomRadius != null) mMainDialog.setBackground(mCustomRadius);
        if (mMessageView.getVisibility() == View.VISIBLE && mTitleView.getVisibility() == View.GONE) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mMessageView.getLayoutParams();
            params.topMargin = MiuiXUtils.dp2px(mContext, 25);
            mMessageView.setLayoutParams(params);
        }
        setTextTypeface();
        mDialog.create();
        isCreated = true;
        return this;
    }

    public void show() {
        if (!isCreated) create();
        mDialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
            @Override
            public void onShow(android.content.DialogInterface d) {
                if (shouldInput)
                    showInputIfNeed();
            }
        });
        mDialog.show();
    }

    public void cancel() {
        mDialog.cancel();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    private void dismissNow() {
        dismissNow = true;
        mDialog.dismiss();
    }

    private void setTextTypeface() {
        if (mTypefaceHashMap.isEmpty()) {
            if (mTypefaceStyleHashMap.isEmpty())
                return;
            else {
                mTypefaceStyleHashMap.forEach((typefaceObject, typefaceIntegerPair) ->
                        setTypeface(typefaceObject, typefaceIntegerPair.first, typefaceIntegerPair.second));
            }
        } else {
            mTypefaceHashMap.forEach((typefaceObject, typeface) ->
                    setTypeface(typefaceObject, typeface, NORMAL));
        }
    }

    private void setTypeface(TypefaceObject typefaceObject, Typeface typeface, int style) {
        switch (typefaceObject) {
            case TYPEFACE_ALERT_TITLE -> {
                mTitleView.setTypeface(typeface, style);
            }
            case TYPEFACE_MESSAGE -> {
                mMessageView.setTypeface(typeface, style);
            }
            case TYPEFACE_POSITIVE_TEXT -> {
                mPositiveButton.setTypeface(typeface, style);
            }
            case TYPEFACE_NEGATIVE_TEXT -> {
                mNegativeButton.setTypeface(typeface, style);
            }
            case TYPEFACE_NEUTRAL_TEXT -> {
                mNeutralButton.setTypeface(typeface, style);
            }
            case TYPEFACE_EDIT -> {
                mEditTextView.setTypeface(typeface, style);
            }
            case TYPEFACE_EDIT_TIP -> {
                mEditTextTipView.setTypeface(typeface, style);
            }
        }
    }

    private void setupCustomContent() {
        View view = mCustomView != null ? mCustomView :
                (mCustomViewId != 0 ? LayoutInflater.from(mContext).inflate(mCustomViewId, mCustomLayout, false) : null);
        if (view == null) return;
        ViewGroup viewParent = (ViewGroup) view.getParent();
        if (viewParent != mCustomLayout) {
            if (viewParent != null)
                viewParent.removeView(view);
            mCustomLayout.addView(view);
        }
        mCustomLayout.setVisibility(View.VISIBLE);

        checkChildAddEditText(mCustomLayout);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCustomLayout.getLayoutParams();
        params.setMarginStart(MiuiXUtils.dp2px(mContext, 25));
        params.setMarginEnd(MiuiXUtils.dp2px(mContext, 25));
        if (isSetNegativeButton || isSetPositiveButton || isSetNeutralButton)
            params.bottomMargin = MiuiXUtils.dp2px(mContext, 25);
        params.topMargin = MiuiXUtils.dp2px(mContext, 25);
        mCustomLayout.setLayoutParams(params);

        if (onBindView != null)
            onBindView.onBindView(view);

        mEditLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setAdapter(null);
    }

    private void checkChildAddEditText(ViewGroup customLayout) {
        for (int i = 0; i < customLayout.getChildCount(); i++) {
            View v = customLayout.getChildAt(i);
            if (v instanceof ViewGroup viewGroup) {
                checkChildAddEditText(viewGroup);
            }
            if (v instanceof EditText) {
                mEditTextViews.add((EditText) v);
            }
        }
    }

    private void hideInputIfNeed(EditText editText, Runnable runnable) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible(editText)) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0, new ResultReceiver(mHandlerWeakReference.get()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (mHandlerWeakReference == null || mHandlerWeakReference.get() == null) {
                        runnable.run();
                        return;
                    }
                    mHandlerWeakReference.get().postDelayed(runnable, 300);
                }
            });
        }
    }

    private void hideInputNow(EditText editText) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible(editText)) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private @Nullable EditText getVisibleEditText() {
        EditText edit = null;
        if (mEditLayout.getVisibility() == View.VISIBLE) {
            if (isInputVisible(mEditTextView))
                edit = mEditTextView;
        } else {
            for (EditText e : mEditTextViews) {
                if (isInputVisible(e)) {
                    edit = e;
                    break;
                }
            }
        }
        return edit;
    }

    private boolean isInputVisible(EditText editText) {
        if (editText == null) return false;
        if (editText.getRootWindowInsets() == null) return false;
        return editText.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
    }

    private void showInputIfNeed() {
        mEditTextView.setFocusable(true);
        mEditTextView.setFocusableInTouchMode(true);
        mEditTextView.requestFocus();
        if (!isInputVisible(mEditTextView)) {
            WindowInsetsController windowInsetsController = mWindow.getDecorView().getWindowInsetsController();
            if (windowInsetsController != null)
                windowInsetsController.show(WindowInsets.Type.ime());
            else {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditTextView, 0);
            }
        }
    }

    private void margin(View v, int start, int end, int top, int bottom) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(v.getLayoutParams());
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        v.setLayoutParams(layoutParams);
    }

    public interface OnBindView {
        void onBindView(View view);
    }

    public interface MakeTypeface {
        default void onMakeTypeface(HashMap<TypefaceObject, Typeface> typefaceHashMap) {
            typefaceHashMap.clear();
        }

        default void onMakeTypefaceStyle(HashMap<TypefaceObject, Pair<Typeface, Integer>> typefaceStyleHashMap) {
            typefaceStyleHashMap.clear();
        }
    }

    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
        private final MiuiAlertDialog mDialog;
        SparseBooleanArray mBooleanArray = new SparseBooleanArray();
        private OnItemsChangeListener mListener;
        private GradientDrawable mDrawableTop;
        private GradientDrawable mDrawableBottom;
        private boolean isChecked;

        protected ListAdapter(MiuiAlertDialog dialog) {
            this.mDialog = dialog;
        }

        private void setDrawableColor(@ColorInt int color) {
            mDrawableTop = new GradientDrawable();
            mDrawableBottom = new GradientDrawable();
            mDrawableTop.setColor(color);
            mDrawableBottom.setColor(color);
            float radius = (mDialog.mRadiusValue == -1) ? MiuiXUtils.dp2px(mDialog.mContext, 32) : mDialog.mRadiusValue;
            if (isChecked || mDialog.isSetNegativeButton || mDialog.isSetPositiveButton || mDialog.isSetNeutralButton
                    || mDialog.mTitleView.getVisibility() == View.VISIBLE || mDialog.mMessageView.getVisibility() == View.VISIBLE) {
                isChecked = true;
                if (!mDialog.isDropDown) radius = 0;
            }
            if (mDialog.mTitleView.getVisibility() == View.GONE && mDialog.mMessageView.getVisibility() == View.GONE && !mDialog.isDropDown) {
                float TopRadius = (mDialog.mRadiusValue == -1) ? MiuiXUtils.dp2px(mDialog.mContext, 32) : mDialog.mRadiusValue;
                mDrawableTop.setCornerRadii(new float[]{TopRadius, TopRadius, TopRadius, TopRadius, 0, 0, 0, 0});
            } else
                mDrawableTop.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
            mDrawableBottom.setCornerRadii(new float[]{0, 0, 0, 0, radius, radius, radius, radius});
        }

        public void update(OnItemsChangeListener listener) {
            this.mListener = listener;
        }

        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            CharSequence s = mDialog.mItems.get(position);
            setFirstOrEndView(holder, position);
            holder.mSwitchView.setText(s);
            holder.mSwitchView.setHapticFeedbackEnabled(false);
            boolean isChecked = mBooleanArray.get(position);
            checkState(holder, position);

            holder.mSwitchView.setOnCheckedChangeListener(null);
            holder.mSwitchView.setOnHoverListener(null);
            holder.mSwitchView.setChecked(isChecked);

            holder.mSwitchView.setOnCheckedChangeListener((b, i) -> {
                if (mDialog.isMultiSelect) mBooleanArray.put(position, i);
                checkState(holder, position);
                if (mDialog.hapticFeedbackEnabled)
                    holder.mMainLayout.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                if (mListener != null) mListener.onClick(mDialog, s, position);
                if (!mDialog.isMultiSelect)
                    mDialog.dismiss();
            });
            holder.mSwitchView.setOnHoverListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE -> {
                        holder.mMainLayout.setBackgroundResource(R.color.touch_down);
                    }
                    case MotionEvent.ACTION_HOVER_EXIT -> {
                        checkState(holder, position);
                    }
                    default -> {
                        return false;
                    }
                }
                return true;
            });
        }

        private void checkState(@NonNull ListViewHolder holder, int position) {
            if (mBooleanArray.get(position)) {
                setDrawableColor(mDialog.mContext.getColor(R.color.list_state_background));
                if (position == 0) holder.mMainLayout.setBackground(mDrawableTop);
                else if (position == mDialog.mItems.size() - 1) {
                    holder.mMainLayout.setBackground(mDrawableBottom);
                } else
                    holder.mMainLayout.setBackgroundResource(R.drawable.list_choose_item_background);
                holder.mSwitchView.setTextColor(mDialog.mContext.getColor(R.color.list_choose_text));
                holder.mImageView.setVisibility(View.VISIBLE);
            } else {
                setDrawableColor(mDialog.mContext.getColor(R.color.list_background));
                if (position == 0) holder.mMainLayout.setBackground(mDrawableTop);
                else if (position == mDialog.mItems.size() - 1) {
                    holder.mMainLayout.setBackground(mDrawableBottom);
                } else holder.mMainLayout.setBackgroundResource(R.drawable.list_item_background);
                holder.mSwitchView.setTextColor(mDialog.mContext.getColor(R.color.list_text));
                holder.mImageView.setVisibility(View.GONE);
            }
        }

        private void setFirstOrEndView(@NonNull ListViewHolder holder, int position) {
            if (position == 0 || position == mDialog.mItems.size() - 1) {
                holder.mFirstView.setVisibility(View.VISIBLE);
                holder.mEndView.setVisibility(View.VISIBLE);
            } else {
                holder.mFirstView.setVisibility(View.GONE);
                holder.mEndView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mDialog.mItems.size();
        }

        public static class ListViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout mMainLayout;
            View mFirstView;
            View mEndView;
            SwitchCompat mSwitchView;
            ImageView mImageView;

            public ListViewHolder(@NonNull View itemView) {
                super(itemView);
                mMainLayout = (ConstraintLayout) itemView;
                mFirstView = itemView.findViewById(R.id.first_view);
                mEndView = itemView.findViewById(R.id.end_view);
                mSwitchView = itemView.findViewById(R.id.list_item);
                mImageView = itemView.findViewById(R.id.list_image);
                mImageView.setVisibility(View.GONE);
            }
        }
    }

    /** @noinspection ClassCanBeRecord */
    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private final MiuiAlertDialog mDialog;

        public ActivityLifecycle(MiuiAlertDialog dialog) {
            this.mDialog = dialog;
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPreDestroyed(@NonNull Activity activity) {
            if (mDialog.isShowing()) {
                mDialog.mHandlerWeakReference.clear();
                mDialog.mHandlerWeakReference = null;
                mDialog.dismissNow();
            }
        }
    }
}
