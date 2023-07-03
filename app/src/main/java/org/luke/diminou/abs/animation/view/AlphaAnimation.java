package org.luke.diminou.abs.animation.view;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class AlphaAnimation extends ViewAnimation {

    public AlphaAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public AlphaAnimation(View view, float to) {
        super(view, to);
    }

    public AlphaAnimation(View view, float from, float to) {
        super(view, from, to);
    }

    @Override
    protected void apply(View view, float v) {
        view.setAlpha(v);
    }

    @Override
    protected float getFrom(View view) {
        return view.getAlpha();
    }
}
