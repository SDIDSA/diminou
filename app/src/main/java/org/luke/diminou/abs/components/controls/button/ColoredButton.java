package org.luke.diminou.abs.components.controls.button;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.data.property.Property;

import java.util.function.Function;

public class ColoredButton extends Button implements Styleable {
    private final Function<Style, Integer> fill;
    private final Function<Style, Integer> textFill;
    public ColoredButton(App owner,
                         Function<Style, Integer> fill,
                         Function<Style, Integer> textFill,
                         String text) {
        super(owner, text);

        this.fill = fill;
        this.textFill = textFill;

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.apply(style));
        setTextFill(textFill.apply(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
