package org.luke.diminou.app.pages;

import android.view.Gravity;

import androidx.core.graphics.Insets;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class SplashScreen extends Page implements Styleable {
    private final App owner;
    private final ColorIcon icon;
    private final ColorIcon subIcon;

    public SplashScreen(App owner) {
        super(owner);

        this.owner = owner;

        VBox root = new VBox(owner);
        root.setGravity(Gravity.CENTER);

        icon = new ColorIcon(owner, R.drawable.icon);
        icon.setSize(100);
        subIcon = new ColorIcon(owner, R.drawable.diminou);
        subIcon.setSize(80);

        root.addView(ViewUtils.spacer(owner));
        root.addView(icon);
        root.addView(ViewUtils.spacer(owner));
        root.addView(subIcon);

        addView(root);
        ViewUtils.setPaddingUnified(this, 30, owner);

        applyStyle(owner.getStyle());
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void applyInsets(Insets insets) {

    }

    @Override
    public void applyStyle(Style style) {
        int color = style.getTextNormal();
        icon.setColor(color);
        subIcon.setColor(color);

        owner.setBackgroundColor(style.getBackgroundTertiary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
