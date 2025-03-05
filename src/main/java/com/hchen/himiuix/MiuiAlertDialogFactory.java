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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
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

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.springback.SpringBackLayout;
import com.hchen.himiuix.widget.MiuiEditText;

import java.util.ArrayList;
import java.util.HashMap;

public class MiuiAlertDialogFactory {
    private final static String TAG = "MiuiPreference";
    private final Context mContext;
    private final int mThemeResId;
    private final boolean mEnableDropDownMode;

    protected MiuiAlertDialogFactory(Context context, @StyleRes int themeResId, boolean enableDropDownMode) {
        mContext = context;
        mThemeResId = themeResId;
        mEnableDropDownMode = enableDropDownMode;
    }

    protected MiuiAlertDialogBaseFactory init() {
        Dialog mDialog = new Dialog(mContext, mThemeResId);
        MiuiAlertDialogBaseFactory baseFactory;
        if (mEnableDropDownMode)
            baseFactory = new MiuiAlertDialogDropDownFactory(mDialog);
        else {
            baseFactory = MiuiXUtils.isVerticalScreen(mContext) ?
                new MiuiAlertDialogVerticalFactory(mDialog) :
                (MiuiXUtils.isPad(mContext) ?
                    new MiuiAlertDialogVerticalFactory(mDialog) :
                    new MiuiAlertDialogHorizontalFactory(mDialog));
        }
        baseFactory.init();
        return baseFactory;
    }

    private static class MiuiAlertDialogVerticalFactory extends MiuiAlertDialogBaseFactory {
        public MiuiAlertDialogVerticalFactory(Dialog dialog) {
            super(dialog);
        }

        @Override
        @SuppressLint("InflateParams")
        public void init() {
            mMainDialogLayout = (ConstraintLayout) LayoutInflater.from(mContext).inflate(R.layout.miuix_vertical_dialog, null);

            mWindow.setContentView(mMainDialogLayout);
            if (MiuiXUtils.isPad(mContext)) {
                mWindow.setGravity(Gravity.CENTER); // 中心
                WindowManager.LayoutParams params = mWindow.getAttributes();
                params.width = (int) (mPoint.x / 2.5); // 距离屏幕左右间隔
                params.height = WindowManager.LayoutParams.WRAP_CONTENT; // 自适应
                mWindow.setAttributes(params);
            } else {
                mWindow.setGravity(Gravity.BOTTOM); // 底部
                WindowManager.LayoutParams params = mWindow.getAttributes();
                params.verticalMargin = (float) MiuiXUtils.dp2px(mContext, 16) / mPoint.y; // 距离底部的百分比
                params.width = (int) (mPoint.x / 1.08); // 距离屏幕左右间隔
                params.height = WindowManager.LayoutParams.WRAP_CONTENT; // 自适应
                mWindow.setAttributes(params);
            }
            mWindow.setWindowAnimations(R.style.Animation_Dialog); // 弹出动画

            loadView();
            hideAllViews();
        }

        @Override
        protected void updateView() {
            super.updateView();
            if (isUsePositiveButton && isUseNegativeButton && isUseNeutralButton)
                loadButtonView(R.layout.miuix_vertical_button); // 垂直布局
            else
                loadButtonView(R.layout.miuix_horizontal_button); // 水平布局

            if (isEnableCustomView) {
                loadCustomView();
                return;
            }
            if (isEnableEditText)
                loadEditTextView();
            else if (isEnableListSelect)
                loadListSelectView(mPoint.y / 3);
        }

        /*
         * 根据使用的 button 更新位置。
         * */
        @Override
        protected void updateButtonLocation() {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mNeutralButton.getLayoutParams();
            if (!isUsePositiveButton && isUseNegativeButton && isUseNeutralButton) {
                params.weight = 1;
                params.setMarginEnd(0);
                params.setMarginStart(MiuiXUtils.dp2px(mContext, 10));
            } else if (isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                params.weight = 1;
                params.setMarginStart(0);
                params.setMarginEnd(MiuiXUtils.dp2px(mContext, 10));
            } else if (!isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                params.weight = 1;
                params.setMarginStart(0);
                params.setMarginEnd(0);
            }
            mNeutralButton.setLayoutParams(params);
        }

