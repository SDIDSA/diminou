package org.luke.diminou.abs.animation.view.corner_radii;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;
import org.luke.diminou.abs.utils.ErrorHandler;

public class RightCornerRadiiAnimation extends ViewAnimation {
    private float top_left, bottom_left;
    public RightCornerRadiiAnimation(long duration, View view, float to) {
        super(duration, view, to);
        check(view);
    }

    public RightCornerRadiiAnimation(View view, float to) {
        super(view, to);
        check(view);
    }

    private void check(View view) {
        if (!(view.getBackground() instanceof GradientDrawable)) {
            ErrorHandler.handle(new IllegalArgumentException("invalid background type..."), "creating corner radius animation");
        }
    }

    @Override
    protected float getFrom(View view) {
        float[] all = ((GradientDrawable)view.getBackground()).getCornerRadii();
        if(all == null) {
            top_left = 0;
            bottom_left = 0;
            return 0;
        }
        top_left = all[0];
        bottom_left = all[6];
        return all[2];
    }

    @Override
    protected void apply(View view, float v) {
        ((GradientDrawable)view.getBackground()).setCornerRadii(new float[]{
                top_left, top_left,
                v, v,
                v, v,
                bottom_left, bottom_left
        });
    }
}
