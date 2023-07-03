package org.luke.diminou.abs.animation.easing;

public class EaseInOut implements Interpolator {
    @Override
    public float interpolate(float v) {
        return v * v * (3.0f - 2.0f * v);
    }
}
