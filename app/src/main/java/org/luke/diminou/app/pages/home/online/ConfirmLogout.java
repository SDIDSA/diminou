package org.luke.diminou.app.pages.home.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.app.pages.login.Login;
import org.luke.diminou.data.SessionManager;
import org.luke.diminou.data.beans.Bean;

public class ConfirmLogout extends MultipleOptionOverlay {
    public ConfirmLogout(App owner) {
        super(owner, "Sign out of your account?", s -> s.equals("yes"));
        addButton("cancel", this::hide);
        addButton("yes", () -> {
            startLoading("yes");
            Session.logout(res -> {
                stopLoading("yes");
                hide();
                Bean.clearCache();
                SessionManager.clearSession(owner);
                owner.loadPage(Login.class);
            });
        });
    }
}
