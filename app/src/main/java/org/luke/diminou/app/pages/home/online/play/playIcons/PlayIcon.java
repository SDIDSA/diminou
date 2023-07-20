package org.luke.diminou.app.pages.home.online.play.playIcons;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.LinearSizeAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayIcon extends StackPane implements Styleable {
    private static final ArrayList<PlayIcon> cache = new ArrayList<>();

    public static List<PlayIcon> getOthers(PlayIcon pressed) {
        return cache.stream().filter(i -> i != pressed && i.isAttachedToWindow())
                .collect(Collectors.toList());
    }

    public static List<PlayIcon> getAll() {
        return cache.stream().filter(View::isAttachedToWindow)
                .collect(Collectors.toList());
    }

    protected final ArrayList<ColorIcon> pieces = new ArrayList<>();
    public static final float SIZE = 140;
    public static int sizePx = -1;
    @SuppressLint("ClickableViewAccessibility")
    public PlayIcon(App owner) {
        super(owner);
        cache.add(this);

        setPadding(15);
        setCornerRadius(10);

        setElevation(ViewUtils.dipToPx(10, owner));

        sizePx = ViewUtils.dipToPx(SIZE, owner);
        setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));

        setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN -> select(this);
                case MotionEvent.ACTION_UP -> {
                    if (e.getX() > 0 && e.getX() < getWidth() &&
                            e.getY() > 0 && e.getY() < getHeight()) {
                        open(this);
                        performClick();
                        Platform.runAfter(PlayIcon::deselect, 500);
                    } else {
                        deselect();
                    }
                }
            }
            return true;
        });

        applyStyle(owner.getStyle());
    }

    public static void select(PlayIcon icon) {
        icon.big();
        getOthers(icon).forEach(PlayIcon::small);
    }

    public static void open(PlayIcon icon) {
        icon.tooBig();
        getOthers(icon).forEach(PlayIcon::tooSmall);
    }

    public static void deselect() {
        getAll().forEach(PlayIcon::normal);
    }

    private void big() {
        new ParallelAnimation(300)
                .addAnimation(new LinearSizeAnimation(this, sizePx * 1.2f))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    private void tooBig() {
        new ParallelAnimation(300)
                .addAnimation(new LinearSizeAnimation(this, sizePx * 2f))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    private void tooSmall() {
        new ParallelAnimation(300)
                .addAnimation(new LinearSizeAnimation(this, 0))
                .addAnimation(new AlphaAnimation(this, 0f))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    private void small() {
        new ParallelAnimation(300)
                .addAnimation(new LinearSizeAnimation(this, sizePx * 0.8f))
                .addAnimation(new AlphaAnimation(this, .5f))
                .addAnimations(pieces.stream().map(p -> new AlphaAnimation(p, 0)).toArray(AlphaAnimation[]::new))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    private void normal() {
        new ParallelAnimation(300)
                .addAnimation(new LinearSizeAnimation(this, sizePx))
                .addAnimation(new AlphaAnimation(this, 1))
                .addAnimations(pieces.stream().map(p -> new AlphaAnimation(p, 1)).toArray(AlphaAnimation[]::new))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
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
