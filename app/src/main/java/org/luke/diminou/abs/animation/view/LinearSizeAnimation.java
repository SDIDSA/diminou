package org.luke.diminou.abs.animation.view;

import android.view.View;
import android.widget.LinearLayout;

import org.luke.diminou.abs.animation.base.ViewAnimation;
import org.luke.diminou.abs.utils.ErrorHandler;

public class LinearSizeAnimation extends ViewAnimation {

    public LinearSizeAnimation(View view, float to) {
        super(view, to);
        check(view);
    }

    public void check(View view) {
        if(!(view.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            ErrorHandler.handle(new IllegalArgumentException("can't use linear height animation on non linear children"), "creating linear height animation");
        }
    }

    @Override
    protected float getFrom(View view) {
        return view.getLayoutParams().height;
    }

    @Override
    protected void apply(View view, float v) {
        view.getLayoutParams().height = (int) v;
        view.getLayoutParams().width = (int) v;
        view.requestLayout();
    }
}
