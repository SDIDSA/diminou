package org.luke.diminou.abs.components.controls.text;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.functional.StyleToColor;
import org.luke.diminou.data.property.Property;

public class ColoredLabel extends Label implements Styleable {
    private final StyleToColor fill;

    public ColoredLabel(App owner, String key, StyleToColor fill) {
        super(owner, key);

        this.fill = fill;

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
