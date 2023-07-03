package org.luke.diminou.abs.animation.view.padding;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class UnifiedPaddingAnimation extends ViewAnimation {
    public UnifiedPaddingAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public UnifiedPaddingAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getPaddingLeft();
    }

    @Override
    protected void apply(View view, float v) {
        int iv = (int) v;
        view.setPadding(iv, iv, iv, iv);
    }
}
