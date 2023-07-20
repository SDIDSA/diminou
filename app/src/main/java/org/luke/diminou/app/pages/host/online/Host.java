package org.luke.diminou.app.pages.host.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.cards.online.DisplayCards;
import org.luke.diminou.app.cards.online.MirorredCards;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.home.online.global.RoomId;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.beans.User;

public class Host extends Titled {
    private final RoomId idDisp;

    private final DisplayCards cards;
    private final MirorredCards mirorredCards;

    private String roomId;

    private final Invite invite;

    public Host(App owner) {
        super(owner, "create_party");

        idDisp = new RoomId(owner);

        cards = new DisplayCards(owner, true);
        mirorredCards = new MirorredCards(owner);

        mirorredCards.bind(cards);

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
        content.addView(mirorredCards);
    }

    public String getRoomId() {
        return roomId;
    }

    public void joined(int id) {
        User.getForId(id, user -> cards.getLast().loadPlayer(user));
    }

    public void left(int id) {
        cards.unloadPlayer(id);
    }

    @Override
    public void setup() {
        super.setup();
        cards.unloadAll();

        Room room = owner.getRoom();
        roomId = room.getId();
        idDisp.setId(room.getId());

        Platform.runBack(() -> {
            for (int player : room.getPlayers()) {
                if(player == -1) break;
                User u = User.getForIdSync(player);
                Platform.runLater(() -> cards.getLast().loadPlayer(u));
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();

        Session.endGame(roomId, res -> {
            //IGNORE
        });
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }
}
