package org.luke.diminou.abs.components.controls.scratches;

import android.view.Gravity;
import org.luke.diminou.abs.components.layout.StackPane;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.utils.ViewUtils;

public class Loading extends HBox {
    private static final int count = 4;
    private final Rectangle[] rectangles;
    private final ParallelAnimation loader;

    public Loading(App owner, float size) {
        super(owner);
        setAlpha(.6f);

        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        rectangles = new Rectangle[count];

        StackPane.LayoutParams params = new StackPane.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        setHorizontalGravity(Gravity.CENTER);

        setSpacing((int) size);

        for(int i = 0; i < count; i++) {
            rectangles[i] = new Rectangle(owner, size, size);
            addView(rectangles[i]);
            rectangles[i].setRadius(size);
            rectangles[i].setSize(size, size);
        }

        float shift = ViewUtils.dipToPx(-(size * 2), owner);
        Runnable preLoad = () -> {
            for(Rectangle rect : rectangles) {
                rect.setTranslationX(shift);
            }
            rectangles[0].setAlpha(0);
            rectangles[count - 1].setAlpha(1);
        };

        preLoad.run();

        setTranslationX(-shift / 2);

        loader = new ParallelAnimation(500)
                .addAnimation(new AlphaAnimation(rectangles[0],0, 1))
                .addAnimation(new AlphaAnimation(rectangles[count - 1], 1, 0))
                .setInterpolator(Interpolator.EASE_OUT).setCycleCount(Animation.INDEFINITE);
        for(int i = 0; i < count; i++) {
            loader.addAnimation(new TranslateXAnimation(rectangles[i], shift, 0));
        }
    }

    private boolean running = false;

    @Override
    protected void onAttachedToWindow() {
        if(running)
            loader.start();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if(running)
            loader.stop();
        super.onDetachedFromWindow();
    }

    public void startLoading() {
        running = true;
        loader.start();
    }

    public void stopLoading() {
        running = false;
        loader.stop();
    }

    public void setColor(int c) {
        for(Rectangle r : rectangles) {
            r.setFill(c);
        }
    }

    public void setFill(int fill) {
        setColor(fill);
    }
}
