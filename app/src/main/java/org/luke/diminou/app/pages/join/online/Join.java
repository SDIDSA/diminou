package org.luke.diminou.app.pages.join.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.cards.online.DisplayCards;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.home.online.global.RoomId;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.beans.User;

public class Join extends Titled {
    private final RoomId idDisp;
    private final HostDisplay host;

    private final DisplayCards cards;

    private String roomId;

    public Join(App owner) {
        super(owner, "join_party");

        idDisp = new RoomId(owner);
        host = new HostDisplay(owner);

        cards = new DisplayCards(owner, false);

        content.addView(idDisp);
        content.addView(host);
        content.addView(cards);
    }

    public void joined(int id) {
        User.getForId(id, user -> cards.getLast().loadPlayer(user));
    }

    public void left(int id) {
        cards.unloadPlayer(id);
    }

    public String getRoomId() {
        return roomId;
    }

    public void swap(int i1, int i2) {
        cards.swap(i1, i2);
    }

    @Override
    public void setup() {
        super.setup();
        cards.unloadAll();

        Room room = owner.getRoom();
        roomId = room.getId();
        idDisp.setId(room.getId());

        User.getForId(room.getHost(), host::setUser);

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

        Session.leave(owner.getUser().getId(), roomId, res -> {
            //IGNORE
        });
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }
}