        @Override
        protected void updateCustomLayoutBottomMarginIfNeed() {
            ConstraintLayout.LayoutParams customParams = (ConstraintLayout.LayoutParams) mCustomLayout.getLayoutParams();
            customParams.bottomMargin = MiuiXUtils.dp2px(mContext, 25);
            mCustomLayout.setLayoutParams(customParams);
        }
    }

    private static class MiuiAlertDialogHorizontalFactory extends MiuiAlertDialogBaseFactory {
        public MiuiAlertDialogHorizontalFactory(Dialog dialog) {
            super(dialog);
        }

        @Override
        public void init() {
            mMainDialogLayout = (ConstraintLayout) LayoutInflater.from(mContext).inflate(R.layout.miuix_horizontal_dialog, null);

            mWindow.setContentView(mMainDialogLayout);
            mWindow.setGravity(Gravity.BOTTOM); // 底部
            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.verticalMargin = (float) MiuiXUtils.dp2px(mContext, 16) / mPoint.y; // 距离底部的百分比
            params.width = (int) (mPoint.x / 1.5); // 距离屏幕左右间隔
            params.height = WindowManager.LayoutParams.WRAP_CONTENT; // 自适应
            mWindow.setAttributes(params);
            mWindow.setWindowAnimations(R.style.Animation_Dialog); // 弹出动画

            loadView();
            hideAllViews();
        }

        @Override
        protected void updateView() {
            super.updateView();
            loadButtonView(R.layout.miuix_vertical_button);

            if (isEnableCustomView) {
                loadCustomView();
                return;
            }
            if (isEnableEditText)
                loadEditTextView();
            else if (isEnableListSelect) {
                loadListSelectView((int) (mPoint.y / 4));
            }
        }

        /*
         * 根据使用的 button 更新位置。
         * */
        @Override
        protected void updateButtonLocation() {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mNeutralButton.getLayoutParams();
            if (isUsePositiveButton && isUseNegativeButton && !isUseNeutralButton) {
                params.weight = 0;
                params.topMargin = 0;
            } else if (!isUsePositiveButton && isUseNegativeButton && isUseNeutralButton) {
                params.topMargin = 0;
            } else if (isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                params.bottomMargin = 0;
            } else if (!isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                params.topMargin = 0;
                params.bottomMargin = 0;
            }
            mNeutralButton.setLayoutParams(params);
        }

        @Override
        protected void updateCustomLayoutBottomMarginIfNeed() {
            super.updateCustomLayoutBottomMarginIfNeed();
        }
    }

    protected static class MiuiAlertDialogDropDownFactory extends MiuiAlertDialogBaseFactory {
        private View mRootView;
        private boolean isVerticalScreen;

        public MiuiAlertDialogDropDownFactory(Dialog dialog) {
            super(dialog);
        }

        @Override
        public void init() {
            mMainDialogLayout = (ConstraintLayout) LayoutInflater.from(mContext).inflate(R.layout.miuix_dropdown_dialog, null);

            mWindow.setContentView(mMainDialogLayout);
            mWindow.setWindowAnimations(R.style.Animation_PopupWindow_DropDown);

            isVerticalScreen = MiuiXUtils.isVerticalScreen(mContext);
        }

        @Override
        protected void updateView() {
            super.updateView();
            loadDropDownListSelectView();
        }

        private void loadDropDownListSelectView() {
            mRecyclerView = new MiuiAlertDialogRecyclerView(mContext).getRecyclerView();
            addView(mMainDialogLayout, mRecyclerView);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mRecyclerView.setAdapter(mListAdapter = new MiuiAlertDialogRecyclerView.MiuiAlertDialogListAdapter(this));

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRecyclerView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mRecyclerView.setLayoutParams(params);

            RecyclerViewCornerRadius cornerRadius = new RecyclerViewCornerRadius(mRecyclerView);
            cornerRadius.setCornerRadius(MiuiXUtils.dp2px(mContext, 18));
            mRecyclerView.addItemDecoration(cornerRadius);
        }

        public void setRootPreferenceView(View rootView) {
            mRootView = rootView;
        }

