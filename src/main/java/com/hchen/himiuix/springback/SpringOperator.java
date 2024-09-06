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
