package org.luke.diminou.app.pages.join.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.data.beans.Room;

public class Invited extends MultipleOptionOverlay {
    public Invited(App owner, String username, Room room) {
        super(owner, username + " invited you to join a room", s -> s.equals("Join now"));

        addButton("Decline", this::hide);

        addButton("Join now", () -> {
            hide();
            Session.join(room.getId(), res -> {
                if(res.has("err")) {
                    owner.toast(res.getString("err"));
                }
            });
        });
    }
}
