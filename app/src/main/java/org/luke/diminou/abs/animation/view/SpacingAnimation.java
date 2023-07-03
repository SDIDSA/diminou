package org.luke.diminou.abs.animation.view;

import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;
import org.luke.diminou.abs.components.layout.linear.LinearBox;
import org.luke.diminou.abs.utils.ErrorHandler;

public class SpacingAnimation extends ViewAnimation {

    public SpacingAnimation(long duration, View view, float to) {
        super(duration, view, to);
        check(view);
    }

    public SpacingAnimation(View view, float to) {
        super(view, to);
        check(view);
    }

    public void check(View view) {
        if(!(view instanceof LinearBox)) {
            ErrorHandler.handle(new IllegalArgumentException("the spacing animation only works on LinearBox views"), "running spacing animation");
        }
    }

    @Override
    protected float getFrom(View view) {
        return (float) ((LinearBox) view).getSpacing();
    }

    @Override
    protected void apply(View view, float v) {
        ((LinearBox) view).setSpacing(v);
    }
}
