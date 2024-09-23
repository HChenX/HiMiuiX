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
package com.hchen.himiuix.springback;

public class SpringOperator {
    private final double damping;
    private final double tension;

    public SpringOperator(float f, float f2) {
        tension = Math.pow(6.283185307179586 / (double) f2, 2.0);
        damping = (double)f * 12.566370614359172 / (double) f2;
    }

    public double updateVelocity(double d, float f, double d2, double d3) {
        return d * (1.0 - damping * (double)f) + (double)((float)(tension * (d2 - d3) * (double)f));
    }
}
