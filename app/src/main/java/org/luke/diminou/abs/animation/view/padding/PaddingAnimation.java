package org.luke.diminou.abs.animation.view.padding;

import android.util.Log;
import android.view.View;

import org.luke.diminou.abs.animation.base.ViewAnimation;

public class PaddingAnimation extends ViewAnimation {

    private int fromLeft, fromTop, fromRight, fromBottom;
    private final int toLeft, toTop, toRight, toBottom;

    public PaddingAnimation(long duration, View view, int toLeft, int toTop, int toRight, int toBottom) {
        super(duration, view, 0, 1);

        this.toLeft = toLeft;
        this.toTop = toTop;
        this.toRight = toRight;
        this.toBottom = toBottom;
    }

    @Override
    public void init() {
        super.init();
        fromLeft = getView().getPaddingLeft();
        fromTop = getView().getPaddingTop();
        fromRight = getView().getPaddingRight();
        fromBottom = getView().getPaddingBottom();
    }

    @Override
    protected float getFrom(View view) {
        return 0;
    }

    @Override
    protected void apply(View view, float v) {
        int left = (int) (fromLeft + (toLeft - fromLeft) * v);
        int top = (int) (fromTop + (toTop - fromTop) * v);
        int right = (int) (fromRight + (toRight - fromRight) * v);
        int bottom = (int) (fromBottom + (toBottom - fromBottom) * v);
        view.setPadding(left, top, right, bottom);
    }
}
