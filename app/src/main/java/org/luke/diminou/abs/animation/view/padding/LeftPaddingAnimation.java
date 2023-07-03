package org.luke.diminou.abs.animation.view.padding;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class LeftPaddingAnimation extends ViewAnimation {
    public LeftPaddingAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public LeftPaddingAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPaddingLeft();
    }

    @Override
    protected void apply(View view, float v) {
        view.setPadding((int) v, view.getPaddingTop(),view.getPaddingRight(),view.getPaddingBottom());
    }
}
