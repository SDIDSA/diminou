package org.luke.diminou.abs.animation.view.position;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class TranslateXAnimation extends ViewAnimation {
    public TranslateXAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public TranslateXAnimation(View view, float to) {
        super(view, to);
    }

    public TranslateXAnimation(View view,float from, float to) {
        super(view, from, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getTranslationX();
    }

    @Override
    protected void apply(View view, float v) {
        view.setTranslationX(v);
    }
}
