package org.luke.diminou.abs.animation.easing;

public class EaseOut implements Interpolator {
    @Override
    public float interpolate(float v) {
        return (float) (1 - Math.pow(1 - v, 4));
    }
}
