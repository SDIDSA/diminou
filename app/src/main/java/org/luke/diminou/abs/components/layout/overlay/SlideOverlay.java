package org.luke.diminou.abs.components.layout.overlay;

import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.ViewGroup;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public abstract class SlideOverlay extends Overlay implements Styleable {
    protected final VBox list;
    private float initTY, initPY, lastY, velocity;
    private long lastTime;

    public SlideOverlay(App owner) {
        super(owner);

        list = new VBox(owner);
        list.setTranslationY(owner.getScreenHeight());
        list.setScaleX(.6f);
        list.setScaleY(.6f);

        list.setOnClickListener(e -> {
            //consume
        });

        addView(list);

        list.setElevation(ViewUtils.dipToPx(40, owner));

        addToShow(new TranslateYAnimation(list, 0)
                .setLateTo(() -> (float) (owner.getScreenHeight() - list.getHeight() - getMarginBottom()))
                .setLateFrom(() -> (float) owner.getScreenHeight() - list.getHeight() / 2 - getMarginBottom()));
        addToShow(new ScaleXYAnimation(list, .6f, 1));
        addToShow(new AlphaAnimation(list, 1));

        addToHide(new TranslateYAnimation(list, 0)
                .setLateTo(() -> list.getTranslationY() + ViewUtils.dipToPx(40, owner)));
        addToHide(new AlphaAnimation(list, 0));

        Animation releaseShow = new TranslateYAnimation(300, list, 0)
                .setLateTo(() -> (float) (owner.getScreenHeight() - list.getHeight() - getMarginBottom()))
                .setInterpolator(Interpolator.EASE_OUT);

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    initTY = event.getRawY();
                    initPY = list.getTranslationY();
                    lastY = initTY;
                }
                case MotionEvent.ACTION_UP -> {
                    float fdy = event.getRawY() - initTY;
                    if (Math.abs(fdy) < 10) {
                        v.performClick();
                    } else {
                        long dy = System.currentTimeMillis() - lastTime;
                        if (dy > 300) {
                            velocity = 0;
                        }
                        if (velocity > 10) {
                            hide();
                        } else if (velocity < -10) {
                            releaseShow.start();
                        } else {
                            int min = owner.getScreenHeight() - list.getHeight() - getMarginBottom();
                            int max = owner.getScreenHeight();
                            int mid = (max + min) / 2;
                            if (list.getTranslationY() > mid) {
                                hide();
                            } else {
                                releaseShow.start();
                            }
                        }
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    float nty = event.getRawY();
                    velocity = nty - lastY;
                    float dy = nty - initTY;
                    float ny = initPY + dy;
                    lastY = nty;
                    list.setTranslationY(Math.max(ny, owner.getScreenHeight() - list.getHeight() - getMarginBottom()));
                    lastTime = System.currentTimeMillis();
                }
            }
            return true;
        });

        applyStyle(owner.getStyle());
    }

    @Override
    public void show() {
        owner.addOverlay(this);
        Platform.runBack(() -> {
            while(list.getHeight() < ViewUtils.dipToPx(100, owner)) {
                Platform.sleep(10);
            }
            Platform.runLater(() -> {
                owner.removeOverlay(this);
                super.show();
            });
        });
    }

    protected void setHeight(int height) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        list.setLayoutParams(params);
    }

    protected void setHeightFactor(double factor) {
        setHeight((int) (owner.getScreenHeight() * factor));
    }

    private int getMarginBottom() {
        return ((MarginLayoutParams)list.getLayoutParams()).bottomMargin;
    }

    @Override
    public void applyStyle(Style style) {
        list.setBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
