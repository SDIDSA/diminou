package org.luke.diminou.app.pages.settings;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.StringConsumer;
import org.luke.diminou.abs.utils.functional.StringSupplier;

public class SettingOverlay extends MultipleOptionOverlay {
    public SettingOverlay(App owner, String key, StringSupplier get, StringConsumer set, String...options) {
        super(owner, "set_" + key, s -> get.get().equalsIgnoreCase(s));

        for(String option : options) {
            addButton(option, () -> {
                try {
                    owner.playMenuSound(R.raw.select);
                    set.accept(option);
                    Platform.runLater(() ->
                            applyStyle(owner.getStyle()));
                } catch (Exception e) {
                    ErrorHandler.handle(e, "setting " + key + " to " + option);
                }
            });
        }
    }
}
