package org.luke.diminou.abs.animation.easing;

public interface Interpolator {
    Interpolator LINEAR = new Linear();
    Interpolator EASE_IN = new EaseIn();
    Interpolator EASE_OUT = new EaseOut();
    Interpolator EASE_BOTH = new EaseInOut();

    Interpolator ANTICIPATE_OVERSHOOT = new AnticipateOvershoot();
    Interpolator ANTICIPATE = new Anticipate();
    Interpolator OVERSHOOT = new Overshoot();

    float interpolate(float v);
}
