package com.hchen.himiuix.springback;

public class SpringOperator {
    private final double damping;
    private final double tension;

    public SpringOperator(float f, float f2) {
        double d = (double)f2;
        this.tension = Math.pow(6.283185307179586 / d, 2.0);
        this.damping = (double)f * 12.566370614359172 / d;
    }

    public double updateVelocity(double d, float f, double d2, double d3) {
        return d * (1.0 - this.damping * (double)f) + (double)((float)(this.tension * (d2 - d3) * (double)f));
    }
}
