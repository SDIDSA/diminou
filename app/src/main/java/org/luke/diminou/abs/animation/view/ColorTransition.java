package org.luke.diminou.abs.animation.view;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.animation.base.ColorAnimation;
import org.luke.diminou.abs.components.controls.abs.ColoredView;

public class ColorTransition extends ColorAnimation {
    private final ColoredView view;

    public ColorTransition(long duration, ColoredView view, @ColorInt int to) {
        super(duration, view.getFill(), to);
        this.view = view;
    }

    public ColorTransition(ColoredView view, @ColorInt int to) {
        super(view.getFill(), to);
        this.view = view;
    }

    public ColorTransition(ColoredView view, @ColorInt int from, @ColorInt int to) {
        super(from, to);
        this.view = view;
    }

    @Override
    public void start() {
        setFrom(view.getFill());
        super.start();
    }

    @Override
    public void updateValue(int color) {
        view.setFill(color);
    }
}
