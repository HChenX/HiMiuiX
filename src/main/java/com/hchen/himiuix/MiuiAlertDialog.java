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
import android.text.Editable;
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
    private ConstraintLayout mainDialog;
    TextView alertTitleView;
    TextView messageView;
    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;
    LinearLayout buttonView;
    View endView;
    RecyclerView recyclerView;
    private EditText editTextView;
    private TextView editTextTipView;
    private ImageView editImageView;
    private ConstraintLayout editLayout;
    boolean isDropDown = false;
    private GradientDrawable customRadius;
    private float radius = -1;
    private int inputType;
    private View customView;
    private int customViewId;
    private ConstraintLayout customLayout;
    private OnBindView onBindView;
    private final ArrayList<EditText> editTextViews = new ArrayList<>();
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
    private final HashMap<TypefaceObject, Typeface> typefaceHashMap = new HashMap<>();
    private final HashMap<TypefaceObject, Pair<Typeface, Integer>> typefaceStyleHashMap = new HashMap<>();

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
        this.context = context;
        typefaceHashMap.clear();
        typefaceStyleHashMap.clear();
        initView();
        if (themeResId == 0)
            themeResId = R.style.MiuiAlertDialog;

        dialog = new Dialog(context, themeResId) {
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
                if (weakReference != null) {
                    weakReference.clear();
                    weakReference = null;
                }
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
        alertTitleView = mainDialog.findViewById(R.id.alertTitle);
        messageView = mainDialog.findViewById(android.R.id.message);
        buttonView = mainDialog.findViewById(R.id.button_view);
        recyclerView = mainDialog.findViewById(R.id.list_view);
        positiveButton = mainDialog.findViewById(android.R.id.button2);
        negativeButton = mainDialog.findViewById(android.R.id.button1);
        neutralButton = mainDialog.findViewById(android.R.id.button3);
        endView = mainDialog.findViewById(R.id.end_view);
        customLayout = mainDialog.findViewById(R.id.dialog_custom);
        editTextView = mainDialog.findViewById(R.id.edit_text_id);
        editLayout = mainDialog.findViewById(R.id.edit_layout);
        editTextTipView = mainDialog.findViewById(R.id.edit_tip);
        editImageView = mainDialog.findViewById(R.id.edit_image);
        customLayout.setVisibility(View.GONE);
        editImageView.setVisibility(View.GONE);
        editTextTipView.setVisibility(View.GONE);
        editLayout.setVisibility(View.GONE);
        editTextView.setVisibility(View.GONE);
        alertTitleView.setVisibility(View.GONE);
        messageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        neutralButton.setVisibility(View.GONE);
        editTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editLayout.setBackgroundResource(R.drawable.focused_border_input_box);
                } else
                    editLayout.setBackgroundResource(R.drawable.nofocused_border_input_box);
            }
        });
    }

    private View.OnClickListener makeButtonClickAction(int id, DialogInterface.OnClickListener listener) {
        return v -> {
            if (hapticFeedbackEnabled)
                v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            if (id == ID_POSITIVE_BUTTON) {
                if (textWatcher != null)
                    textWatcher.onResult(editTextView.getText().toString());
                if (itemsChangeListener != null) {
                    ArrayList<CharSequence> result = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        if (listAdapter.booleanArray.get(i)) {
                            result.add(items.get(i));
                        }
                    }
                    itemsChangeListener.onResult(result, items, listAdapter.booleanArray);
                }
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

    public MiuiAlertDialog setCustomView(View view, OnBindView onBindView) {
        this.onBindView = onBindView;
        customView = view;
        customViewId = 0;
        return this;
    }

    public MiuiAlertDialog setCustomView(int viewId, OnBindView onBindView) {
        this.onBindView = onBindView;
        customViewId = viewId;
        customView = null;
        return this;
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

    public MiuiAlertDialog isMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        return this;
    }

    public MiuiAlertDialog setTitle(@StringRes int titleResId) {
        return setTitle(context.getResources().getText(titleResId));
    }

    public MiuiAlertDialog setTitle(CharSequence title) {
        alertTitleView.setText(title);
        alertTitleView.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setTitleSize(float size) {
        alertTitleView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setTextTypeface(MakeTypeface makeTypeface) {
        makeTypeface.onMakeTypeface(typefaceHashMap);
        makeTypeface.onMakeTypefaceStyle(typefaceStyleHashMap);
        return this;
    }

    public MiuiAlertDialog setMessage(@StringRes int messageResId) {
        return setMessage(context.getResources().getText(messageResId));
    }

    public MiuiAlertDialog setMessage(CharSequence message) {
        this.messageView.setText(message);
        this.messageView.setVisibility(View.VISIBLE);
        return this;
    }

    public MiuiAlertDialog setMessageSize(float size) {
        messageView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setPositiveButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        positiveButton.setText(text);
        positiveButton.setOnClickListener(makeButtonClickAction(ID_POSITIVE_BUTTON, listener));
        isSetPositiveButton = true;
        return this;
    }

    public MiuiAlertDialog setPositiveButtonTextSize(float size) {
        positiveButton.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNegativeButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        negativeButton.setText(text);
        negativeButton.setOnClickListener(makeButtonClickAction(ID_NEGATIVE_BUTTON, listener));
        isSetNegativeButton = true;
        return this;
    }

    public MiuiAlertDialog setNegativeButtonTextSize(float size) {
        negativeButton.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setNeutralButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
        return setNeutralButton(context.getResources().getText(textId), listener);
    }

    public MiuiAlertDialog setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        neutralButton.setText(text);
        neutralButton.setVisibility(View.VISIBLE);
        neutralButton.setOnClickListener(makeButtonClickAction(ID_NEUTRAL_BUTTON, listener));
        isSetNeutralButton = true;
        return this;
    }

    public MiuiAlertDialog setNeutralButtonTextSize(float size) {
        neutralButton.setTextSize(size);
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
            editTextView.setText(defText);
            editTextView.setSelection(editTextView.getText().length());
        }
        this.needInput = needInput;
        if (watcher != null)
            editTextView.addTextChangedListener(new android.text.TextWatcher() {
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
    
    public MiuiAlertDialog setEditTextSize(float size) {
        editTextView.setTextSize(size);
        return this;
    }
    
    public MiuiAlertDialog setInputType(int type) {
        inputType = type;
        editTextView.setInputType(inputType);
        return this;
    }

    public MiuiAlertDialog setEditHint(CharSequence text) {
        editTextView.setHint(text);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditHint(@StringRes int textResId) {
        editTextView.setHint(textResId);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(CharSequence textTip) {
        editTextTipView.setVisibility(View.VISIBLE);
        editTextTipView.setText(textTip);
        shouldShowEdit = true;
        return this;
    }

    public MiuiAlertDialog setEditTextTip(@StringRes int textTipResId) {
        return setEditTextTip(context.getText(textTipResId));
    }

    public MiuiAlertDialog setEditTextTipSize(float size) {
        editTextTipView.setTextSize(size);
        return this;
    }

    public MiuiAlertDialog setEditTextImage(Drawable drawable) {
        editImageView.setVisibility(View.VISIBLE);
        editImageView.setImageDrawable(drawable);
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
                editTextView.setVisibility(View.VISIBLE);
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

                    if (alertTitleView.getVisibility() == View.GONE && messageView.getVisibility() == View.GONE) {
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
        if (messageView.getVisibility() == View.VISIBLE && alertTitleView.getVisibility() == View.GONE) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageView.getLayoutParams();
            params.topMargin = MiuiXUtils.sp2px(context, 25);
            messageView.setLayoutParams(params);
        }
        setTextTypeface();
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

    public void cancel() {
        dialog.cancel();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void dismissNow() {
        dismissNow = true;
        dialog.dismiss();
    }

    private void setTextTypeface() {
        if (typefaceHashMap.isEmpty()) {
            if (typefaceStyleHashMap.isEmpty())
                return;
            else {
                typefaceStyleHashMap.forEach((typefaceObject, typefaceIntegerPair) ->
                        setTypeface(typefaceObject, typefaceIntegerPair.first, typefaceIntegerPair.second));
            }
        } else {
            typefaceHashMap.forEach((typefaceObject, typeface) ->
                    setTypeface(typefaceObject, typeface, NORMAL));
        }
    }

    private void setTypeface(TypefaceObject typefaceObject, Typeface typeface, int style) {
        switch (typefaceObject) {
            case TYPEFACE_ALERT_TITLE -> {
                alertTitleView.setTypeface(typeface, style);
            }
            case TYPEFACE_MESSAGE -> {
                messageView.setTypeface(typeface, style);
            }
            case TYPEFACE_POSITIVE_TEXT -> {
                positiveButton.setTypeface(typeface, style);
            }
            case TYPEFACE_NEGATIVE_TEXT -> {
                negativeButton.setTypeface(typeface, style);
            }
            case TYPEFACE_NEUTRAL_TEXT -> {
                neutralButton.setTypeface(typeface, style);
            }
            case TYPEFACE_EDIT -> {
                editTextView.setTypeface(typeface, style);
            }
            case TYPEFACE_EDIT_TIP -> {
                editTextTipView.setTypeface(typeface, style);
            }
        }
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

        if (onBindView != null)
            onBindView.onBindView(view);

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
                editTextViews.add((EditText) v);
            }
        }
    }

    private WeakReference<ResultReceiver> weakReference = null;

    private void hideInputIfNeed(EditText editText, Runnable runnable) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isInputVisible(editText)) {
            if (weakReference != null && weakReference.get() == null) {
                weakReference.clear();
                weakReference = null;
            }
            if (weakReference == null) {
                weakReference = new WeakReference<>(new ResultReceiver(new Handler(context.getMainLooper())) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        new Handler(context.getMainLooper()).postDelayed(runnable, 300);
                    }
                });
            }
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0, weakReference.get());
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
            if (isInputVisible(editTextView))
                edit = editTextView;
        } else {
            for (EditText e : editTextViews) {
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
        editTextView.setFocusable(true);
        editTextView.setFocusableInTouchMode(true);
        editTextView.requestFocus();
        if (!isInputVisible(editTextView)) {
            WindowInsetsController windowInsetsController = window.getDecorView().getWindowInsetsController();
            if (windowInsetsController != null)
                windowInsetsController.show(WindowInsets.Type.ime());
            else {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextView, 0);
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
                    || dialog.alertTitleView.getVisibility() == View.VISIBLE || dialog.messageView.getVisibility() == View.VISIBLE) {
                isChecked = true;
                if (!dialog.isDropDown) radius = 0;
            }
            if (dialog.alertTitleView.getVisibility() == View.GONE && dialog.messageView.getVisibility() == View.GONE && !dialog.isDropDown) {
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
