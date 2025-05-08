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

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.hchen.himiuix.R;

public class ColorSelectView extends View {
    private Context context;
    private int colorValue = -1;
    private boolean isGrayedOut = false;
    private final Paint paint = new Paint();

    public ColorSelectView(Context context) {
        this(context, null);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        colorValue = this.context.getColor(R.color.white_or_black);
    }

    @ColorInt
    public int getColorValue() {
        return colorValue;
    }

    public void setColorValue(@ColorInt int colorValue) {
        this.colorValue = colorValue;
        invalidate();
    }

    public void setGrayedOut(boolean isGrayedOut) {
        this.isGrayedOut = isGrayedOut;
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        paint.reset();
        float width = (float) getWidth();
        float radius = width / 2;
        paint.setColor(context.getColor(android.R.color.transparent));
        canvas.drawCircle(radius, radius, radius, paint);
        paint.reset();
        SweepGradient sweepGradient = new SweepGradient(radius, radius, context.getResources().getIntArray(R.array.gradual_color), null);
        Matrix matrix = new Matrix();
        matrix.preRotate(90, radius, radius);
        sweepGradient.setLocalMatrix(matrix);
        paint.setShader(sweepGradient);
        canvas.drawCircle(radius, radius, radius - 0.2F, paint);
        paint.reset();
        paint.setColor(context.getColor(R.color.white_or_black));
        canvas.drawCircle(radius, radius, radius - width / 9.0F, paint);
        paint.setColor(colorValue);
        canvas.drawCircle(radius, radius, radius - width / 5.0F, paint);

        if (isGrayedOut) { // 变灰
            paint.reset();
            paint.setColor(Color.argb(99, 255, 255, 255));
            canvas.drawCircle(radius, radius, radius, paint);
        }
        super.onDraw(canvas);
    }
}
