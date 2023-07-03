package org.luke.diminou.abs.components.controls.scratches;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.abs.ColoredView;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.StyleToColor;
import org.luke.diminou.data.property.Property;

public class ColoredSeparator extends Separator implements Styleable {

    private final StyleToColor color;

    public ColoredSeparator(App owner, Orientation orientation, float margin, StyleToColor color) {
        super(owner, orientation, margin);
        this.color = color;

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setColor(color.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