        public void showDialogByTouchPosition(float x, float y) {
            int dialogHeight = calculateHeight();
            int windowHeight = mPoint.y;
            int[] location = new int[2];
            mRootView.getLocationOnScreen(location);
            int viewX = location[0];
            int viewY = location[1];
            int viewWidth = mRootView.getWidth();
            int viewHeight = mRootView.getHeight();

            int spaceBelow = windowHeight - (viewY + viewHeight);
            boolean showBelow = (spaceBelow - dialogHeight) > windowHeight / 8;
            boolean showRight = x > ((float) (viewX + viewWidth) / 2);

            mWindow.setGravity(Gravity.TOP | (showRight ? Gravity.RIGHT : Gravity.LEFT));
            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.x = MiuiXUtils.dp2px(mContext, 35) /* 距离屏幕边缘 */;
            params.y = showBelow ? /* 是否显示在下方 */
                viewY + MiuiXUtils.dp2px(mContext, 15) :
                viewY - dialogHeight - MiuiXUtils.dp2px(mContext, 10);
            params.width = calculateWidth();
            params.height = dialogHeight;
            mWindow.setAttributes(params);

            updateWindowAnimations(showBelow, showRight);
        }

        private void updateWindowAnimations(boolean showBelow, boolean showRight) {
            if (showBelow) {
                if (showRight)
                    mWindow.setWindowAnimations(R.style.Animation_PopupWindow_DropDown_RightTop);
                else
                    mWindow.setWindowAnimations(R.style.Animation_PopupWindow_DropDown_LeftTop);
            } else {
                if (showRight)
                    mWindow.setWindowAnimations(R.style.Animation_PopupWindow_DropDown_RightBottom);
                else
                    mWindow.setWindowAnimations(R.style.Animation_PopupWindow_DropDown_LeftBottom);
            }
        }

        private int calculateWidth() {
            final int[] textWidth = {-1};
            mItems.forEach(sequence -> {
                int width = sequence.length() * MiuiXUtils.sp2px(mContext, 18);
                if (width > textWidth[0])
                    textWidth[0] = width;
            });

            textWidth[0] = textWidth[0] + MiuiXUtils.dp2px(mContext, 80 + (isVerticalScreen ? 65 : 80) /* 增加间隔 */);
            int maxWidth = isVerticalScreen ? (int) (mPoint.x / 1.5) : (int) (mPoint.x / 2.8);

            return Math.min(textWidth[0], maxWidth);
        }

