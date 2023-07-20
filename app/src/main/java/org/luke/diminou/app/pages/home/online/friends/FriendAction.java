package org.luke.diminou.app.pages.home.online.friends;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class FriendAction extends ColorIcon implements Styleable {
    public FriendAction(App owner, int id) {
        super(owner, id);
        ViewUtils.setMarginLeft(this, owner, 8);
        ViewUtils.setPaddingUnified(this, 8, owner);
        setCornerRadius(7);
        setSize(36);

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(style.getTextNormal());
        //setBorder(style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
