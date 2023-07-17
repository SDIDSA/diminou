package org.luke.diminou.app.pages.home.online.friends;

import org.luke.diminou.abs.App;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.online.Home;

public class FriendRequests extends Titled {
    public FriendRequests(App owner) {
        super(owner, "Friend requests");
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }
}
