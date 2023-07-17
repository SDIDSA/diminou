package org.luke.diminou.app.pages.home.online.play.playIcons;

import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.game.piece.Piece;

public class PrivatePlay extends PlayIcon {
    protected final ColorIcon ic1;
    protected final ColorIcon ic2;
    protected final ColoredIcon ic3;
    protected final ColoredLabel lab;
    public PrivatePlay(App owner) {
        super(owner);

        Piece p1 = Piece.random();
        Piece p2 = Piece.random();

        ic1 = p1.getImage(owner, PlayIcon.SIZE / 3 - 15, Orientation.VERTICAL);
        ic2 = p2.getImage(owner, PlayIcon.SIZE / 3 - 15, Orientation.VERTICAL);
        ic3 = new ColoredIcon(owner, Style::getTextNormal, R.drawable.friends);
        ic3.setSize(PlayIcon.SIZE / 3 - 15);
        ViewUtils.setPadding(ic3, 4, 4, 4, 4, owner);

        lab = new ColoredLabel(owner, "Private", Style::getTextNormal);
        lab.setLineSpacing(4);
        lab.setFont(new Font(17));

        addView(ic1);
        addView(ic2);
        addView(ic3);
        addView(lab);

        ViewUtils.alignInFrame(ic1, Gravity.START | Gravity.TOP);
        ViewUtils.alignInFrame(ic2, Gravity.END | Gravity.BOTTOM);
        ViewUtils.alignInFrame(ic3, Gravity.END | Gravity.TOP);
        ViewUtils.alignInFrame(lab, Gravity.START | Gravity.BOTTOM);
    }
}
