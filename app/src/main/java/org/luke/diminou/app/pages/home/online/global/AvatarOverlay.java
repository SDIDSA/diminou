package org.luke.diminou.app.pages.home.online.global;

import android.graphics.Bitmap;
import android.view.Gravity;

import androidx.core.graphics.Insets;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class AvatarOverlay extends Overlay implements Styleable {
    private final Image img;
    public AvatarOverlay(App owner, Bitmap avatar) {
        super(owner);

        img = new Image(owner);
        img.setImageBitmap(avatar);
        img.setSize(ViewUtils.pxToDip(owner.getScreenWidth(), owner) - 20);
        img.setCornerRadius(15);

        addView(img);

        img.setAlpha(0f);
        img.setScaleX(0.5f);
        img.setScaleY(0.5f);

        addToShow(new AlphaAnimation(img, 1));
        addToShow(new ScaleXYAnimation(img, 1));

        addToHide(new AlphaAnimation(img, 0));
        addToHide(new ScaleXYAnimation(img, 0.5f));

        ViewUtils.alignInFrame(img, Gravity.CENTER);

        applyStyle(owner.getStyle());
    }

    @Override
    public void applySystemInsets(Insets insets) {

    }

    @Override
    public void applyStyle(Style style) {
        img.setBackgroundColor(style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
