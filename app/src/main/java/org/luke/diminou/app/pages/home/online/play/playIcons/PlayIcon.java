package org.luke.diminou.app.pages.home.online.play.playIcons;

import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class PlayIcon extends StackPane implements Styleable {
    public static final float SIZE = 140;
    public PlayIcon(App owner) {
        super(owner);

        setPadding(15);
        setCornerRadius(10);

        setElevation(ViewUtils.dipToPx(10, owner));

        int sizePx = ViewUtils.dipToPx(SIZE, owner);
        setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));

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
