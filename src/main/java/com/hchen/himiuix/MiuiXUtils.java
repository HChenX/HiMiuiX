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
package com.hchen.himiuix;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MiuiXUtils {
    private static final Point windowSizePoint = new Point();
    private static final Point screenSizePoint = new Point();

    // --------------- 获取窗口参数 --------------
    public static WindowManager getWindowManager(Context context) {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static Display getDisplay(Context context) {
        try {
            return context.getDisplay();
        } catch (UnsupportedOperationException unused) {
            // Log.w("WindowUtils", "This context is not associated with a display. You should use createDisplayContext() to create a display context to work with windows.");
            return getWindowManager(context).getDefaultDisplay();
        }
    }

    public static void getWindowSize(Context context, Point point) {
        getWindowSize(getWindowManager(context), point);
    }

    public static void getWindowSize(WindowManager windowManager, Point point) {
        Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
        point.x = bounds.width();
        point.y = bounds.height();
    }

    public static Point getWindowSize(Context context) {
        getWindowSize(context, windowSizePoint);
        return windowSizePoint;
    }

    @Deprecated
    public static int getWindowHeight(Context context) {
        return getWindowSize(context).y;
    }

    public static void getScreenSize(Context context, Point point) {
        getScreenSize(getWindowManager(context), point);
    }

    public static Point getScreenSize(Context context) {
        getScreenSize(getWindowManager(context), screenSizePoint);
        return screenSizePoint;
    }

    public static void getScreenSize(WindowManager windowManager, Point point) {
        Rect bounds = windowManager.getMaximumWindowMetrics().getBounds();
        point.x = bounds.width();
        point.y = bounds.height();
    }

    public static boolean isFreeformMode(Configuration configuration, Point point, Point point2) {
        return configuration.toString().contains("mWindowingMode=freeform") &&
            ((((float) point2.x) + 0.0f) / ((float) point.x) <= 0.9f ||
                (((float) point2.y) + 0.0f) / ((float) point.y) <= 0.9f);
    }

    public static boolean isHorizontalScreen(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isVerticalScreen(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isPad(Context context) {
        int flag = 0;
        if (PadDeviceCheckHelper.isPadByProp()) ++flag;
        if (PadDeviceCheckHelper.isPadBySize(context)) ++flag;
        if (PadDeviceCheckHelper.isPadByApi(context)) ++flag;
        return flag >= 2;
    }

    // -------------- 转换 ------------------
    public static int px2dp(Context context, float pxValue) {
        // 获取屏幕密度（每英寸多少个像素点）
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        // 获取字体的缩放密度
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        // 获取屏幕密度
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        // 获取字体的缩放密度
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    // 获取 Drawable
    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        return AppCompatResources.getDrawable(context, id);
    }

    public static Drawable[] getDrawables(Context context, @DrawableRes int... ids) {
        return Arrays.stream(ids).mapToObj(value -> AppCompatResources.getDrawable(context, value))
            .collect(Collectors.toCollection(ArrayList::new))
            .toArray(new Drawable[]{});
    }

    private static class PadDeviceCheckHelper {
        public static boolean isPadByProp() {
            String mDeviceType = SystemPropHelper.getProp("ro.build.characteristics", "default");
            return mDeviceType != null && mDeviceType.toLowerCase().contains("tablet");
        }

        public static boolean isPadBySize(Context context) {
            Display display = getDisplay(context);
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            return Math.sqrt(x + y) >= 7.0;
        }

        public static boolean isPadByApi(Context context) {
            return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }

        private static class SystemPropHelper {

            private static final Class<?> clazz = InvokeUtils.findClass("android.os.SystemProperties");

            public static String getProp(String key, String def) {
                return (String) Optional.ofNullable(invokeMethod("get", new Class[]{String.class, String.class}, key, def))
                    .orElse(def);
            }

            private static <T> T invokeMethod(String str, Class<?>[] clsArr, Object... objArr) {
                return InvokeUtils.callStaticMethod(clazz, str, clsArr, objArr);
            }
        }
    }

    // ----------------------------------------------------------------------------------------
    public static Bitmap drawableToBitmap(Drawable drawable) {
        return drawableToBitmap(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (width <= 0 || height <= 0) {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
        }

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static class RoundedDrawable extends Drawable {
        private final static HashMap<BitmapData, RoundedDrawable> mCacheDrawables = new HashMap<>();
        private final Bitmap mBitmap; // 原始位图
        private final Paint mPaint; // 绘制时使用的画笔
        private final RectF mRectF; // 绘制区域的矩形框
        private float mCornerRadius; // 圆角半径
        private final BitmapShader mBitmapShader; // 位图着色器，用于绘制位图
        private final Matrix mShaderMatrix; // 用于调整 BitmapShader 的矩阵

        /**
         * 构造函数
         * @param bitmap 原始 Bitmap
         * @param cornerRadius 圆角半径，单位为像素
         */
        public RoundedDrawable(Bitmap bitmap, float cornerRadius) {
            mBitmap = bitmap;
            mCornerRadius = cornerRadius;
            mPaint = new Paint();
            mPaint.setAntiAlias(true); // 启用抗锯齿
            // 创建 BitmapShader，并设置 TileMode.CLAMP，防止边缘重复
            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(mBitmapShader);
            mRectF = new RectF();
            mShaderMatrix = new Matrix();
        }

        /**
         * 当 Drawable 的边界发生变化时会调用此方法，
         * 同时在这里更新绘制区域和 BitmapShader 的矩阵
         */
        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            super.onBoundsChange(bounds);
            mRectF.set(bounds);
            updateShaderMatrix();
        }

        /**
         * 根据当前 Drawable 的 bounds 以及 Bitmap 的大小，计算合适的缩放与平移，
         * 使得 Bitmap 完全显示在 Drawable 中（类似于 fitCenter 效果）。
         */
        private void updateShaderMatrix() {
            mShaderMatrix.set(null);
            float scale;
            float dx = 0, dy = 0;
            int bitmapWidth = mBitmap.getWidth();
            int bitmapHeight = mBitmap.getHeight();

            // 计算缩放比例，采用 fitCenter 策略（保证整个图片都能显示）
            if (bitmapWidth * mRectF.height() > mRectF.width() * bitmapHeight) {
                scale = mRectF.width() / (float) bitmapWidth;
                dy = (mRectF.height() - bitmapHeight * scale) * 0.5f;
            } else {
                scale = mRectF.height() / (float) bitmapHeight;
                dx = (mRectF.width() - bitmapWidth * scale) * 0.5f;
            }
            mShaderMatrix.setScale(scale, scale);
            mShaderMatrix.postTranslate((int)(dx + 0.5f) + mRectF.left, (int)(dy + 0.5f) + mRectF.top);
            mBitmapShader.setLocalMatrix(mShaderMatrix);
        }

        /**
         * 重写 draw 方法，在画布上绘制圆角矩形
         */
        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRectF, mCornerRadius, mCornerRadius, mPaint);
        }

        /**
         * 设置透明度
         */
        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
            invalidateSelf();
        }

        /**
         * 设置颜色过滤器
         */
        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        /**
         * 获取不透明度类型
         */
        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        /**
         * 设置新的圆角半径，并刷新 Drawable
         * @param cornerRadius 新的圆角半径
         */
        public void setCornerRadius(float cornerRadius) {
            mCornerRadius = cornerRadius;
            invalidateSelf();
        }

        /**
         * 获取当前的圆角半径
         */
        public float getCornerRadius() {
            return mCornerRadius;
        }

        /**
         * 工厂方法，方便外部调用
         */
        public static RoundedDrawable fromBitmap(Bitmap bitmap, float cornerRadius) {
            RoundedDrawable roundedDrawable;
            BitmapData bitmapData = new BitmapData(bitmap, cornerRadius);
            if (mCacheDrawables.get(bitmapData) == null) {
                roundedDrawable = new RoundedDrawable(bitmap, cornerRadius);
                mCacheDrawables.put(bitmapData, roundedDrawable);
            } else
                roundedDrawable = mCacheDrawables.get(bitmapData);

            return roundedDrawable;
        }

        private record BitmapData(Bitmap bitmap, float cornerRadius) {
            @Override
            public boolean equals(Object object) {
                if (this == object) return true;
                if (!(object instanceof BitmapData that)) return false;
                return Float.compare(cornerRadius, that.cornerRadius) == 0 && Objects.equals(bitmap, that.bitmap);
            }

            @Override
            public int hashCode() {
                return Objects.hash(bitmap, cornerRadius);
            }
        }
    }
}
