package com.hchen.himiuix;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

public class MiuiDropDownPreference extends MiuiPreference {
    private MiuiAlertDialog mDialog;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence mDefValue;
    private boolean shouldShowOnSummary;
    private String mValue;
    private View mTouchView;
    private boolean isInitialTime = true;
    private final ArrayList<CharSequence> mEntriesList = new ArrayList<>();
    private final SparseBooleanArray mBooleanArray = new SparseBooleanArray();

    public MiuiDropDownPreference(@NonNull Context context) {
        this(context, null);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.MiuiPreference);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        safeCheck();
        mEntriesList.addAll(Arrays.asList(mEntries));
        setDefaultValue(mDefValue);
    }

    @Override
    @SuppressLint({"RestrictedApi", "PrivateResource"})
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
        try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MiuiDropDownPreference, defStyleAttr, defStyleRes)) {
            mEntries = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entries,
                    R.styleable.MiuiDropDownPreference_android_entries);
            mEntryValues = TypedArrayUtils.getTextArray(array, R.styleable.MiuiDropDownPreference_entryValues,
                    R.styleable.MiuiDropDownPreference_android_entryValues);
            mDefValue = TypedArrayUtils.getString(array, R.styleable.MiuiDropDownPreference_defaultValue,
                    R.styleable.MiuiDropDownPreference_android_defaultValue);
            shouldShowOnSummary = array.getBoolean(R.styleable.MiuiDropDownPreference_showOnSummary, false);
        }
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
        mEntriesList.clear();
        mEntriesList.addAll(Arrays.asList(entries));
        notifyChanged();
    }

    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
        notifyChanged();
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public void setValue(String value) {
        if (value == null) return;
        valueCheck(value);
        safeCheck();
        if (!value.equals(mValue)) {
            if (callChangeListener(value) || isInitialTime) {
                persistString(value);
                makeBooleanArray(value);
                mValue = value;
                if (shouldShowOnSummary)
                    setSummary(mEntriesList.get(Integer.parseInt(mValue)));
                if (!isInitialTime) {
                    notifyChanged();
                }
            }
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
    
    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
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
    
    @Override
    @SuppressLint("NotifyDataSetChanged")
    protected void notifyChanged() {
        super.notifyChanged();
        if (mEntryValues.length != mEntries.length) return;
        if (mDialog != null && mDialog.mListAdapter != null) {
            mDialog.mListAdapter.mBooleanArray = mBooleanArray;
            mDialog.mListAdapter.notifyDataSetChanged();
        }
    }

    private void makeBooleanArray(@NonNull String mValue) {
        mBooleanArray.clear();
        for (int i = 0; i < mEntryValues.length; i++) {
            if (mEntryValues[i].equals(mValue)) {
                mBooleanArray.put(i, true);
                break;
            }
        }
    }

    // 安全检查
    private void safeCheck() {
        if (mEntries.length != mEntryValues.length) { // 元素数与索引数不相等
            throw new RuntimeException("MiuiDropDownPreference: The length of entries must be equal to the length of entryValues!");
        }
        if (!mEntryValues[0].equals("0")) { // 不是从零开始的索引
            throw new RuntimeException("MiuiDropDownPreference: EntryValues must start from scratch!");
        }
        for (int i = 0; i < mEntryValues.length; i++) { // 索引必须是连续的数字
            if (!mEntryValues[i].equals(Integer.toString(i))) {
                throw new RuntimeException("MiuiDropDownPreference: The entryValues must be continuous!");
            }
        }
    }

    private void valueCheck(String value) {
        for (CharSequence mEntryValue : mEntryValues) {
            if (mEntryValue.equals(value)) {
                return;
            }
        }
        throw new RuntimeException("MiuiDropDownPreference: The input value is not an existing set of available values! " +
                "Input: " + value + " Available values: " + Arrays.toString(mEntryValues));
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        getArrowRightView().setVisibility(View.VISIBLE);
        if (isEnabled())
            getArrowRightView().setImageDrawable(
                    AppCompatResources.getDrawable(getContext(), R.drawable.ic_preference_arrow_up_down));
        else
            getArrowRightView().setImageDrawable(
                    AppCompatResources.getDrawable(getContext(), R.drawable.ic_preference_disable_arrow_up_down));
    }

    @Override
    protected boolean disableArrowRightView() {
        return true;
    }

    @Override
    protected boolean shouldShowSummary() {
        return getSummary() != null || shouldShowOnSummary;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
        isInitialTime = false;
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEnabled()) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.touch_down);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundResource(R.color.touch_up);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            safeCheck();
            if (mDialog != null && mDialog.isShowing()) return false;
            v.setBackgroundResource(R.color.touch_down);
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            mTouchView = v;
            float x = event.getRawX();
            float y = event.getRawY();
            showDialogAtPosition(x, y);
        }
        return false;
    }

    private int mDialogHeight;
    private int mShowX;
    private int mShowY;
    private boolean shouldShowRight;

    private void showDialogAtPosition(float x, float y) {
        int screenHeight = MiuiXUtils.getScreenSize(getContext()).y;

        int[] location = new int[2];
        getMiuiPrefMainLayout().getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        int viewWidth = getMiuiPrefMainLayout().getWidth();
        int viewHeight = getMiuiPrefMainLayout().getHeight();

        mDialogHeight = calculateHeight();
        int spaceBelow = screenHeight - (viewY + viewHeight);
        boolean showBelow = (spaceBelow - mDialogHeight) > screenHeight / 8;
        shouldShowRight = x > ((float) (viewX + viewWidth) / 2);

        mShowX = MiuiXUtils.dp2px(getContext(), 25);
        mShowY = showBelow ? viewY + MiuiXUtils.sp2px(getContext(), 5) : viewY - mDialogHeight - MiuiXUtils.sp2px(getContext(), 30);

        initDialog();
        calculateLayout();
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mTouchView.setBackgroundResource(R.color.touch_up);
            }
        });
        mDialog.show();
    }

    private void calculateLayout() {
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        window.setGravity(Gravity.TOP | (shouldShowRight ? Gravity.RIGHT : Gravity.LEFT));
        params.x = mShowX;
        params.y = mShowY;
        params.width = calculateWidth();
        params.height = mDialogHeight;
        window.setAttributes(params);

        mDialog.mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mDialog.mRecyclerView.getLayoutParams();
                layoutParams.width = calculateWidth();
                layoutParams.height = mDialogHeight;
                mDialog.mRecyclerView.setLayoutParams(layoutParams);
            }
        });
    }

    private int calculateWidth() {
        Point point = MiuiXUtils.getScreenSize(getContext());
        return MiuiXUtils.isVerticalScreen(getContext()) ? (int) (point.x / 2.1) : (int) (point.x / 3.4);
    }

    private int calculateHeight() {
        if (mEntryValues != null) {
            int count = mEntryValues.length;
            int height = (MiuiXUtils.sp2px(getContext(), 56) * (count)) + MiuiXUtils.sp2px(getContext(), 20);
            int maxHeight = MiuiXUtils.isVerticalScreen(getContext()) ? MiuiXUtils.getScreenSize(getContext()).y / 3 : (int) (MiuiXUtils.getScreenSize(getContext()).y / 2.1);
            return Math.min(height, maxHeight);
        } else return WRAP_CONTENT;
    }

    private void initDialog() {
        mDialog = new MiuiAlertDialog(getContext());
        mDialog.isDropDown = true;
        mDialog.mTitleView.setVisibility(View.GONE); // 隐藏标题
        mDialog.mMessageView.setVisibility(View.GONE); // 隐藏信息
        mDialog.mButtonLayout.setVisibility(View.GONE); // 隐藏按钮布局
        mDialog.mEndView.setVisibility(View.GONE); // 隐藏垫底 view
        mDialog.setHapticFeedbackEnabled(true);
        mDialog.setWindowAnimations(R.style.Animation_Dialog_Center);
        mDialog.setCornersRadius(MiuiXUtils.dp2px(getContext(), 20));
        mDialog.mListAdapter.mBooleanArray = mBooleanArray;
        mDialog.setItems(mEntriesList, new DialogInterface.OnItemsChangeListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, CharSequence item, int which) {
                if (mDialog.mListAdapter.mBooleanArray.get(which)) {
                    return;
                }
                setValue(String.valueOf(which));
            }
        });
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
