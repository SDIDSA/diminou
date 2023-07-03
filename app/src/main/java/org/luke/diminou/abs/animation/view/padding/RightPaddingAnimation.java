package org.luke.diminou.abs.animation.view.padding;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class RightPaddingAnimation extends ViewAnimation {
    public RightPaddingAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public RightPaddingAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPaddingRight();
    }

    @Override
    protected void apply(View view, float v) {
        view.setPadding(view.getPaddingLeft(),view.getPaddingTop(), (int) v,view.getPaddingBottom());
    }
}
