package org.luke.diminou.abs.components.controls.abs;

import androidx.annotation.ColorInt;

public interface ColoredView {
    @ColorInt
    int getFill();

    void setFill(@ColorInt int fill);
}
