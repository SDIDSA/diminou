package org.luke.diminou.app.pages.game;

import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;

public class Cherrat extends HBox {
    private final PassInit passInit;

    public Cherrat(App owner) {
        super(owner);
        setGravity(Gravity.BOTTOM);
        setPadding(7);

        ColoredIcon khabt = new ColoredIcon(owner, Style::getTextNormal, R.drawable.khabet_static);
        khabt.setSize(38);

        ColoredIcon sakt = new ColoredIcon(owner, Style::getTextNormal, R.drawable.saket_static);
        sakt.setSize(38);

        khabt.setOnClick(() -> cherra("khabet", R.drawable.khabet, R.raw.khabet));

        sakt.setOnClick(() -> cherra("saket", R.drawable.saket, R.raw.saket));

        passInit = new PassInit(owner);

        addView(sakt);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(passInit);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(khabt);

        ViewUtils.setMarginBottom(this, owner, 20);
    }

    public PassInit getPassInit() {
        return passInit;
    }

    Animation show() {
        setTranslationY(ViewUtils.dipToPx(80, getOwner()));
        return new ParallelAnimation().addAnimation(new AlphaAnimation(this, 1))
                .addAnimation(new TranslateYAnimation(this, 0));
    }

    public Animation hide() {
        return new ParallelAnimation()
                .addAnimation(new AlphaAnimation(this, 0))
                .addAnimation(new TranslateYAnimation(this, ViewUtils.dipToPx(80, getOwner())));
    }

    private void cherra(String name, int drawable, int sound) {
        Game game = (Game) Page.getInstance(getOwner(), Game.class);
        assert game != null;
        game.getBottomHolder().cherra(drawable, sound);
        if(game.isHost()) {
            getOwner().getSockets()
                    .forEach(s -> s.emit(name, game.getBottomHolder().getPlayer().serialize()));
        } else {
            getOwner().getSocket().emit(name, game.getBottomHolder().getPlayer().serialize());
        }
    }
}
