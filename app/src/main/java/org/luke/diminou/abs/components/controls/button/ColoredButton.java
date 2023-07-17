package org.luke.diminou.abs.components.controls.button;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.functional.StyleToColor;
import org.luke.diminou.data.property.Property;

public class ColoredButton extends Button implements Styleable {
    private final StyleToColor fill;
    private final StyleToColor textFill;
    public ColoredButton(App owner,
                         StyleToColor fill,
                         StyleToColor textFill,
                         String text) {
        super(owner, text);

        this.fill = fill;
        this.textFill = textFill;

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.get(style));
        setTextFill(textFill.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
