package org.luke.diminou.app.pages.home.offline;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.abs.utils.Platform;

public class ConfirmExit extends MultipleOptionOverlay {
    public ConfirmExit(App owner) {
        super(owner, "exit_confirm", s -> s.equals("yes"));
        addButton("cancel", this::hide);
        addButton("yes", () -> {
            OfflineHome.settingUp = true;
            Page.clearCache();
            owner.finishAndRemoveTask();
            Platform.runAfter(() ->
                    System.exit(0), 1000);
        });
    }
}
