package com.hchen.himiuix;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MiuiDropDownPreference extends MiuiPreference {
    private MiuiAlertDialog dialog;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence mDefValue;
    private String mKey;
    private String mValue;
    private View view;
    private final ArrayList<CharSequence> mEntriesList = new ArrayList<>();
    private final SparseBooleanArray booleanArray = new SparseBooleanArray();

    public MiuiDropDownPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint({"RestrictedApi", "PrivateResource"})
    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.miuix_preference);
        this.context = context;
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiDropDownPreference, defStyleAttr, defStyleRes)) {
            mEntries = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entries,
                    R.styleable.MiuiDropDownPreference_android_entries);
            mEntryValues = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entryValues,
                    R.styleable.MiuiDropDownPreference_android_entryValues);
            mDefValue = TypedArrayUtils.getString(array, R.styleable.MiuiDropDownPreference_defaultValue,
                    R.styleable.MiuiDropDownPreference_android_defaultValue);
            mKey = TypedArrayUtils.getString(array, R.styleable.MiuiDropDownPreference_key,
                    R.styleable.MiuiDropDownPreference_android_key);
        }
        mEntriesList.addAll(Arrays.asList(mEntries));
        safeCheck();
        setPersistent(true);
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public CharSequence getDefValue() {
        return mDefValue;
    }

    public void setDefValue(CharSequence def) {
        mDefValue = def;
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
        mEntriesList.clear();
        mEntriesList.addAll(Arrays.asList(entries));
        notifyChanged();
    }

    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(context.getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
        notifyChanged();
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(context.getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public void setValue(String value) {
        if (value == null) return;
        if (!value.equals(mValue)) {
            persistString(value);
            makeBooleanArray(value);
            mValue = value;
            notifyChanged();
        }
    }

    public String getValue() {
        return mValue;
    }

    @Nullable
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }

    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(mEntryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
            if (dialog != null && dialog.listAdapter != null)
                dialog.listAdapter.booleanArray = booleanArray;
            notifyChanged();
        }
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    protected void notifyChanged() {
        super.notifyChanged();
        if (mEntryValues.length != mEntries.length) return;
        if (dialog != null && dialog.listAdapter != null) {
            dialog.listAdapter.booleanArray = booleanArray;
            dialog.listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        // 不存在条目时调用用于初始化
        if (!Objects.requireNonNull(getSharedPreferences()).contains(mKey)) {
            if (mDefValue != null) {
                setValue((String) mDefValue);
            }
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (isPersistent())
            return parcelable;

        final SavedState savedState = new SavedState(parcelable);
        savedState.mValue = mValue;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(((SavedState) state).getSuperState());
        setValue(savedState.mValue);
    }

    private void makeBooleanArray(@NonNull String mValue) {
        booleanArray.clear();
        safeCheck();
        for (int i = 0; i < mEntryValues.length; i++) {
            if (mEntryValues[i].equals(mValue)) {
                booleanArray.put(i, true);
                break;
            }
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEnabled()) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            v.setBackgroundColor(Color.argb(255, 0xEB, 0xEB, 0xEB));
        } else if (action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundColor(Color.argb(255, 0xFF, 0xFF, 0xFF));
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            safeCheck();
            if (dialog != null && dialog.isShowing()) return false;
            v.setBackgroundColor(Color.argb(255, 0xEB, 0xEB, 0xEB));
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            view = v;
            float x = event.getRawX();
            float y = event.getRawY();
            showDialogAtPosition(x, y);
        }
        return false;
    }

    int screenHeight;
    int screenWidth;
    int viewX;
    int viewY;
    int viewWidth;
    int viewHeight;
    int dialogHeight;
    int showX;
    int showY;
    boolean showRight;

    private void showDialogAtPosition(float x, float y) {
        screenHeight = MiuiXUtils.getScreenSize(context).y;
        screenWidth = MiuiXUtils.getScreenSize(context).x;

        int[] location = new int[2];
        mainLayout.getLocationOnScreen(location);
        viewX = location[0];
        viewY = location[1];
        viewWidth = mainLayout.getWidth();
        viewHeight = mainLayout.getHeight();

        dialogHeight = calculateHeight();
        int spaceBelow = screenHeight - (viewY + viewHeight);
        boolean showBelow = (spaceBelow - dialogHeight) > screenHeight / 8;
        showRight = x > ((float) (viewX + viewWidth) / 2);

        showX = MiuiXUtils.dp2px(context, 25);
        showY = showBelow ? viewY + MiuiXUtils.sp2px(context, 5) : viewY - dialogHeight - MiuiXUtils.sp2px(context, 30);

        initDialog();
        calculateLayout();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setBackgroundColor(Color.argb(255, 0xFF, 0xFF, 0xFF));
            }
        });
        dialog.show();
    }

    private void calculateLayout() {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        window.setGravity(Gravity.TOP | (showRight ? Gravity.RIGHT : Gravity.LEFT));
        params.x = showX;
        params.y = showY;
        params.width = calculateWidth();
        params.height = dialogHeight;
        window.setAttributes(params);

        dialog.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) dialog.recyclerView.getLayoutParams();
                layoutParams.width = calculateWidth();
                layoutParams.height = dialogHeight;
                dialog.recyclerView.setLayoutParams(layoutParams);
            }
        });
    }

    private int calculateWidth() {
        Point point = MiuiXUtils.getScreenSize(context);
        return MiuiXUtils.isVerticalScreen(context) ? (int) (point.x / 2.1) : (int) (point.x / 3.4);
    }

    private int calculateHeight() {
        if (mEntryValues != null) {
            int count = mEntryValues.length;
            int height = (MiuiXUtils.sp2px(context, 56) * (count)) + MiuiXUtils.sp2px(context, 20);
            int maxHeight = MiuiXUtils.isVerticalScreen(context) ? MiuiXUtils.getScreenSize(context).y / 3 : (int) (MiuiXUtils.getScreenSize(context).y / 2.1);
            return Math.min(height, maxHeight);
        } else return WRAP_CONTENT;
    }

    private void initDialog() {
        dialog = new MiuiAlertDialog(context);
        dialog.alertTitle.setVisibility(View.GONE); // 隐藏标题
        dialog.message.setVisibility(View.GONE); // 隐藏信息
        dialog.buttonView.setVisibility(View.GONE); // 隐藏按钮布局
        dialog.endView.setVisibility(View.GONE); // 隐藏垫底 view
        dialog.isDropDown = true;
        dialog.setHapticFeedbackEnabled(true);
        dialog.setWindowAnimations(R.style.Animation_Dialog_Center);
        dialog.setCornersRadius(MiuiXUtils.dp2px(context, 20));
        dialog.listAdapter.booleanArray = booleanArray;
        dialog.setItems(mEntriesList, new DialogInterface.OnItemsChangeListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, CharSequence item, int which) {
                if (dialog.listAdapter.booleanArray.get(which)) {
                    return;
                }
                setValue(String.valueOf(which));
            }
        });
    }

    private void safeCheck() {
        if (mEntries.length != mEntryValues.length) {
            throw new RuntimeException("MiuiDropDownPreference: The length of entries must be equal to the length of entryValues!");
        }
        if (!mEntryValues[0].equals("0")) {
            throw new RuntimeException("MiuiDropDownPreference: EntryValues must start from scratch!");
        }
        for (int i = 0; i < mEntryValues.length; i++) {
            if (!mEntryValues[i].equals(Integer.toString(i))) {
                throw new RuntimeException("MiuiDropDownPreference: The entryValues must be continuous!");
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        String mValue;

        public SavedState(Parcel source) {
            super(source);
            mValue = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mValue);
        }
    }
}