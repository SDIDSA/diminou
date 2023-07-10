package org.luke.diminou.abs.components.layout.overlay;

import android.graphics.Color;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ColorAnimation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.utils.Platform;

import java.util.ArrayList;

public abstract class Overlay extends StackPane {
    protected final App owner;

    private final ParallelAnimation show, hide;
    private final ArrayList<Runnable> onHidden;
    private final ArrayList<Runnable> onShowing;

    private boolean autoHide = true;

    public Overlay(App owner) {
        super(owner);
        this.owner = owner;
        int shown = Color.argb(192, 0, 0, 0);
        int hidden = Color.argb(0, 0, 0, 0);
        setBackgroundColor(hidden);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        onHidden = new ArrayList<>();
        onShowing = new ArrayList<>();

        show = new ParallelAnimation(300)
                .setInterpolator(Interpolator.EASE_OUT);

        show.addAnimation(new ValueAnimation(0, 192) {
            @Override
            public void updateValue(float color) {
                setBackgroundColor(Color.argb((int) Math.min(192, color), 0, 0, 0));
            }
        });

        hide = new ParallelAnimation(300)
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    owner.removeOverlay(this);
                    for(Runnable act : onHidden) {
                        act.run();
                    }
                });

        hide.addAnimation(new ColorAnimation(shown, hidden) {
            @Override
            public void updateValue(int color) {
                setBackgroundColor(color);
            }
        });

        setOnClickListener(e -> {if(autoHide) hide();});
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    private boolean shown = false;
    public boolean isShown() {
        return shown;
    }

    public void show() {
        if(shown) return;
        shown = true;
        hide.stop();
        show.stop();
        for(Runnable act : onShowing) {
            act.run();
        }
        owner.addOverlay(this);
        Platform.runBack(() -> {
            while(!ViewCompat.isLaidOut(this)) {
                Platform.sleep(5);
            }
            show.start();
        });
    }

    public void hide() {
        if(!shown) return;
        shown = false;
        show.stop();
        hide.stop();
        hide.start();
    }

    public void addToShow(Animation animation) {
        show.addAnimation(animation);
    }

    public void addToHide(Animation animation) {
        hide.addAnimation(animation);
    }

    public void addOnHidden(Runnable onHidden) {
        this.onHidden.add(onHidden);
    }

    public void addOnShowing(Runnable onShown) {
        this.onShowing.add(onShown);
    }

    public void back() {
        if(autoHide)
            hide();
    }

    public abstract void applySystemInsets(Insets insets);
}
