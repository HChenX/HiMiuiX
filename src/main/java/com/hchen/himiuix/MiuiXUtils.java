package com.hchen.himiuix;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;

public class MiuiXUtils {
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
        Point point = new Point();
        getWindowSize(context, point);
        return point;
    }

    @Deprecated
    public static int getWindowHeight(Context context) {
        return getWindowSize(context).y;
    }

    public static void getScreenSize(Context context, Point point) {
        getScreenSize(getWindowManager(context), point);
    }

    public static Point getScreenSize(Context context) {
        Point point = new Point();
        getScreenSize(getWindowManager(context), point);
        return point;
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
}
