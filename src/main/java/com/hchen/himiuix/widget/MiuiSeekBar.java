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
package com.hchen.himiuix.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.hchen.himiuix.MiuiXUtils;
import com.hchen.himiuix.R;

@SuppressLint("AppCompatCustomView")
public class MiuiSeekBar extends SeekBar {
    private String TAG = "MiuiPreference";
    private Paint mPaint;
    private boolean shouldStep = false;
    private int mDefValue = -1;
    private int mDefStep = -1;
    private boolean showDefaultPoint;

    public MiuiSeekBar(Context context) {
        this(context, null);
    }

    public MiuiSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.MiuiSeekBar);
    }

    public MiuiSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MiuiSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getContext().getColor(R.color.seekbar_def));
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showDefaultPoint) {
            int width = getWidth();
            int height = getHeight() + MiuiXUtils.dp2px(getContext(), 5); // seekbar padding 量

            float scaleWidth = (float) width / (getMax() - getMin());
            float xPosition = scaleWidth * (shouldStep ? (mDefStep) : (mDefValue - getMin()));
            float yPosition = (float) height / 2;

            canvas.drawCircle(xPosition, yPosition, (float) height / 6, mPaint);
        }
    }

    public void setDefValue(int defValue) {
        this.mDefValue = defValue;
    }

    public void setShouldStep(boolean shouldStep) {
        this.shouldStep = shouldStep;
    }

    public void setDefStep(int defStep) {
        this.mDefStep = defStep;
    }

    public void setShowDefaultPoint(boolean show) {
        this.showDefaultPoint = show;
    }
}
