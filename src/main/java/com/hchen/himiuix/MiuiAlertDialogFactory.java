/*
 * This file is part of HiMiuiX.

 * HiMiuiX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.InputType;
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

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.himiuix.springback.SpringBackLayout;
import com.hchen.himiuix.widget.MiuiCheckBox;
import com.hchen.himiuix.widget.MiuiEditText;

import java.util.ArrayList;
import java.util.HashMap;

class MiuiAlertDialogFactory {
    private final static String TAG = "MiuiPreference";
    private final Context mContext;
    private final int mThemeResId;
    private final boolean mEnableDropDownMode;

    MiuiAlertDialogFactory(Context context, @StyleRes int themeResId, boolean enableDropDownMode) {
        mContext = context;
        mThemeResId = themeResId;
        mEnableDropDownMode = enableDropDownMode;
    }

    MiuiAlertDialogBaseFactory init() {
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
        private MiuiAlertDialogVerticalFactory(Dialog dialog) {
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
                params.verticalMargin = (float) MiuiXUtils.dp2px(mContext, 15) / mPoint.y; // 距离底部的百分比
                params.width = (int) (mPoint.x / 1.065); // 距离屏幕左右间隔
                params.height = WindowManager.LayoutParams.WRAP_CONTENT; // 自适应
                mWindow.setAttributes(params);
            }
            mWindow.setWindowAnimations(R.style.Animation_Dialog); // 弹出动画

            super.init();
        }

        @Override
        void updateView() {
            super.updateView();
            if (isUsePositiveButton && isUseNegativeButton && isUseNeutralButton)
                loadButtonView(true, R.layout.miuix_vertical_button); // 垂直布局
            else
                loadButtonView(false, R.layout.miuix_horizontal_button); // 水平布局

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
        void updateButtonLocation(boolean vertical) {
            int px5 = MiuiXUtils.dp2px(mContext, 5);
            LinearLayout.LayoutParams positiveParams = (LinearLayout.LayoutParams) mPositiveButton.getLayoutParams();
            LinearLayout.LayoutParams negativeParams = (LinearLayout.LayoutParams) mNegativeButton.getLayoutParams();
            LinearLayout.LayoutParams neutralParams = (LinearLayout.LayoutParams) mNeutralButton.getLayoutParams();
            if (vertical) {
                neutralParams.topMargin = MiuiXUtils.dp2px(mContext, 10);
                neutralParams.bottomMargin = MiuiXUtils.dp2px(mContext, 10);
            } else {
                if (isUsePositiveButton && isUseNegativeButton && !isUseNeutralButton) {
                    positiveParams.setMarginStart(px5);
                    negativeParams.setMarginEnd(px5);
                } else if (!isUsePositiveButton && isUseNegativeButton && isUseNeutralButton) {
                    negativeParams.setMarginEnd(px5);
                    neutralParams.setMarginStart(px5);
                } else if (isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                    positiveParams.setMarginStart(px5);
                    neutralParams.setMarginEnd(px5);
                }
            }

            mPositiveButton.setLayoutParams(positiveParams);
            mNegativeButton.setLayoutParams(negativeParams);
            mNeutralButton.setLayoutParams(neutralParams);
        }

        @Override
        void updateCustomLayoutBottomMarginIfNeed() {
            ConstraintLayout.LayoutParams customParams = (ConstraintLayout.LayoutParams) mCustomLayout.getLayoutParams();
            customParams.bottomMargin = MiuiXUtils.dp2px(mContext, 25);
            mCustomLayout.setLayoutParams(customParams);
        }
    }

    private static class MiuiAlertDialogHorizontalFactory extends MiuiAlertDialogBaseFactory {
        private MiuiAlertDialogHorizontalFactory(Dialog dialog) {
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

            super.init();
        }

        @Override
        void updateView() {
            super.updateView();
            loadButtonView(true, R.layout.miuix_vertical_button);

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
        void updateButtonLocation(boolean vertical) {
            int px5 = MiuiXUtils.dp2px(mContext, 10);
            LinearLayout.LayoutParams positiveParams = (LinearLayout.LayoutParams) mPositiveButton.getLayoutParams();
            LinearLayout.LayoutParams negativeParams = (LinearLayout.LayoutParams) mNegativeButton.getLayoutParams();
            LinearLayout.LayoutParams neutralParams = (LinearLayout.LayoutParams) mNeutralButton.getLayoutParams();

            if (isUsePositiveButton && isUseNegativeButton && !isUseNeutralButton) {
                negativeParams.bottomMargin = px5;
                positiveParams.topMargin = px5;
            } else if (!isUsePositiveButton && isUseNegativeButton && isUseNeutralButton) {
                negativeParams.bottomMargin = px5;
                neutralParams.topMargin = px5;
            } else if (isUsePositiveButton && !isUseNegativeButton && isUseNeutralButton) {
                neutralParams.bottomMargin = px5;
                positiveParams.topMargin = px5;
            } else if (isUsePositiveButton && isUseNegativeButton && isUseNeutralButton) {
                neutralParams.topMargin = px5;
                neutralParams.bottomMargin = px5;
            }

            mPositiveButton.setLayoutParams(positiveParams);
            mNegativeButton.setLayoutParams(negativeParams);
            mNeutralButton.setLayoutParams(neutralParams);
        }

        @Override
        void updateCustomLayoutBottomMarginIfNeed() {
            super.updateCustomLayoutBottomMarginIfNeed();
        }
    }

    static class MiuiAlertDialogDropDownFactory extends MiuiAlertDialogBaseFactory {
        private View mRootView;
        private boolean isVerticalScreen;

        private MiuiAlertDialogDropDownFactory(Dialog dialog) {
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
        void updateView() {
            mRecyclerView = new MiuiAlertDialogRecyclerView(mContext).getRecyclerView();
            addView(mMainDialogLayout, mRecyclerView);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mRecyclerView.setAdapter(mListAdapter = new MiuiAlertDialogRecyclerView.MiuiAlertDialogListAdapter(this));

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRecyclerView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mRecyclerView.setLayoutParams(params);

            RecyclerViewCornerRadius cornerRadius = new RecyclerViewCornerRadius(mRecyclerView);
            cornerRadius.setCornerRadius(MiuiXUtils.dp2px(mContext, 18)); // 选项圆角
            mRecyclerView.addItemDecoration(cornerRadius);
        }

        void setRootPreferenceView(View rootView) {
            mRootView = rootView;
        }

        void showDialogByTouchPosition(float x, float y) {
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
                int width = sequence.length() * MiuiXUtils.sp2px(mContext, 17);
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

    static abstract class MiuiAlertDialogBaseFactory implements DialogInterface {
        private final Dialog mDialog;
        Window mWindow;
        Point mPoint;
        Context mContext;
        ConstraintLayout mMainDialogLayout;
        private TextView mTitleView;
        private TextView mMessageView;
        CharSequence mTitle;
        CharSequence mMessage;
        ConstraintLayout mCustomLayout;
        private LinearLayout mButtonLayout;
        HashMap<Integer, Pair<CharSequence, OnClickListener>> mButtonHashMap = new HashMap<>();
        Button mNegativeButton;
        Button mPositiveButton;
        Button mNeutralButton;
        boolean isUsePositiveButton;
        boolean isUseNegativeButton;
        boolean isUseNeutralButton;
        boolean isEnableEditText;
        private EditText mEditText;
        CharSequence mDefEditText = "";
        CharSequence mEditTextHint = "";
        CharSequence mEditTextTip = "";
        Drawable mEditTextImage;
        boolean mEditTextAutoKeyboard;
        int mEditTextInputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        DialogInterface.TextWatcher mTextWatcher;
        boolean isEnableListSelect;
        ArrayList<CharSequence> mItems;
        RecyclerView mRecyclerView;
        MiuiAlertDialogRecyclerView.MiuiAlertDialogListAdapter mListAdapter;
        SparseBooleanArray mBooleanArray = new SparseBooleanArray();
        DialogInterface.OnItemsClickListener mItemsClickListener;
        boolean isEnableListSpringBack;
        boolean isEnableMultiSelect;
        boolean isEnableCustomView;
        View mCustomView;
        OnBindView mOnBindView;
        int mWindowAnimations = -1;
        boolean isEnableHapticFeedback;
        private boolean isCreated;
        private boolean isCanceled;
        boolean isCancelable = true;
        boolean isCanceledOnTouchOutside = true;
        boolean isAutoDismiss = true;
        OnShowListener mOnShowListener;
        OnCancelListener mOnCancelListener;
        OnDismissListener mOnDismissListener;

        private MiuiAlertDialogBaseFactory(Dialog dialog) {
            mDialog = dialog;
            mWindow = mDialog.getWindow();
            mContext = mDialog.getContext();
            mPoint = MiuiXUtils.getWindowSize(mContext);
        }

        public void init() {
            mTitleView = mMainDialogLayout.findViewById(R.id.dialog_title);
            mMessageView = mMainDialogLayout.findViewById(R.id.dialog_message);
            mCustomLayout = mMainDialogLayout.findViewById(R.id.dialog_custom_view);
            mButtonLayout = mMainDialogLayout.findViewById(R.id.dialog_button_view);

            mTitleView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
        }

        void updateView() {
            if (mTitle != null) {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(mTitle);
            }
            if (mMessage != null) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(mMessage);
            }
        }

        void loadButtonView(boolean vertical, @LayoutRes int id) {
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
            updateButtonLocation(vertical);
        }

        void updateButtonLocation(boolean vertical) {
        }

        void loadEditTextView() {
            mWindow.setWindowAnimations(R.style.Animation_Dialog_ExistIme); // 存在键盘

            addView(mCustomLayout, new MiuiEditText(mContext));
            ConstraintLayout editLayout = mCustomLayout.findViewById(R.id.edit_layout);
            mEditText = editLayout.findViewById(R.id.edit_text);
            mEditText.setText(mDefEditText);
            mEditText.setSelection(mDefEditText.length());
            mEditText.setHint(mEditTextHint);
            mEditText.setInputType(mEditTextInputType);
            if (mTextWatcher != null)
                mEditText.addTextChangedListener(mTextWatcher);

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

        void loadListSelectView(int maxHeight) {
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

        void loadCustomView() {
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

        void updateCustomLayoutBottomMarginIfNeed() {
        }

        private void addView(ViewGroup supperView, @LayoutRes int id) {
            addView(supperView, LayoutInflater.from(mContext).inflate(id, supperView, false));
        }

        void addView(ViewGroup supperView, View view) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != supperView) {
                if (viewGroup != null)
                    viewGroup.removeView(view);
                supperView.addView(view);
            }
        }

        private View.OnClickListener createButtonClickAction(int id, DialogInterface.OnClickListener listener) {
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

        private boolean isInputVisible(EditText editText) {
            if (editText == null) return false;
            if (editText.getRootWindowInsets() == null) return false;
            return editText.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
        }

        private void showInputIfNeed() {
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

        boolean isShowing() {
            return mDialog.isShowing();
        }

        void create() {
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

        void show() {
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

        RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        static class MiuiAlertDialogListAdapter extends RecyclerView.Adapter<MiuiAlertDialogListAdapter.MiuiAlertDialogListViewHolder> {
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
                holder.mTextView.setText(title);
                boolean isChecked = mBaseFactory.mBooleanArray.get(position);
                holder.mMiuiCheckBox.setOnCheckedChangeListener(null);
                holder.mMiuiCheckBox.setChecked(isChecked);
                holder.mLayout.setOnTouchListener(null);
                holder.mTextView.setOnTouchListener(null);
                updateSate(holder, position);

                if (holder.mMiuiCheckBox.isEnabled()) {
                    holder.mMiuiCheckBox.setClickable(true);
                    holder.mLayout.setOnTouchListener((v, event) -> holder.mMiuiCheckBox.onTouchEvent(event));
                    holder.mTextView.setOnTouchListener((v, event) -> holder.mMiuiCheckBox.onTouchEvent(event));

                    holder.mMiuiCheckBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                        if (mBaseFactory.isEnableMultiSelect)
                            mBaseFactory.mBooleanArray.put(position, isChecked1);

                        updateSate(holder, position);

                        if (mBaseFactory.isEnableHapticFeedback)
                            holder.mLayout.performHapticFeedback(HapticFeedbackConstants.CONFIRM);

                        if (mBaseFactory.mItemsClickListener != null)
                            mBaseFactory.mItemsClickListener.onClick(mBaseFactory, title, position);

                        if (!mBaseFactory.isEnableMultiSelect)
                            mBaseFactory.dismiss();
                    });
                }
            }

            private void updateSate(MiuiAlertDialogListViewHolder holder, int position) {
                if (mBaseFactory.mBooleanArray.get(position)) {
                    holder.mTextView.setTextColor(mBaseFactory.mContext.getColor(R.color.list_choose_text));
                    holder.mLayout.setBackgroundResource(R.drawable.list_choose_item_background);
                } else {
                    holder.mLayout.setBackgroundResource(R.drawable.list_item_background);
                    holder.mTextView.setTextColor(mBaseFactory.mContext.getColor(R.color.list_text));
                }
            }

            @Override
            public int getItemCount() {
                return mBaseFactory.mItems.size();
            }

            private static class MiuiAlertDialogListViewHolder extends RecyclerView.ViewHolder {
                ConstraintLayout mLayout;
                TextView mTextView;
                MiuiCheckBox mMiuiCheckBox;

                private MiuiAlertDialogListViewHolder(@NonNull View itemView) {
                    super(itemView);
                    mLayout = (ConstraintLayout) itemView;
                    mTextView = itemView.findViewById(R.id.list_item);
                    mMiuiCheckBox = itemView.findViewById(R.id.list_image);
                }
            }
        }
    }
}
