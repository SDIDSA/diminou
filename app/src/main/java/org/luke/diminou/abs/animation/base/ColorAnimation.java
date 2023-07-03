package org.luke.diminou.abs.animation.base;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public abstract class ColorAnimation extends Animation {
    private int fromRed, fromGreen, fromBlue, fromAlpha;
    private int toRed, toGreen, toBlue, toAlpha;

    public ColorAnimation(long duration, @ColorInt int from, @ColorInt int to) {
        super(duration);
        setFrom(from);
        setTo(to);
    }

    public ColorAnimation(@ColorInt int from, @ColorInt int to) {
        super();
        setFrom(from);
        setTo(to);
    }


    public void setFrom(@ColorInt int from) {
        fromRed = Color.red(from);
        fromGreen = Color.green(from);
        fromBlue = Color.blue(from);
        fromAlpha = Color.alpha(from);
    }

    public void setTo(@ColorInt int to) {
        toRed = Color.red(to);
        toGreen = Color.green(to);
        toBlue = Color.blue(to);
        toAlpha = Color.alpha(to);
    }

    @Override
    public void update(float v) {
        int red = (int) (fromRed + (toRed - fromRed) * v);
        int green = (int) (fromGreen + (toGreen - fromGreen) * v);
        int blue = (int) (fromBlue + (toBlue - fromBlue) * v);
        int alpha = (int) (fromAlpha + (toAlpha - fromAlpha) * v);
        updateValue(Color.argb(alpha, red, green, blue));
    }

    public abstract void updateValue(@ColorInt int color);
}
