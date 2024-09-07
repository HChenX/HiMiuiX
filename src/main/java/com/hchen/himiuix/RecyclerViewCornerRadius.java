package com.hchen.himiuix;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewCornerRadius extends RecyclerView.ItemDecoration {
    public static final String TAG = "RecyclerViewCornerRadius";

    private RectF rectF;
    private Path path;

    private float topLeftRadius = 0;
    private float topRightRadius = 0;
    private float bottomLeftRadius = 0;
    private float bottomRightRadius = 0;

    public RecyclerViewCornerRadius(final RecyclerView recyclerView) {
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rectF = new RectF(0, 0, recyclerView.getMeasuredWidth(), recyclerView.getMeasuredHeight());

                path = new Path();
                path.reset();
                path.addRoundRect(rectF, new float[]{
                        topLeftRadius, topLeftRadius,
                        topRightRadius, topRightRadius,
                        bottomLeftRadius, bottomLeftRadius,
                        bottomRightRadius, bottomRightRadius
                }, Path.Direction.CCW);

                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void setCornerRadius(float radius) {
        this.topLeftRadius = radius;
        this.topRightRadius = radius;
        this.bottomLeftRadius = radius;
        this.bottomRightRadius = radius;
    }

    public void setCornerRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        this.topLeftRadius = defOrChange(this.topLeftRadius, topLeftRadius);
        this.topRightRadius = defOrChange(this.topRightRadius, topRightRadius);
        this.bottomLeftRadius = defOrChange(this.bottomLeftRadius, bottomLeftRadius);
        this.bottomRightRadius = defOrChange(this.bottomRightRadius, bottomRightRadius);
    }

    private float defOrChange(float def, float change) {
        if (change == -1) return def;
        return change;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        c.clipRect(rectF);
        c.clipPath(path, Region.Op.INTERSECT);
    }
}
