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
package com.hchen.himiuix.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

public class ColorSelectView extends View {
    private boolean mIsGrayedOut = false;
    private int mColor = -1;
    private Context mContext;
    private final Paint mPaint = new Paint();

    public ColorSelectView(Context context) {
        super(context);
        init(context);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mColor = mContext.getColor(R.color.white_or_black);
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    public void setGrayedOut(boolean isGrayedOut) {
        mIsGrayedOut = isGrayedOut;
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        mPaint.reset();
        float width = (float) getWidth();
        float radius = width / 2;
        mPaint.setColor(mContext.getColor(android.R.color.transparent));
        canvas.drawCircle(radius, radius, radius, mPaint);
        mPaint.reset();
        SweepGradient sweepGradient = new SweepGradient(radius, radius, mContext.getResources().getIntArray(R.array.gradual_color), null);
        Matrix matrix = new Matrix();
        matrix.preRotate(90, radius, radius);
        sweepGradient.setLocalMatrix(matrix);
        mPaint.setShader(sweepGradient);
        canvas.drawCircle(radius, radius, radius - 0.2F, mPaint);
        mPaint.reset();
        mPaint.setColor(mContext.getColor(R.color.white_or_black));
        canvas.drawCircle(radius, radius, radius - width / 9.0F, mPaint);
        mPaint.setColor(mColor);
        canvas.drawCircle(radius, radius, radius - width / 5.0F, mPaint);

        if (mIsGrayedOut) { // 变灰
            mPaint.reset();
            mPaint.setColor(Color.argb(99, 255, 255, 255));
            canvas.drawCircle(radius, radius, radius, mPaint);
        }
        super.onDraw(canvas);
    }
}
