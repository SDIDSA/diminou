package org.luke.diminou.abs.components.layout.overlay.media.medialist;

import androidx.recyclerview.widget.RecyclerView;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.padding.UnifiedPaddingAnimation;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.media.Media;
import org.luke.diminou.data.property.Property;

public class MediaEntry extends Image implements Styleable {
    public static void clearSelection() {
        if(selected != null) {
            selected.deselect();
        }
    }
    private static MediaEntry selected;
    private final int size;
    private final Animation press;
    private final Animation release;

    public MediaEntry(App owner, int size) {
        super(owner);
        setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        this.size = size;
        setSize(ViewUtils.pxToDip(size, owner));

        press = new UnifiedPaddingAnimation(300, this, ViewUtils.dipToPx(10, owner))
                .setInterpolator(Interpolator.EASE_OUT);
        release = new UnifiedPaddingAnimation(300, this, 0)
                .setInterpolator(Interpolator.EASE_OUT);

        setScaleType(ScaleType.CENTER_CROP);

        setPadding(0, 0, 0, 0);

        applyStyle(owner.getStyle());
    }

    public void select() {
        if (selected == this) return;
        else if (selected != null) selected.deselect();
        release.stop();
        press.start();
        selected = this;
    }

    public void deselect() {
        press.stop();
        release.start();
        selected = null;
    }

    public void load(Media media) {
        setImageBitmap(null);
        media.getThumbnail(
                getOwner(),
                size,
                this::setImageBitmap,
                () -> setImageResource(R.drawable.problem)
        );
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}