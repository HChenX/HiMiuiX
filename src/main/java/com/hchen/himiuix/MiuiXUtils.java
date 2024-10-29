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
package com.hchen.himiuix;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.Arrays;
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
}
