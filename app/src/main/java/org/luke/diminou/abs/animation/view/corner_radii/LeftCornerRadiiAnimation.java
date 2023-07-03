package org.luke.diminou.abs.animation.view.corner_radii;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;
import org.luke.diminou.abs.utils.ErrorHandler;

public class LeftCornerRadiiAnimation extends ViewAnimation {
    private float top_right, bottom_right;

    public LeftCornerRadiiAnimation(long duration, View view, float to) {
        super(duration, view, to);
        check(view);
    }

    public LeftCornerRadiiAnimation(View view, float to) {
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
        float[] all = ((GradientDrawable) view.getBackground()).getCornerRadii();
        if (all == null) {
            top_right = 0;
            bottom_right = 0;
            return 0;
        }
        top_right = all[2];
        bottom_right = all[4];
        return all[0];
    }

    @Override
    protected void apply(View view, float v) {
        ((GradientDrawable) view.getBackground()).setCornerRadii(new float[]{
                v, v,
                top_right, top_right,
                bottom_right, bottom_right,
                v, v
        });
    }
}
