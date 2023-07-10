package org.luke.diminou.abs.components.controls.input;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

import java.util.function.Consumer;

public class Switch extends StackPane implements Styleable {
    private final App owner;

    private final GradientDrawable trackBack;
    private final GradientDrawable thumbBack;
    private final StackPane thumb;

    private final float sizeDp;
    private final int sizePx;

    private Consumer<Boolean> onChange;
    private Consumer<Boolean> postChange;

    public Switch(App owner, float sizeDp) {
        super(owner);
        this.owner = owner;
        this.sizeDp = sizeDp;
        sizePx = ViewUtils.dipToPx(sizeDp, owner);

        setLayoutParams(new LinearLayout.LayoutParams((int) (sizePx * 2.5f), sizePx));

        trackBack = new GradientDrawable();
        trackBack.setCornerRadius(sizePx / 4f);
        View track = new View(owner);
        track.setBackground(trackBack);
        track.setLayoutParams(new LayoutParams((int) (sizePx * 1.5f), sizePx / 2));
        ViewUtils.setMarginHorizontal(track, owner, sizeDp / 2);
        ViewUtils.alignInFrame(track, Gravity.CENTER);

        thumbBack = new GradientDrawable();
        thumbBack.setCornerRadius(sizePx / 2f);
        thumb = new StackPane(owner);
        thumb.setBackground(thumbBack);
        thumb.setElevation(sizeDp / 3);
        thumb.setLayoutParams(new LayoutParams(sizePx, sizePx));

        addView(track);
        addView(thumb);

        setOnClickListener((v) -> toggle());

        applyStyle(owner.getStyle());
    }

    private ColoredIcon oldIcon;
    public void setIcon(@DrawableRes int res) {
        ColoredIcon icon = new ColoredIcon(owner, Style::getBackgroundPrimary, res);
        icon.setSize(sizeDp);
        icon.setAlpha(0f);
        icon.setTranslationY(sizePx);

        ParallelAnimation set = new ParallelAnimation(400)
                .addAnimation(new TranslateYAnimation(icon, 0))
                .addAnimation(new AlphaAnimation(icon, 1))
                .setInterpolator(Interpolator.EASE_OUT);

        if(oldIcon != null) {
            final ColoredIcon old = oldIcon;
            set.addAnimation(new TranslateYAnimation(old, -sizePx))
                    .addAnimation(new AlphaAnimation(old, 0))
                    .setOnFinished(() -> thumb.removeView(old));
        }
        thumb.addView(icon);

        oldIcon = icon;
        set.start();
    }

    private boolean state = false;
    private void toggle() {
        if(state) {
            disable();
        }else {
            enable();
        }
    }

    public void enable() {
        state = true;

        if(onChange != null) onChange.accept(true);

        new TranslateXAnimation(400, thumb, sizePx * 1.5f)
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    if(postChange != null) postChange.accept(true);
                })
                .start();
    }

    public void disable() {
        state = false;

        if(onChange != null) onChange.accept(false);

        new TranslateXAnimation(400, thumb, 0)
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    if(postChange != null) postChange.accept(false);
                })
                .start();
    }

    public void setOnChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
    }

    public void setPostChange(Consumer<Boolean> postChange) {
        this.postChange = postChange;
    }

    @Override
    public void applyStyle(Style style) {
        trackBack.setColor(style.getTextMuted());
        thumbBack.setColor(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
