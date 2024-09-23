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

import android.graphics.Color;

public class ColorPickerData {
    public int hue = 0;
    public int saturation = 0;
    public int lightness = 100;
    public int alpha = 255;

    public int HSVToColor() {
        return Color.HSVToColor(alpha, new float[]{(float) hue / 100, (float) saturation / 10000, (float) lightness / 10000});
    }
}
