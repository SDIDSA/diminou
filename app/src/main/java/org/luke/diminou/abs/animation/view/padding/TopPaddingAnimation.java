package org.luke.diminou.abs.animation.view.padding;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class TopPaddingAnimation extends ViewAnimation {

    public TopPaddingAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public TopPaddingAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPaddingTop();
    }

    @Override
    protected void apply(View view, float v) {
        view.setPadding(view.getPaddingLeft(), (int) v, view.getPaddingRight(), view.getPaddingBottom());
    }
}
