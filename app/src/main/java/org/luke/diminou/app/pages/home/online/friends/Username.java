package org.luke.diminou.app.pages.home.online.friends;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.observable.ChangeListener;

public class Username extends ColoredLabel {
    private final ChangeListener<String> onChange;
    private User old;
    public Username(App owner) {
        super(owner, "", Style::getTextNormal);
        onChange = (obs, ov, nv) -> setText(nv);
    }

    public void setUser(User user) {
        if(old != null) {
            old.usernameProperty().removeListener(onChange);
        }

        if(user != null) {
            old = user;
            user.usernameProperty().addListener(onChange);
        }
    }
}
