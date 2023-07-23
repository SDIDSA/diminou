package org.luke.diminou.app.pages.game.offline;

import android.view.Gravity;
import android.view.View;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class OfflinePassInit extends VBox implements Styleable {
    private final Label passInitLabel;
    private final Button passInitYes;
    private final Button passInitNo;

    public OfflinePassInit(App owner) {
        super(owner);
        setGravity(Gravity.CENTER);

        passInitLabel = new Label(owner, "pass_init");
        passInitYes = new Button(owner, "yes");
        passInitYes.setMinimumWidth(ViewUtils.dipToPx(70, owner));
        ViewUtils.setMarginRight(passInitYes, owner, 15);
        passInitNo = new Button(owner, "no");
        passInitNo.setMinimumWidth(ViewUtils.dipToPx(70, owner));

        HBox passInitButtons = new HBox(owner);
        ViewUtils.setMarginTop(passInitButtons, owner, 15);
        passInitButtons.setGravity(Gravity.CENTER);
        passInitButtons.addView(passInitYes);
        passInitButtons.addView(passInitNo);

        passInitNo.setOnClick(this::hide);

        addView(passInitLabel);
        addView(passInitButtons);

        setAlpha(0);
        setTranslationY(ViewUtils.by(owner));
        setScaleX(.5f);
        setScaleY(.5f);

        applyStyle(owner.getStyle());
    }

    private boolean shown = false;
    public void show(Runnable onYes) {
        if(shown) return;
        shown = true;
        setVisibility(View.VISIBLE);
        new ParallelAnimation(300)
                .addAnimation(new AlphaAnimation(this, 1))
                .addAnimation(new ScaleXYAnimation(this, 1))
                .addAnimation(new TranslateYAnimation(this, 0))
                .setInterpolator(Interpolator.EASE_OUT).start();

        passInitYes.setOnClick(onYes);
    }

    public void hide() {
        if(!shown) return;
        shown = false;
        new ParallelAnimation(300)
                .addAnimation(new AlphaAnimation(this, 0))
                .addAnimation(new ScaleXYAnimation(this, .5f))
                .addAnimation(new TranslateYAnimation(this, ViewUtils.dipToPx(40, getOwner())))
                .setOnFinished(() -> setVisibility(View.INVISIBLE))
                .setInterpolator(Interpolator.EASE_OUT).start();
    }

    @Override
    public void applyStyle(Style style) {
        passInitLabel.setFill(style.getTextNormal());
        passInitYes.setFill(style.getBackgroundPrimary());
        passInitYes.setTextFill(style.getTextNormal());
        passInitNo.setFill(style.getBackgroundPrimary());
        passInitNo.setTextFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
