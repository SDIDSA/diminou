package org.luke.diminou.app.pages.home.online.play.playIcons;

import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;

public class FourPlayers extends BasicPlayIcon {
    public FourPlayers(App owner) {
        super(owner, "Game\nOf 4", R.drawable.four);

        ViewUtils.alignInFrame(ic1, Gravity.START | Gravity.BOTTOM);
        ViewUtils.alignInFrame(ic2, Gravity.START | Gravity.TOP);
        ViewUtils.alignInFrame(ic3, Gravity.TOP | Gravity.END);
        ViewUtils.alignInFrame(lab, Gravity.BOTTOM | Gravity.END);
    }
}
