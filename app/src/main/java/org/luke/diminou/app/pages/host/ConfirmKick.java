package org.luke.diminou.app.pages.host;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;

public class ConfirmKick extends MultipleOptionOverlay {

    private Runnable onYes;
    public ConfirmKick(App owner) {
        super(owner, "kick_confirm", s -> s.equals("yes"));

        addButton("cancel", this::hide);
        addButton("yes", () -> {
            if(onYes != null) {
                onYes.run();
            }
            hide();
        });
    }

    public void setOnYes(Runnable onYes) {
        this.onYes = onYes;
    }
}
