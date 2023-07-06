package org.luke.diminou.app.pages.game;

import android.view.Gravity;

import androidx.core.graphics.Insets;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class GamePause extends Overlay implements Styleable {
    private final VBox root;

    public GamePause(App owner) {
        super(owner);

        root = new VBox(owner);
        root.setPadding(10);
        root.setCornerRadius(10);

        ViewUtils.alignInFrame(root, Gravity.CENTER);

        addView(root);

        applyStyle(owner.getStyle());
    }

    @Override
    public void applySystemInsets(Insets insets) {

    }

    @Override
    public void applyStyle(Style style) {
        root.setBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
