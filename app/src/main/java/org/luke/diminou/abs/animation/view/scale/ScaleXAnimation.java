package org.luke.diminou.abs.animation.view.scale;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class ScaleXAnimation extends ViewAnimation {
    public ScaleXAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public ScaleXAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getScaleX();
    }

    @Override
    protected void apply(View view, float v) {
        view.setScaleX(v);
    }
}
