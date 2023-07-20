package org.luke.diminou.app.pages.home.online.play.playIcons;

import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;

public class OneVOne extends BasicPlayIcon {
    public OneVOne(App owner) {
        super(owner,  "1 VS 1", R.drawable.battle);

        ViewUtils.alignInFrame(ic1, Gravity.END);
        ViewUtils.alignInFrame(ic2, Gravity.END | Gravity.BOTTOM);
        ViewUtils.alignInFrame(ic3, Gravity.BOTTOM | Gravity.START);
        ViewUtils.alignInFrame(lab, Gravity.TOP | Gravity.START);
    }
}
