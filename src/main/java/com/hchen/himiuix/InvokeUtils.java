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

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

class InvokeUtils {
    private static final String TAG = "InvokeUtils";
    private static final HashMap<String, Method> methodCache = new HashMap<>();
    private static final HashMap<String, Field> fieldCache = new HashMap<>();

    // ----------------------------反射调用方法--------------------------------
    static <T> T callMethod(Object instance, String method, Class<?>[] param, Object... value) {
        return baseInvokeMethod(null, instance, method, param, value);
    }

    static <T> T callStaticMethod(Class<?> clz, String method, Class<?>[] param, Object... value) {
        return baseInvokeMethod(clz, null, method, param, value);
    }

    static <T> T callStaticMethod(String clz, String method, Class<?>[] param, Object... value) {
        return baseInvokeMethod(findClass(clz), null, method, param, value);
    }

    static <T> T callStaticMethod(String clz, ClassLoader classLoader, String method, Class<?>[] param, Object... value) {
        return baseInvokeMethod(findClass(clz, classLoader), null, method, param, value);
    }

    // ----------------------------设置字段--------------------------------
    static <T> T setField(Object instance, String field, Object value) {
        return baseInvokeField(null, instance, field, true, value);
    }

    static <T> T setStaticField(Class<?> clz, String field, Object value) {
        return baseInvokeField(clz, null, field, true, value);
    }

    static <T> T setStaticField(String clz, String field, Object value) {
        return baseInvokeField(findClass(clz), null, field, true, value);
    }

    static <T> T setStaticField(String clz, ClassLoader classLoader, String field, Object value) {
        return baseInvokeField(findClass(clz, classLoader), null, field, true, value);
    }

    static <T> T getField(Object instance, String field) {
        return baseInvokeField(null, instance, field, false, null);
    }

    static <T> T getStaticField(Class<?> clz, String field) {
        return baseInvokeField(clz, null, field, false, null);
    }

    static <T> T getStaticField(String clz, String field) {
        return baseInvokeField(findClass(clz), null, field, false, null);
    }

    static <T> T getStaticField(String clz, ClassLoader classLoader, String field) {
        return baseInvokeField(findClass(clz, classLoader), null, field, false, null);
    }

    /**
     * @noinspection unchecked
     */
    private static <T> T baseInvokeMethod(Class<?> clz /* 类 */, Object instance /* 实例 */, String method /* 方法名 */,
                                          Class<?>[] param /* 方法参数 */, Object... value /* 值 */) {
        Method declaredMethod;
        if (clz == null && instance == null) {
            Log.w(TAG, "Class and instance is null, can't invoke method: " + method);
            return null;
        } else if (clz == null) {
            clz = instance.getClass();
        }
        try {
            String methodTag = clz.getName() + "#" + method + "#" + Arrays.toString(param);
            declaredMethod = methodCache.get(methodTag);
            if (declaredMethod == null) {
                declaredMethod = clz.getDeclaredMethod(method, param);
                methodCache.put(methodTag, declaredMethod);
            }
            declaredMethod.setAccessible(true);
            return (T) declaredMethod.invoke(instance, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "BaseInvokeMethod Failed: ", e);
            return null;
        }
    }

    /**
     * @noinspection unchecked
     */
    private static <T> T baseInvokeField(Class<?> clz /* 类 */, Object instance /* 实例 */, String field /* 字段名 */,
                                         boolean set /* 是否为 set 模式 */, Object value /* 指定值 */) {
        Field declaredField = null;
        if (clz == null && instance == null) {
            Log.w(TAG, "Class and instance is null, can't invoke field: " + field);
            return null;
        } else if (clz == null) {
            clz = instance.getClass();
        }
        try {
            String fieldTag = clz.getName() + "#" + field;
            declaredField = fieldCache.get(fieldTag);
            if (declaredField == null) {
                try {
                    declaredField = clz.getDeclaredField(field);
                } catch (NoSuchFieldException e) {
                    while (true) {
                        clz = clz.getSuperclass();
                        if (clz == null || clz.equals(Object.class))
                            break;

                        try {
                            declaredField = clz.getDeclaredField(field);
                            break;
                        } catch (NoSuchFieldException ignored) {
                        }
                    }
                    if (declaredField == null) throw e;
                }
                fieldCache.put(fieldTag, declaredField);
            }
            declaredField.setAccessible(true);
            if (set) {
                declaredField.set(instance, value);
                return null;
            } else
                return (T) declaredField.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "BaseInvokeField Failed: ", e);
            return null;
        }
    }

    static Class<?> findClass(String className) {
        return findClass(className, null);
    }

    static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            if (classLoader == null)
                classLoader = ClassLoader.getSystemClassLoader();

            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "FindClass Failed: ", e);
        }
        return null;
    }
}
