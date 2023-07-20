package org.luke.diminou.app.pages.home.online.global;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class RoomId extends HBox implements Styleable {
    private final ColoredLabel id;
    public RoomId(App owner) {
        super(owner);
        setPadding(10);
        setCornerRadius(7);

        ColoredLabel idLab = new ColoredLabel(owner, "Room ID", Style::getTextNormal);
        id = new ColoredLabel(owner, "", Style::getTextNormal);

        addView(idLab);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(id);

        applyStyle(owner.getStyle());
    }

    public void setId(String id) {
        this.id.setText(id);
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(style.getBackgroundPrimary());
        setBorderColor(style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
