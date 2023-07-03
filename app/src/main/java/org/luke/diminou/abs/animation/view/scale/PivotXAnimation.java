package org.luke.diminou.abs.animation.view.scale;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class PivotXAnimation extends ViewAnimation {
    public PivotXAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public PivotXAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPivotX();
    }

    @Override
    protected void apply(View view, float v) {
        view.setPivotX(v);
    }
}
