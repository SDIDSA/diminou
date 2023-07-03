package org.luke.diminou.abs.components.controls.image;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.functional.StyleToColor;
import org.luke.diminou.data.property.Property;

public class ColoredIcon extends ColorIcon implements Styleable {
    private final StyleToColor color;

    public ColoredIcon(App owner, StyleToColor color , int id) {
        super(owner, id);
        this.color = color;

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(color.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
