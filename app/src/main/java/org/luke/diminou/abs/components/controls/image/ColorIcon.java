package org.luke.diminou.abs.components.controls.image;

import android.graphics.PorterDuff;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;

public class ColorIcon extends Image {

    public ColorIcon(App owner, @DrawableRes int id) {
        super(owner);
        if (id != Integer.MIN_VALUE)
            setImageResource(id);
    }

    public ColorIcon(App owner) {
        this(owner, Integer.MIN_VALUE);
    }

    public void setColor(@ColorInt int color) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void setFill(int fill) {
        setColor(fill);
    }
}
