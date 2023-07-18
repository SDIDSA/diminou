package org.luke.diminou.app.pages.home.online.play.playIcons;

import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.game.piece.Piece;

public class BasicPlayIcon extends PlayIcon {
    protected final ColorIcon ic1;
    protected final ColorIcon ic2;
    protected final ColoredIcon ic3;
    protected final ColoredLabel lab;
    public BasicPlayIcon(App owner, String text, @DrawableRes int icon) {
        super(owner);

        Piece p1 = Piece.random();
        Piece p2 = Piece.random();

        ic1 = p1.getImage(owner, PlayIcon.SIZE / 3 - 12, Orientation.VERTICAL);
        ic2 = p2.getImage(owner, PlayIcon.SIZE / 3 - 12, Orientation.HORIZONTAL);
        ic3 = new ColoredIcon(owner, Style::getTextNormal, icon);
        ic3.setSize(PlayIcon.SIZE / 3 - 12);
        ViewUtils.setPadding(ic3, 4, 4, 4, 4, owner);

        pieces.add(ic1);
        pieces.add(ic2);

        lab = new ColoredLabel(owner, text, Style::getTextNormal);
        lab.setLineSpacing(4);
        lab.setFont(new Font(18));

        addView(ic1);
        addView(ic2);
        addView(ic3);
        addView(lab);
    }
}
