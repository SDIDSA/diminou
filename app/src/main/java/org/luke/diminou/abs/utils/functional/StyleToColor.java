package org.luke.diminou.abs.utils.functional;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.style.Style;

public interface StyleToColor {
    @ColorInt int get(Style style);
}
