package org.luke.diminou.app.pages.game.pause;

import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

import java.util.function.Consumer;

public class ToggleIcon extends FrameLayout implements Styleable {
    private static final int SIZE = 38;

    private final ColorIcon icon;
    private final Rectangle disable;
    private final Animation show, hide;

    private Consumer<Boolean> onChange;

    public ToggleIcon(App owner,@DrawableRes int res) {
        super(owner);

        icon = new ColorIcon(owner, res);
        icon.setSize(SIZE);
        icon.setCornerRadius(12);
        ViewUtils.setPaddingUnified(icon, 6, owner);

        disable = new Rectangle(owner);
        disable.setWidth(SIZE - 3);
        disable.setRotation(-45);
        disable.setHeight(8);
        disable.setStrokeWidth(3);

        ViewUtils.alignInFrame(disable, Gravity.CENTER);

        addView(icon);
        addView(disable);

        icon.setOnClick(() -> {
            owner.playMenuSound(R.raw.swap);
            if(enabled) disable();
            else enable();
        });

        show = new ParallelAnimation(300)
                .addAnimation(new ValueAnimation(0, SIZE - 3) {
                    @Override
                    public void updateValue(float v) {
                        disable.setWidth(v);
                    }
                })
                .setInterpolator(Interpolator.EASE_OUT);
        hide = new ParallelAnimation(300)
                .addAnimation(new ValueAnimation(SIZE - 3, 0) {
                    @Override
                    public void updateValue(float v) {
                        disable.setWidth(v);
                    }
                })
                .setInterpolator(Interpolator.EASE_OUT);

        enable();
        applyStyle(owner.getStyle());
    }

    public void setOnChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
    }

    boolean enabled = false;

    public synchronized void enable() {
        if(enabled) return;
        enabled = true;

        if(onChange != null) onChange.accept(true);

        show.stop();
        hide.start();
    }

    public synchronized void disable() {
        if(!enabled) return;
        enabled = false;

        if(onChange != null) onChange.accept(false);

        hide.stop();
        show.start();
    }

    public synchronized void apply(String p) {
        if(p.equals("on")) {
            enable();
        }else {
            disable();
        }
    }

    @Override
    public void applyStyle(Style style) {
        icon.setFill(style.getTextNormal());
        icon.setBackgroundColor(style.getBackgroundPrimary());
        disable.setFill(style.getTextNormal());
        disable.setStroke(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
