package org.luke.diminou.app.pages.home.online.global;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;

public class AvatarViewChange extends MultipleOptionOverlay {
    public AvatarViewChange(App owner, Runnable onView, Runnable onChange) {
        super(owner, "Your avatar", o -> false);

        addButton("Preview", () -> {
            hide();
            onView.run();
        });
        addButton("Change", () -> {
            hide();
            onChange.run();
        });
    }
}