        private int calculateHeight() {
            if (mItems != null) {
                int height = MiuiXUtils.dp2px(mContext, 58) * mItems.size();
                int maxHeight = isVerticalScreen ?
                    (int) (mPoint.y / 2.7) : // 竖屏最大高度
                    (int) (mPoint.y / 2.1); // 横屏最大高度
                return Math.min(height, maxHeight);
            } else return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    protected static abstract class MiuiAlertDialogBaseFactory implements DialogInterface {
        public Dialog mDialog;
        public Window mWindow;
        protected Point mPoint;
        public Context mContext;
        public ConstraintLayout mMainDialogLayout;
        public TextView mTitleView;
        public TextView mMessageView;
        public CharSequence mTitle;
        public CharSequence mMessage;
        public ConstraintLayout mCustomLayout;
        public LinearLayout mButtonLayout;
        public HashMap<Integer, Pair<CharSequence,
            OnClickListener>> mButtonHashMap = new HashMap<>();
        protected Button mNegativeButton;
        protected Button mPositiveButton;
        protected Button mNeutralButton;
        public boolean isUsePositiveButton;
        public boolean isUseNegativeButton;
        public boolean isUseNeutralButton;
        public boolean isEnableEditText;
        protected EditText mEditText;
        public CharSequence mDefEditText = "";
        public CharSequence mEditTextHint = "";
        public CharSequence mEditTextTip = "";
        public Drawable mEditTextImage;
        public boolean mEditTextAutoKeyboard;
        public int mEditTextInputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        public DialogInterface.TextWatcher mTextWatcher;
        public boolean isEnableListSelect;
        public ArrayList<CharSequence> mItems;
        protected RecyclerView mRecyclerView;
        protected MiuiAlertDialogRecyclerView.MiuiAlertDialogListAdapter mListAdapter;
        protected SparseBooleanArray mBooleanArray = new SparseBooleanArray();
        public DialogInterface.OnItemsClickListener mItemsClickListener;
        public boolean isEnableListSpringBack;
        public boolean isEnableMultiSelect;
        public boolean isEnableCustomView;
        public View mCustomView;
        public OnBindView mOnBindView;
        public int mWindowAnimations = -1;
        public boolean isEnableHapticFeedback;
        public boolean isCreated;
        public boolean isCanceled;
        public boolean isCancelable = true;
        public boolean isCanceledOnTouchOutside = true;
        public boolean isAutoDismiss = true;
        public OnShowListener mOnShowListener;
        public OnCancelListener mOnCancelListener;
        public OnDismissListener mOnDismissListener;

        public MiuiAlertDialogBaseFactory(Dialog dialog) {
            mDialog = dialog;
            mWindow = mDialog.getWindow();
            mContext = mDialog.getContext();
            mPoint = MiuiXUtils.getWindowSize(mContext);
        }

        public abstract void init();

        @CallSuper
        protected void updateView() {
            updateText();
        }

        protected void loadView() {
            mTitleView = mMainDialogLayout.findViewById(R.id.dialog_title);
            mMessageView = mMainDialogLayout.findViewById(R.id.dialog_message);
            mButtonLayout = mMainDialogLayout.findViewById(R.id.dialog_button_view);
            mCustomLayout = mMainDialogLayout.findViewById(R.id.dialog_custom_view);
        }

        protected void hideAllViews() {
            mTitleView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
        }

        private void updateText() {
            if (mTitle != null) {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(mTitle);
            }
            if (mMessage != null) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(mMessage);
            }
        }

        protected void loadButtonView(@LayoutRes int id) {
            addView(mButtonLayout, id);
            mNegativeButton = mButtonLayout.findViewById(android.R.id.button1);
            mPositiveButton = mButtonLayout.findViewById(android.R.id.button2);
            mNeutralButton = mButtonLayout.findViewById(android.R.id.button3);
            mButtonHashMap.forEach((integer, pair) -> {
                switch (integer) {
                    case BUTTON_POSITIVE -> {
                        mPositiveButton.setVisibility(View.VISIBLE);
                        mPositiveButton.setText(pair.first);
                        mPositiveButton.setOnClickListener(createButtonClickAction(integer, pair.second));
                    }
                    case BUTTON_NEGATIVE -> {
                        mNegativeButton.setVisibility(View.VISIBLE);
                        mNegativeButton.setText(pair.first);
                        mNegativeButton.setOnClickListener(createButtonClickAction(integer, pair.second));
                    }
                    case BUTTON_NEUTRAL -> {
                        mNeutralButton.setVisibility(View.VISIBLE);
                        mNeutralButton.setText(pair.first);
                        mNeutralButton.setOnClickListener(createButtonClickAction(integer, pair.second));
                    }
                }
            });
            updateButtonLocation();
        }

        protected void updateButtonLocation() {
        }

        protected void loadEditTextView() {
            mWindow.setWindowAnimations(R.style.Animation_Dialog_ExistIme); // 存在键盘

            addView(mCustomLayout, new MiuiEditText(mContext));
            ConstraintLayout editLayout = mCustomLayout.findViewById(R.id.edit_layout);
            // 设置输入框的边距
            ConstraintLayout.LayoutParams editParams = (ConstraintLayout.LayoutParams) editLayout.getLayoutParams();
            editParams.setMarginStart(0);
            editParams.setMarginEnd(0);
            editLayout.setLayoutParams(editParams);

            mEditText = editLayout.findViewById(R.id.edit_text);
            mEditText.setText(mDefEditText);
            mEditText.setSelection(mDefEditText.length());
            mEditText.setHint(mEditTextHint);
            mEditText.setInputType(mEditTextInputType);
            if (mTextWatcher != null) mEditText.addTextChangedListener(mTextWatcher);

            if (mEditTextTip != "") {
                TextView editTip = editLayout.findViewById(R.id.edit_tip);
                editTip.setText(mEditTextTip);
                editTip.setVisibility(View.VISIBLE);
            }
            if (mEditTextImage != null) {
                ImageView editImage = editLayout.findViewById(R.id.edit_image);
                editImage.setImageDrawable(mEditTextImage);
                editImage.setVisibility(View.VISIBLE);
            }

            updateCustomLayoutBottomMarginIfNeed();
        }

        protected void loadListSelectView(int maxHeight) {
            if (mItems == null) {
                throw new RuntimeException("MiuiAlertDialog: Enable list select view, but items is null?? are you sure?");
            }
            mRecyclerView = new MiuiAlertDialogRecyclerView(mContext).getRecyclerView();
            mRecyclerView.setAdapter(mListAdapter = new MiuiAlertDialogRecyclerView.MiuiAlertDialogListAdapter(this));

            ViewGroup viewGroup = mRecyclerView;
            if (isEnableListSpringBack) {
                SpringBackLayout springBackLayout = (SpringBackLayout) (viewGroup = new SpringBackLayout(mContext));
                springBackLayout.setTarget(mRecyclerView);
                addView(springBackLayout, mRecyclerView);
            }

            addView(mCustomLayout, viewGroup);
            ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
            int height = (MiuiXUtils.dp2px(mContext, 58) * (mItems.size()));
            params.height = Math.min(height, maxHeight);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            viewGroup.setLayoutParams(params);

            updateCustomLayoutBottomMarginIfNeed();
        }

        protected void loadCustomView() {
            if (isExistEditTextView(mCustomLayout))
                mWindow.setWindowAnimations(R.style.Animation_Dialog_ExistIme);

            addView(mCustomLayout, mCustomView);
            if (mOnBindView != null)
                mOnBindView.onBindView(mCustomLayout, mCustomView);
            updateCustomLayoutBottomMarginIfNeed();
        }

        private boolean isExistEditTextView(ViewGroup customLayout) {
            for (int i = 0; i < customLayout.getChildCount(); i++) {
                View v = customLayout.getChildAt(i);
                if (v instanceof ViewGroup viewGroup) {
                    isExistEditTextView(viewGroup);
                }
                if (v instanceof EditText) {
                    return true;
                }
            }
            return false;
        }

        protected void updateCustomLayoutBottomMarginIfNeed() {
        }

        protected void addView(ViewGroup supperView, @LayoutRes int id) {
            addView(supperView, LayoutInflater.from(mContext).inflate(id, supperView, false));
        }

        protected void addView(ViewGroup supperView, View view) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != supperView) {
                if (viewGroup != null)
                    viewGroup.removeView(view);
                supperView.addView(view);
            }
        }

