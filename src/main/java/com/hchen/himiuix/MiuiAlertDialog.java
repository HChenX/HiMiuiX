package com.hchen.himiuix;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
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

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiAlertDialog implements DialogInterface {
    private static final String TAG = "MiuiPreference";
    private ConstraintLayout mainDialog;
    TextView alertTitle;
    TextView message;
    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;
    LinearLayout buttonView;
    View endView;
    RecyclerView recyclerView;
    private EditText editText;
    private TextView editTextTip;
    private ImageView editImage;
    private ConstraintLayout editLayout;
    boolean isDropDown = false;
    private GradientDrawable customRadius;
    private float radius = -1;
    private int inputType;
    private View customView;
    private int customViewId;
    private ConstraintLayout customLayout;
    private CustomViewCallBack viewCallBack;
    private final ArrayList<EditText> editTexts = new ArrayList<>();
    private static final int ID_POSITIVE_BUTTON = 0;
    private static final int ID_NEGATIVE_BUTTON = 1;
    private static final int ID_NEUTRAL_BUTTON = 2;
    private boolean isSetPositiveButton;
    private boolean isSetNegativeButton;
    private boolean isSetNeutralButton;
    private boolean isMultiSelect = false;
    private boolean dismissNow;
    private ArrayList<CharSequence> items = new ArrayList<>();
    final ListAdapter listAdapter;
    private final Context context;
    private final Dialog dialog;
    private final Window window;
    private boolean shouldShowEdit = false;
    private boolean hapticFeedbackEnabled;
    private TextWatcher textWatcher;
    private boolean needInput;
    private boolean isCreated;
    private OnItemsChangeListener itemsChangeListener;

    public MiuiAlertDialog(@NonNull Context context) {
        this(context, 0);
    }

    public MiuiAlertDialog(@NonNull Context context, @StyleRes int themeResId) {
        this.context = context;
        initView();
        if (themeResId == 0)
            themeResId = R.style.MiuiAlertDialog;

        dialog = new Dialog(context, themeResId) {
            @Override
            public void dismiss() {
                if (!isShowing()) return;
                if (itemsChangeListener != null) {
                    ArrayList<CharSequence> result = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        if (listAdapter.booleanArray.get(i)) {
                            result.add(items.get(i));
                        }
                    }
                    itemsChangeListener.onResult(result, items, listAdapter.booleanArray);
                }
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
                super.dismiss();
            }
        };

        window = dialog.getWindow();
        assert window != null;
        window.setContentView(mainDialog);
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        Point windowPoint = MiuiXUtils.getScreenSize(context);
        params.verticalMargin = (MiuiXUtils.sp2px(context, 16) * 1.0f) / windowPoint.y;
        params.width = MiuiXUtils.isVerticalScreen(context) ? (int) (windowPoint.x / 1.08) : (int) (windowPoint.x / 2.0);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setWindowAnimations(R.style.Animation_Dialog);

        listAdapter = new ListAdapter(this);
        if (context instanceof Activity activity) {
            activity.registerActivityLifecycleCallbacks(new ActivityLifecycle(this));
        }
    }

    private void initView() {
        mainDialog = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.miuix_dialog, null);
        alertTitle = mainDialog.findViewById(R.id.alertTitle);
        message = mainDialog.findViewById(android.R.id.message);
        buttonView = mainDialog.findViewById(R.id.button_view);
        recyclerView = mainDialog.findViewById(R.id.list_view);
        positiveButton = mainDialog.findViewById(android.R.id.button2);
        negativeButton = mainDialog.findViewById(android.R.id.button1);
        neutralButton = mainDialog.findViewById(android.R.id.button3);
        endView = mainDialog.findViewById(R.id.end_view);
        customLayout = mainDialog.findViewById(R.id.dialog_custom);
        editText = mainDialog.findViewById(R.id.edit_text_id);
        editLayout = mainDialog.findViewById(R.id.edit_layout);
        editTextTip = mainDialog.findViewById(R.id.edit_tip);
        editImage = mainDialog.findViewById(R.id.edit_image);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editLayout.setBackgroundResource(R.drawable.focused_border_input_box);
                } else
                    editLayout.setBackgroundResource(R.drawable.nofocused_border_input_box);
            }
        });
        customLayout.setVisibility(View.GONE);
        editImage.setVisibility(View.GONE);
        editTextTip.setVisibility(View.GONE);
        editLayout.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        alertTitle.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        neutralButton.setVisibility(View.GONE);
    }

    private View.OnClickListener customClickAction(int id, DialogInterface.OnClickListener listener) {
        return v -> {
            if (hapticFeedbackEnabled)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            if (id == ID_POSITIVE_BUTTON && textWatcher != null) {
                textWatcher.onResult(editText.getText().toString());
            }
            if (listener != null) listener.onClick(this, id);
            if (dialog.isShowing())
                dismiss();
        };
    }

    public Window getWindow() {
        return window;
    }

    public MiuiAlertDialog setWindowAnimations(@StyleRes int resId) {
        window.setWindowAnimations(resId);
        return this;
    }

    public MiuiAlertDialog setCornersRadius(int radius) {
        customRadius = new GradientDrawable();
        customRadius.setColor(context.getColor(R.color.white_or_black));
        customRadius.setShape(GradientDrawable.RECTANGLE);
        customRadius.setCornerRadius(radius);
        this.radius = radius;
        return this;
    }

    public MiuiAlertDialog setCustomView(View view, CustomViewCallBack viewCallBack) {
        this.viewCallBack = viewCallBack;
        customView = view;
        customViewId = 0;
        return this;
    }

    public MiuiAlertDialog setCustomView(int viewId, CustomViewCallBack viewCallBack) {
        this.viewCallBack = viewCallBack;
        customViewId = viewId;
        customView = null;
        return this;
    }

    public View getCustomView() {
        if (customView != null)
            return customView;
        else
            return LayoutInflater.from(context).inflate(customViewId, customLayout, false);
    }

    public CustomViewCallBack getCustomViewCallBack() {
        return viewCallBack;
    }

    public MiuiAlertDialog setItems(@ArrayRes int items, OnItemsChangeListener listener) {
        return setItems(context.getResources().getTextArray(items), listener);
    }

    public MiuiAlertDialog setItems(CharSequence[] items, OnItemsChangeListener listener) {
        ArrayList<CharSequence> list = new ArrayList<>(Arrays.asList(items));
        return setItems(list, listener);
    }

    public MiuiAlertDialog setItems(ArrayList<CharSequence> items, OnItemsChangeListener listener) {
        this.items = items;
        listAdapter.update(listener);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(listAdapter);
        itemsChangeListener = listener;
        return this;
    }

    public ArrayList<CharSequence> getItems() {
        return items;
    }

    public MiuiAlertDialog isMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        return this;
    }

    public MiuiAlertDialog setTitle(@StringRes int titleResId) {
        return setTitle(context.getResources().getText(titleResId));
    }

    public MiuiAlertDialog setTitle(CharSequence title) {
        alertTitle.setText(title);
        alertTitle.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setTitleSize(float size) {
        alertTitle.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setMessage(@StringRes int messageResId) {
        return setMessage(context.getResources().getText(messageResId));
    }

    public MiuiAlertDialog setMessage(CharSequence message) {
        this.message.setText(message);
        this.message.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setMessageSize(float size) {
        alertTitle.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setPositiveButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        positiveButton.setText(text);
        positiveButton.setOnClickListener(customClickAction(ID_POSITIVE_BUTTON, listener));
        isSetPositiveButton = true;
        return this;
    }

    public MiuiAlertDialog setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNegativeButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        negativeButton.setText(text);
        negativeButton.setOnClickListener(customClickAction(ID_NEGATIVE_BUTTON, listener));
        isSetNegativeButton = true;
        return this;
    }

    public MiuiAlertDialog setNeutralButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNeutralButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        neutralButton.setText(text);
        neutralButton.setVisibility(View.VISIBLE);
        neutralButton.setOnClickListener(customClickAction(ID_NEUTRAL_BUTTON, listener));
        isSetNeutralButton = true;
        return this;
    }

    public MiuiAlertDialog setContentView(@LayoutRes int layoutResID) {
        window.setContentView(layoutResID);
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
            editText.setText(defText);
            editText.setSelection(editText.getText().length());
        }
        this.needInput = needInput;
        if (watcher != null)
            editText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    watcher.beforeTextChanged(s, start, count, after);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    watcher.onTextChanged(s, start, before, count);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    watcher.afterTextChanged(s);
                }
            });
        textWatcher = watcher;
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setInputType(int type) {
        inputType = type;
        editText.setInputType(inputType);
        return this;
    }

    public int getInputType() {
        return inputType;
    }

    public MiuiAlertDialog setEditHint(CharSequence text) {
        editText.setHint(text);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditHint(@StringRes int textResId) {
        editText.setHint(textResId);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(CharSequence textTip) {
        editTextTip.setVisibility(View.VISIBLE);
        editTextTip.setText(textTip);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(@StringRes int textTipResId) {
        return setEditTextTip(context.getText(textTipResId));
    }

    public MiuiAlertDialog setEditTextImage(Drawable drawable) {
        editImage.setVisibility(View.VISIBLE);
        editImage.setImageDrawable(drawable);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextImage(@DrawableRes int drawable) {
        return setEditTextImage(AppCompatResources.getDrawable(context, drawable));
    }

    public MiuiAlertDialog setHapticFeedbackEnabled(boolean enabled) {
        hapticFeedbackEnabled = enabled;
        return this;
    }

    public MiuiAlertDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public MiuiAlertDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public Context getContext() {
        return context;
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public MiuiAlertDialog create() {
        if (isCreated) return this;
        if (isSetNeutralButton) {
            buttonView.setOrientation(LinearLayout.VERTICAL);
            margin(negativeButton, MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 25), 0, 0);
            margin(neutralButton, MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 10), 0);
            margin(positiveButton, MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 10), 0);
        } else {
            if (!isSetPositiveButton && !isSetNegativeButton)
                dialog.setCancelable(true); // 防止无法关闭 dialog
            if (!isSetPositiveButton || !isSetNegativeButton) {
                margin(positiveButton, MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 25), 0, 0);
                margin(negativeButton, MiuiXUtils.sp2px(context, 25), MiuiXUtils.sp2px(context, 25), 0, 0);
            }
        }
        if (customView != null || customViewId != 0) {
            setupCustomContent();
        } else {
            if (shouldShowEdit) {
                editLayout.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE); // 不支持同时显示文本输入框和多选菜单 (至少是我不想写
                recyclerView.setAdapter(null);
            } else {
                RecyclerViewCornerRadius cornerRadius = new RecyclerViewCornerRadius(recyclerView);
                float radius = (this.radius == -1) ? MiuiXUtils.sp2px(context, 32) : this.radius;
                cornerRadius.setCornerRadius(radius);
                if (items != null && !isDropDown) {
                    cornerRadius.setCornerRadius(0);
                    ConstraintLayout.LayoutParams layout = (ConstraintLayout.LayoutParams) recyclerView.getLayoutParams();
                    int height = (MiuiXUtils.sp2px(context, 56) * (items.size())) + MiuiXUtils.sp2px(context, 20);
                    int maxHeight = MiuiXUtils.isVerticalScreen(context) ? MiuiXUtils.getScreenSize(context).y / 3 : (int) (MiuiXUtils.getScreenSize(context).y / 2.5);
                    layout.height = Math.min(height, maxHeight);
                    recyclerView.setLayoutParams(layout);

                    if (isSetPositiveButton || isSetNegativeButton || isSetNeutralButton) {
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) buttonView.getLayoutParams();
                        layoutParams.topMargin = MiuiXUtils.sp2px(context, 20);
                        buttonView.setLayoutParams(layoutParams);
                    } else {
                        endView.setVisibility(View.GONE);
                        buttonView.setVisibility(View.GONE);
                        cornerRadius.setCornerRadius(-1, -1, radius, radius);
                    }

                    if (alertTitle.getVisibility() == View.GONE && message.getVisibility() == View.GONE) {
                        cornerRadius.setCornerRadius(radius, radius, -1, -1);
                    }
                }
                recyclerView.addItemDecoration(cornerRadius);
            }
        }
        if (!isSetNeutralButton) neutralButton.setVisibility(View.GONE);
        if (!isSetNegativeButton) negativeButton.setVisibility(View.GONE);
        if (!isSetPositiveButton) positiveButton.setVisibility(View.GONE);
        if (customRadius != null) mainDialog.setBackground(customRadius);
        if (message.getVisibility() == View.VISIBLE && alertTitle.getVisibility() == View.GONE) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) message.getLayoutParams();
            params.topMargin = MiuiXUtils.sp2px(context, 25);
            message.setLayoutParams(params);
        }
        dialog.create();
        isCreated = true;
        return this;
    }

    public void show() {
        if (!isCreated) create();
        dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
            @Override
            public void onShow(android.content.DialogInterface d) {
                if (needInput)
                    showInputIfNeed();
            }
        });
        dialog.show();
    }

    public MiuiAlertDialog setOnDismissListener(OnDismissListener dismissListener) {
        dialog.setOnDismissListener(dialog -> dismissListener.onDismiss(MiuiAlertDialog.this));
        return this;
    }

    public void dismiss() {
        dialog.dismiss();
    }

    protected void dismissNow() {
        dismissNow = true;
        dialog.dismiss();
    }

    private void setupCustomContent() {
        View view = customView != null ? customView :
                (customViewId != 0 ? LayoutInflater.from(context).inflate(customViewId, customLayout, false) : null);
        if (view == null) return;
        ViewGroup viewParent = (ViewGroup) view.getParent();
        if (viewParent != customLayout) {
            if (viewParent != null)
                viewParent.removeView(view);
            customLayout.addView(view);
        }
        customLayout.setVisibility(View.VISIBLE);

        checkChildAddEditText(customLayout);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) customLayout.getLayoutParams();
        params.setMarginStart(MiuiXUtils.sp2px(context, 25));
        params.setMarginEnd(MiuiXUtils.sp2px(context, 25));
        if (isSetNegativeButton || isSetPositiveButton || isSetNeutralButton)
            params.bottomMargin = MiuiXUtils.sp2px(context, 25);
        params.topMargin = MiuiXUtils.sp2px(context, 25);
        customLayout.setLayoutParams(params);

        if (viewCallBack != null)
            viewCallBack.onCustomViewCreate(view);

        editLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(null);
    }

    private void checkChildAddEditText(ViewGroup customLayout) {
        for (int i = 0; i < customLayout.getChildCount(); i++) {
            View v = customLayout.getChildAt(i);
            if (v instanceof ViewGroup viewGroup) {
                checkChildAddEditText(viewGroup);
            }
            if (v instanceof EditText) {
                editTexts.add((EditText) v);
            }
        }
    }

    private void hideInputIfNeed(EditText editText, Runnable runnable) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible(editText)) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0,
                    new ResultReceiver(new Handler(context.getMainLooper())) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            new Handler(context.getMainLooper()).postDelayed(runnable, 300);
                        }
                    });
        }
    }

    private void hideInputNow(EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible(editText)) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private @Nullable EditText getVisibleEditText() {
        EditText edit = null;
        if (editLayout.getVisibility() == View.VISIBLE) {
            if (isInputVisible(editText))
                edit = editText;
        } else {
            for (EditText e : editTexts) {
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
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        if (!isInputVisible(editText)) {
            WindowInsetsController windowInsetsController = window.getDecorView().getWindowInsetsController();
            if (windowInsetsController != null)
                windowInsetsController.show(WindowInsets.Type.ime());
            else {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, 0);
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

    public interface CustomViewCallBack {
        void onCustomViewCreate(View view);
    }

    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
        private final MiuiAlertDialog dialog;
        SparseBooleanArray booleanArray = new SparseBooleanArray();
        private OnItemsChangeListener listener;
        private GradientDrawable drawableTop;
        private GradientDrawable drawableBottom;
        private boolean isChecked;

        protected ListAdapter(MiuiAlertDialog dialog) {
            this.dialog = dialog;
        }

        private void setDrawableColor(@ColorInt int color) {
            drawableTop = new GradientDrawable();
            drawableBottom = new GradientDrawable();
            drawableTop.setColor(color);
            drawableBottom.setColor(color);
            float radius = (dialog.radius == -1) ? MiuiXUtils.sp2px(dialog.context, 32) : dialog.radius;
            if (isChecked || dialog.isSetNegativeButton || dialog.isSetPositiveButton || dialog.isSetNeutralButton
                    || dialog.alertTitle.getVisibility() == View.VISIBLE || dialog.message.getVisibility() == View.VISIBLE) {
                isChecked = true;
                if (!dialog.isDropDown) radius = 0;
            }
            if (dialog.alertTitle.getVisibility() == View.GONE && dialog.message.getVisibility() == View.GONE && !dialog.isDropDown) {
                float TopRadius = (dialog.radius == -1) ? MiuiXUtils.sp2px(dialog.context, 32) : dialog.radius;
                drawableTop.setCornerRadii(new float[]{TopRadius, TopRadius, TopRadius, TopRadius, 0, 0, 0, 0});
            } else
                drawableTop.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});
            drawableBottom.setCornerRadii(new float[]{0, 0, 0, 0, radius, radius, radius, radius});
        }

        public void update(OnItemsChangeListener listener) {
            this.listener = listener;
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
            CharSequence s = dialog.items.get(position);
            setFirstOrEndView(holder, position);
            holder.switchView.setText(s);
            boolean isChecked = booleanArray.get(position);
            checkState(holder, position);

            holder.switchView.setOnCheckedChangeListener(null);
            holder.switchView.setOnHoverListener(null);
            holder.switchView.setChecked(isChecked);

            holder.switchView.setOnCheckedChangeListener((b, i) -> {
                if (dialog.isMultiSelect) booleanArray.put(position, i);
                checkState(holder, position);
                if (dialog.hapticFeedbackEnabled)
                    b.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                if (listener != null) listener.onClick(dialog, s, position);
                if (!dialog.isMultiSelect)
                    dialog.dismiss();
            });
            holder.switchView.setOnHoverListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE -> {
                        holder.mainLayout.setBackgroundResource(R.color.touch_down);
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
            if (booleanArray.get(position)) {
                setDrawableColor(dialog.context.getColor(R.color.list_state_background));
                if (position == 0) holder.mainLayout.setBackground(drawableTop);
                else if (position == dialog.items.size() - 1) {
                    holder.mainLayout.setBackground(drawableBottom);
                } else
                    holder.mainLayout.setBackgroundResource(R.drawable.list_choose_item_background);
                holder.switchView.setTextColor(dialog.context.getColor(R.color.list_choose_text));
                holder.imageView.setVisibility(View.VISIBLE);
            } else {
                setDrawableColor(dialog.context.getColor(R.color.list_background));
                if (position == 0) holder.mainLayout.setBackground(drawableTop);
                else if (position == dialog.items.size() - 1) {
                    holder.mainLayout.setBackground(drawableBottom);
                } else holder.mainLayout.setBackgroundResource(R.drawable.list_item_background);
                holder.switchView.setTextColor(dialog.context.getColor(R.color.list_text));
                holder.imageView.setVisibility(View.GONE);
            }
        }

        private void setFirstOrEndView(@NonNull ListViewHolder holder, int position) {
            if (position == 0 || position == dialog.items.size() - 1) {
                holder.firstView.setVisibility(View.VISIBLE);
                holder.endView.setVisibility(View.VISIBLE);
            } else {
                holder.firstView.setVisibility(View.GONE);
                holder.endView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return dialog.items.size();
        }

        public static class ListViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout mainLayout;
            View firstView;
            View endView;
            SwitchCompat switchView;
            ImageView imageView;

            public ListViewHolder(@NonNull View itemView) {
                super(itemView);
                mainLayout = (ConstraintLayout) itemView;
                firstView = itemView.findViewById(R.id.first_view);
                endView = itemView.findViewById(R.id.end_view);
                switchView = itemView.findViewById(R.id.list_item);
                imageView = itemView.findViewById(R.id.list_image);
                imageView.setVisibility(View.GONE);
            }
        }
    }

    /** @noinspection ClassCanBeRecord */
    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private final MiuiAlertDialog dialog;

        public ActivityLifecycle(MiuiAlertDialog dialog) {
            this.dialog = dialog;
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
            if (dialog.isShowing()) {
                dialog.dismissNow();
            }
        }
    }
}
