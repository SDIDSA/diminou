package org.luke.diminou.app.pages.home.online.global;

import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.ElevationAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;

public class NavItem extends ColoredIcon {
    public NavItem(App owner, @DrawableRes int icon) {
        super(owner, Style::getTextNormal, icon);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, ViewUtils.dipToPx(42, owner));
        params.weight = 1;
        setLayoutParams(params);
        ViewUtils.setPaddingUnified(this, 10, owner);
        deselect();
    }

    public void select() {
        new ParallelAnimation(300)
                .addAnimation(new ScaleXYAnimation(this, 1.3f))
                .addAnimation(new AlphaAnimation(this, 1))
                .addAnimation(new ElevationAnimation(this, ViewUtils.dipToPx(5, owner)))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    public void deselect() {
        new ParallelAnimation(300)
                .addAnimation(new ScaleXYAnimation(this, 1f))
                .addAnimation(new AlphaAnimation(this, .6f))
                .addAnimation(new ElevationAnimation(this, 0))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }
}
