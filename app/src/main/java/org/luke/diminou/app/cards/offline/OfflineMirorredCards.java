package org.luke.diminou.app.cards.offline;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.utils.ViewUtils;

public class OfflineMirorredCards extends VBox {
    private final OfflinePlayerCard[] cards = new OfflinePlayerCard[4];

    public OfflineMirorredCards(App owner) {
        super(owner);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ViewUtils.spacer(this);
        setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        HBox center = new HBox(owner);
        center.setClipChildren(false);
        center.setClipToOutline(false);
        center.setClipToPadding(false);

        setClipChildren(false);

        int[] order = new int[] {0,1,3,2};
        for(int i = 0; i < 4; i++) {
            int oi = order[i];
            cards[oi] = new OfflinePlayerCard(owner, false, oi);
            cards[oi].setHolder(this);
            switch (i) {
                case 0, 3 -> addView(cards[oi]);
                case 1 -> {
                    addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
                    addView(center);
                    addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
                    center.addView(cards[oi]);
                    center.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
                }
                case 2 -> center.addView(cards[oi]);
            }
        }
    }

    public void bind(OfflineDisplayCards other) {
        for(int i = 0; i < 4; i++) {
            cards[i].bind(other.getAt(i));
        }
    }

    public void unbind() {
        for(OfflinePlayerCard card : cards) {
            card.unbind();
        }
    }
}