        protected View.OnClickListener createButtonClickAction(int id, DialogInterface.OnClickListener listener) {
            return v -> {
                if (isEnableHapticFeedback)
                    v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                if (id == BUTTON_POSITIVE) {
                    if (isEnableEditText && mTextWatcher != null)
                        mTextWatcher.onResult(this, mEditText.getText().toString());
                    if (isEnableListSelect && mItemsClickListener != null) {
                        ArrayList<CharSequence> result = new ArrayList<>();
                        for (int i = 0; i < mItems.size(); i++) {
                            if (mBooleanArray.get(i)) {
                                result.add(mItems.get(i));
                            }
                        }
                        mItemsClickListener.onResult(this, mItems, result);
                    }
                }
                if (listener != null)
                    listener.onClick(this, id);
                if (isAutoDismiss) dismiss();
            };
        }

        protected boolean isInputVisible(EditText editText) {
            if (editText == null) return false;
            if (editText.getRootWindowInsets() == null) return false;
            return editText.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
        }

        protected void showInputIfNeed() {
            if (!isEnableEditText) return;
            if (mEditText == null) return;
            mEditText.setFocusable(true);
            mEditText.setFocusableInTouchMode(true);
            mEditText.requestFocus();
            if (!isInputVisible(mEditText)) {
                WindowInsetsController windowInsetsController = mWindow.getDecorView().getWindowInsetsController();
                if (windowInsetsController != null)
                    windowInsetsController.show(WindowInsets.Type.ime());
                else {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditText, 0);
                }
            }
        }

        public boolean isShowing() {
            return mDialog.isShowing();
        }

        public void create() {
            if (isCreated) return;
            updateView();

            if (mWindowAnimations != -1)
                mWindow.setWindowAnimations(mWindowAnimations);
            mDialog.setOnShowListener(dialog -> {
                if (mEditTextAutoKeyboard)
                    showInputIfNeed();
                if (mOnShowListener != null)
                    mOnShowListener.onShow(this);
            });
            mDialog.setOnDismissListener(dialog -> {
                if (mOnDismissListener != null)
                    mOnDismissListener.onDismiss(this);
            });

            mDialog.setCancelable(isCancelable);
            mDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
            mDialog.create();
            isCreated = true;
        }

        public void show() {
            if (!isCreated) create();
            mDialog.show();
        }

        public void cancel() {
            if (!isCanceled) {
                if (mOnCancelListener != null)
                    mOnCancelListener.onCancel(this);
                isCanceled = true;
            }

            mDialog.dismiss();
        }

        public void dismiss() {
            if (isEnableEditText && mEditText != null) {
                if (mTextWatcher != null) {
                    mEditText.removeTextChangedListener(mTextWatcher);
                    mTextWatcher = null;
                }
            }
            mDialog.dismiss();
        }
    }

    private static class MiuiAlertDialogRecyclerView {
        private final RecyclerView mRecyclerView;

        private MiuiAlertDialogRecyclerView(Context context) {
            mRecyclerView = new RecyclerView(context);
            mRecyclerView.setId(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            mRecyclerView.setVerticalScrollBarEnabled(false);
            mRecyclerView.setHorizontalScrollBarEnabled(false);
        }

        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        protected static class MiuiAlertDialogListAdapter extends RecyclerView.Adapter<MiuiAlertDialogListAdapter.MiuiAlertDialogListViewHolder> {
            private final MiuiAlertDialogBaseFactory mBaseFactory;

            private MiuiAlertDialogListAdapter(MiuiAlertDialogBaseFactory baseFactory) {
                mBaseFactory = baseFactory;
            }

            @NonNull
            @Override
            public MiuiAlertDialogListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MiuiAlertDialogListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
            }

            @Override
            @SuppressLint("ClickableViewAccessibility")
            public void onBindViewHolder(@NonNull MiuiAlertDialogListViewHolder holder, @SuppressLint("RecyclerView") int position) {
                CharSequence title = mBaseFactory.mItems.get(position);
                holder.switchCompat.setText(title);
                boolean isChecked = mBaseFactory.mBooleanArray.get(position);
                holder.switchCompat.setHapticFeedbackEnabled(false);
                holder.switchCompat.setOnCheckedChangeListener(null);
                holder.switchCompat.setChecked(isChecked);
                updateSate(holder, position);

                holder.layout.setOnTouchListener((v, event) -> holder.switchCompat.onTouchEvent(event));
                holder.imageView.setOnTouchListener((v, event) -> holder.switchCompat.onTouchEvent(event));
                holder.switchCompat.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                    if (mBaseFactory.isEnableMultiSelect)
                        mBaseFactory.mBooleanArray.put(position, isChecked1);
                    updateSate(holder, position);
                    if (mBaseFactory.isEnableHapticFeedback)
                        holder.layout.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    if (mBaseFactory.mItemsClickListener != null)
                        mBaseFactory.mItemsClickListener.onClick(mBaseFactory, title, position);
                    if (!mBaseFactory.isEnableMultiSelect)
                        mBaseFactory.dismiss();
                });
            }

            private void updateSate(MiuiAlertDialogListViewHolder holder, int position) {
                if (mBaseFactory.mBooleanArray.get(position)) {
                    holder.switchCompat.setTextColor(mBaseFactory.mContext.getColor(R.color.list_choose_text));
                    holder.layout.setBackgroundResource(R.drawable.list_choose_item_background);
                    holder.imageView.setVisibility(View.VISIBLE);
                } else {
                    holder.layout.setBackgroundResource(R.drawable.list_item_background);
                    holder.switchCompat.setTextColor(mBaseFactory.mContext.getColor(R.color.list_text));
                    holder.imageView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public int getItemCount() {
                return mBaseFactory.mItems.size();
            }

            private static class MiuiAlertDialogListViewHolder extends RecyclerView.ViewHolder {
                ConstraintLayout layout;
                SwitchCompat switchCompat;
                ImageView imageView;

                public MiuiAlertDialogListViewHolder(@NonNull View itemView) {
                    super(itemView);
                    layout = (ConstraintLayout) itemView;
                    switchCompat = itemView.findViewById(R.id.list_item);
                    imageView = itemView.findViewById(R.id.list_image);
                }
            }
        }
    }

    public static class MiuiSwitchCompat extends SwitchCompat {

        public MiuiSwitchCompat(@NonNull Context context) {
            super(context);
        }

        public MiuiSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MiuiSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean isFocused() {
            return true;
        }
    }
}
