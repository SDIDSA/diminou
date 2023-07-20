package org.luke.diminou.app.pages.home.offline;

import android.view.View;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.RotateAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.abs.components.controls.image.ColorIcon;

import java.util.Random;

public class FloatingPiece {
    private final ColorIcon img;
    private final Animation anim;
    private final Animation hide;

    private static final Random random = new Random();

    public FloatingPiece(App owner) {
        Piece p = Piece.random();
        img = p.getImage(owner, 50);
        img.setAlpha(.4f);

        img.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        int ir = random.nextInt(360);
        int ix = random.nextInt(owner.getScreenWidth());
        int iy = random.nextInt(owner.getScreenHeight());

        img.setAlpha(0f);
        img.setScaleX(.8f);
        img.setScaleY(.8f);

        img.setRotation(ir);
        img.setTranslationY(iy);
        img.setTranslationX(ix);

        int tr = ir + 30 + random.nextInt(50);

        anim = new RotateAnimation(10000, img, tr)
                .setFrom(ir)
                .setCycleCount(Animation.INDEFINITE)
                .setAutoReverse(true)
                .setFps(10)
                .setInterpolator(Interpolator.LINEAR);
        Animation show = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(img, .3f))
                .addAnimation(new ScaleXYAnimation(img, 1))
                .setInterpolator(Interpolator.OVERSHOOT);
        hide = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(img, 0))
                .addAnimation(new ScaleXYAnimation(img, .8f))
                .setInterpolator(Interpolator.ANTICIPATE);

        show.start(50);
        anim.start(100);
    }

    public void hide(Runnable post) {
        hide.start();
        hide.setOnFinished(() -> {
            anim.stop();
            post.run();
        });
    }

    public ColorIcon getImg() {
        return img;
    }
}
