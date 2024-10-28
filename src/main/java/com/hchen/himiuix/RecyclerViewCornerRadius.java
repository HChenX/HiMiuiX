/*
 * This file is part of HiMiuiX.
 *
 * HiMiuiX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * HiMiuiX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2024 HiMiuiX Contributions
 */
package com.hchen.himiuix;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewCornerRadius extends RecyclerView.ItemDecoration {
    public static final String TAG = "MiuiPreference";

    private final RectF rectF = new RectF();
    private final Path path = new Path();

    private float topLeftRadius = 0;
    private float topRightRadius = 0;
    private float bottomLeftRadius = 0;
    private float bottomRightRadius = 0;

    protected RecyclerViewCornerRadius(final RecyclerView recyclerView) {
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePath(recyclerView);
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void updatePath(RecyclerView recyclerView) {
        rectF.set(0, 0, recyclerView.getMeasuredWidth(), recyclerView.getMeasuredHeight());
        path.reset();
        path.addRoundRect(rectF, new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomLeftRadius, bottomLeftRadius,
                bottomRightRadius, bottomRightRadius
        }, Path.Direction.CCW);
    }

    public void setCornerRadius(float radius) {
        setCornerRadius(radius, radius, radius, radius);
    }

    public void setCornerRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        this.topLeftRadius = defOrChange(this.topLeftRadius, topLeftRadius);
        this.topRightRadius = defOrChange(this.topRightRadius, topRightRadius);
        this.bottomLeftRadius = defOrChange(this.bottomLeftRadius, bottomLeftRadius);
        this.bottomRightRadius = defOrChange(this.bottomRightRadius, bottomRightRadius);
    }

    private float defOrChange(float def, float change) {
        return change == -1 ? def : change;
    }

    @Override
    public void onDraw(Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        c.clipRect(rectF);
        c.clipPath(path);
    }
}
