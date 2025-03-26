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

public class SpringOperator {
    private final double damping;
    private final double tension;

    public SpringOperator(float n, float n2) {
        tension = Math.pow(6.283185307179586 / (double) n2, 2.0);
        damping = n * 12.566370614359172 / (double) n2;
    }

    public double updateVelocity(double n, float n2, double n3, double n4) {
        return n * (1.0 - damping * (double) n2) + (float) (tension * (n3 - n4) * (double) n2);
    }
}
