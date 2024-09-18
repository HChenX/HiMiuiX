package com.hchen.himiuix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class MiuiSeekBar extends SeekBar {
    private String TAG = "MiuiPreference";
    private Paint paint;
    private boolean shouldStep = false;
    private int defValue = -1;
    private int defStep = -1;
    private boolean showDefaultPoint;

    public MiuiSeekBar(Context context) {
        super(context);
        init();
    }

    public MiuiSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiuiSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MiuiSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getContext().getColor(R.color.seekbar_def));
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showDefaultPoint) {
            int width = getWidth() - MiuiXUtils.sp2px(getContext(), 50);
            int height = getHeight() - MiuiXUtils.sp2px(getContext(), 20);

            float scaleWidth = (float) width / (getMax() - getMin());
            float xPosition = scaleWidth * (shouldStep ? (defStep) : (defValue - getMin()))
                    + MiuiXUtils.sp2px(getContext(), 25);
            float yPosition = (float) height / 2;
            
            canvas.drawCircle(xPosition, yPosition, (float) height / 5, paint);
        }
    }

    public void setDefValue(int defValue) {
        this.defValue = defValue;
    }

    public void setShouldStep(boolean shouldStep) {
        this.shouldStep = shouldStep;
    }

    public void setDefStep(int defStep) {
        this.defStep = defStep;
    }

    public void setShowDefaultPoint(boolean show) {
        this.showDefaultPoint = show;
    }
}
