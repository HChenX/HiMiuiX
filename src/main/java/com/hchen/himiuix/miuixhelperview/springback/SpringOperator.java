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
package com.hchen.himiuix.miuixhelperview.springback;

public class SpringOperator {
    private final double damping;
    private final double tension;

    public SpringOperator(float f2, float f3) {
        this.tension = Math.pow(6.283185307179586d / (double) f3, 2.0d);
        this.damping = (f2 * 12.566370614359172d) / (double) f3;
    }

    public double updateVelocity(double d2, float f2, double d3, double d4) {
        return (d2 * (1.0d - (this.damping * f2))) + ((float) (this.tension * (d3 - d4) * f2));
    }
}
