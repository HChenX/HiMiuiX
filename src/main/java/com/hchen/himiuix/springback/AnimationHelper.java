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
package com.hchen.himiuix.springback;

import android.view.View;

class AnimationHelper {
    public static void postInvalidateOnAnimation(View view) {
        AnimationHandler.getInstance().postVsyncCallback();
        view.postInvalidateOnAnimation();
    }

    public static void postOnAnimation(View view, Runnable action) {
        AnimationHandler.getInstance().postVsyncCallback();
        view.postOnAnimation(action);
    }
}
