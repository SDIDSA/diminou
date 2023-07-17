package org.luke.diminou.app.pages.home.online.play;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;
import org.luke.diminou.app.pages.home.online.play.playIcons.FourPlayers;
import org.luke.diminou.app.pages.home.online.play.playIcons.OneVOne;
import org.luke.diminou.app.pages.home.online.play.playIcons.PlayIcon;
import org.luke.diminou.app.pages.home.online.play.playIcons.PrivatePlay;

public class Play extends HomeFragment {
    public Play(App owner) {
        super(owner);

        setGravity(Gravity.CENTER);
        setClipChildren(false);

        HBox row1 = new HBox(owner);
        row1.setClipChildren(false);
        row1.setGravity(Gravity.CENTER);
        PlayIcon ovo = new OneVOne(owner);
        ViewUtils.setMarginRight(ovo, owner, 15);
        row1.addView(ovo);
        row1.addView(new FourPlayers(owner));

        addView(row1);

        addView(new PrivatePlay(owner));
    }
}
