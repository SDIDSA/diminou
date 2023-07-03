package org.luke.diminou.app.pages.home;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.abs.utils.Store;

public class ConfirmExit extends MultipleOptionOverlay {
    public ConfirmExit(App owner) {
        super(owner, "exit_confirm", s -> s.equals("yes"));
        addButton("cancel", this::hide);
        addButton("yes", () -> {
            Home.settingUp = true;
            Page.clearCache();
            Store.destroy();
            owner.finishAndRemoveTask();
        });
    }
}
