package org.luke.diminou.app.avatar;

import android.graphics.drawable.GradientDrawable;

import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.LinearLayout;


import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class AvatarDisplay extends StackPane implements Styleable {
    private final App owner;
    private final GradientDrawable background;
    private final GradientDrawable foreground;

    private final Image img;

    public static final int preSize = 64;

    public AvatarDisplay(App owner, float sizeDp) {
        super(owner);
        this.owner = owner;

        int size = ViewUtils.dipToPx(sizeDp, owner);
        setLayoutParams(new LinearLayout.LayoutParams(size, size));

        background = new GradientDrawable();
        foreground = new GradientDrawable();

        int radii = ViewUtils.dipToPx(7, owner);
        background.setCornerRadius(radii);
        foreground.setCornerRadius(radii);
        setBackground(background);
        setForeground(foreground);

        img = new Image(owner);
        img.setSize(sizeDp);
        img.setCornerRadius(7);

        addView(img);

        applyStyle(owner.getStyle());
    }

    public AvatarDisplay(App owner) {
        this(owner, preSize);
    }

    public Image getImg() {
        return img;
    }

    public void setOnClick(Runnable onClick) {
        img.setOnClick(onClick);
    }

    public void setValue(Avatar value) {
        img.setImageResource(value.getRes());
    }

    public void setUrl(String url) {
        ImageProxy.getImage(url, img::setImageBitmap);
    }

    public void setValue(String val) {
        setValue(Avatar.valueOf(val));
    }

    @Override
    public void applyStyle(Style style) {
        background.setColor(style.getBackgroundPrimary());
        foreground.setStroke(ViewUtils.dipToPx(1, owner), style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
