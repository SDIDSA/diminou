package org.luke.diminou.abs.animation.view;

import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.view.View;
import android.widget.TextView;

import org.luke.diminou.abs.animation.base.ViewAnimation;
import org.luke.diminou.abs.utils.ViewUtils;

public class FontSizeAnimation extends ViewAnimation {

    private TextView view;

    public FontSizeAnimation(long duration, TextView view, float to) {
        super(duration, view, to);
        this.view = view;
    }

    public FontSizeAnimation(TextView view, float to) {
        super(view, to);
        this.view = view;
    }

    public FontSizeAnimation(TextView view, float from, float to) {
        super(view, from, to);
        this.view = view;
    }

    @Override
    protected void apply(View view, float v) {
        this.view.setTextSize(COMPLEX_UNIT_PX ,ViewUtils.spToPx(v, view.getContext()));
    }

    @Override
    protected float getFrom(View view) {
        return ViewUtils.pxToSp(this.view.getTextSize(), view.getContext());
    }
}
