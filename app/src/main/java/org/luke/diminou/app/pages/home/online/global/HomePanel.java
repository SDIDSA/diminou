package org.luke.diminou.app.pages.home.online.global;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class HomePanel extends HBox implements Styleable {
    public HomePanel(App owner) {
        super(owner);
        setCornerRadius(15);
        setGravity(Gravity.CENTER_VERTICAL);
        setElevation(ViewUtils.dipToPx(4, owner));
        ViewUtils.setPaddingUnified(this, 10, owner);

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
