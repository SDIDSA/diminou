package org.luke.diminou.abs.animation.view;

import android.util.Log;
import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class RotateAnimation extends ViewAnimation {

    public RotateAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public RotateAnimation(View view, float to) {
        super(view, to);
    }

    public RotateAnimation(View view, float from, float to) {
        super(view, from, to);
    }

    @Override
    protected void apply(View view, float v) {
        float norm = ((v + 360) % 360);
        view.setRotation(norm);
    }

    @Override
    protected float getFrom(View view) {
        return view.getRotation();
    }
}
