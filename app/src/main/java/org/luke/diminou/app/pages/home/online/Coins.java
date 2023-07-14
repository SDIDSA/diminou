package org.luke.diminou.app.pages.home.online;

import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class Coins extends HBox implements Styleable {
    private final ColoredLabel value;

    public Coins(App owner) {
        super(owner);
        setCornerRadius(10);
        setPadding(7);
        setElevation(-ViewUtils.dipToPx(10, owner));

        setGravity(Gravity.CENTER_VERTICAL);

        setLayoutParams(new LayoutParams(ViewUtils.dipToPx(100, owner), -2));

        value = new ColoredLabel(owner, "", Style::getTextNormal);

        ColoredIcon coins = new ColoredIcon(owner, Style::getTextNormal, R.drawable.coins);
        coins.setSize(18);

        addView(value);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(coins);

        applyStyle(owner.getStyle());
    }

    public void setValue(int val) {
        value.setText(String.valueOf(val));
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(style.getBackgroundTertiary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
