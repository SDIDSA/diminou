package org.luke.diminou.abs.animation.view.padding;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class BottomPaddingAnimation extends ViewAnimation {
    public BottomPaddingAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public BottomPaddingAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPaddingBottom();
    }

    @Override
    protected void apply(View view, float v) {
        view.setPadding(view.getPaddingLeft(),view.getPaddingTop(), view.getPaddingRight(),(int) v);
    }
}
