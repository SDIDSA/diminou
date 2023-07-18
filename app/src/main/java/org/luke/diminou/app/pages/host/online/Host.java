package org.luke.diminou.app.pages.host.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.cards.online.DisplayCards;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.data.beans.User;

public class Host extends Titled {
    private final HBox idDisp;
    private final ColoredLabel id;

    private final DisplayCards cards;

    private final Invite invite;

    public Host(App owner) {
        super(owner, "Private Game");

        idDisp = new HBox(owner);
        idDisp.setPadding(10);
        idDisp.setCornerRadius(7);

        ColoredLabel idLab = new ColoredLabel(owner, "Room ID", Style::getTextNormal);
        id = new ColoredLabel(owner, "", Style::getTextNormal);

        idDisp.addView(idLab);
        idDisp.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        idDisp.addView(id);

        cards = new DisplayCards(owner, true);

        invite = new Invite(owner);

        cards.forEach(card -> {
            card.setOnClickListener(v -> {
                if(!card.isLoaded()) {
                    invite.show();
                }
            });
        });

        content.addView(idDisp);
        content.addView(cards);

        applyStyle(owner.getStyle());
    }

    @Override
    public void setup() {
        super.setup();
        cards.unloadAll();

        Session.createGame(res -> {
            String roomId = res.getString("room_id");
            owner.putRoomId(roomId);
            id.setText(roomId);

            cards.getLast().loadPlayer(owner.getUser());
        });
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }

    @Override
    public void applyStyle(Style style) {
        if(idDisp == null) return;

        super.applyStyle(style);

        idDisp.setBackground(style.getBackgroundPrimary());
        idDisp.setBorderColor(style.getTextMuted());
    }
}
