package org.luke.diminou.app.pages.host;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.input.checkBox.LabeledCheckBox;
import org.luke.diminou.abs.components.layout.overlay.MultipleOptionOverlay;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.app.pages.settings.Settings;

public class TeamModeOverlay extends MultipleOptionOverlay {
    private Runnable onDone;

    public TeamModeOverlay(App owner) {
        super(owner, "set_4_players_mode", s -> true);

        LabeledCheckBox remember = new LabeledCheckBox(owner, "dont_ask");

        addButton("team_mode", () -> {
            if (remember.isChecked()) {
                Store.setFourMode(FourMode.TEAM_MODE.getText(), () -> {
                    Page.clearCache(Settings.class);
                });
            }
            owner.putString("mode", FourMode.TEAM_MODE.getText());
            if (onDone != null) {
                onDone.run();
            }
            hide();
        });

        addButton("normal_mode", () -> {
            if (remember.isChecked()) {
                Store.setFourMode(FourMode.NORMAL_MODE.getText(), () -> {
                    Page.clearCache(Settings.class);
                });
            }
            owner.putString("mode", FourMode.NORMAL_MODE.getText());
            if (onDone != null) {
                onDone.run();
            }
            hide();
        });

        root.addView(remember);

        applyStyle(owner.getStyle().get());
    }

    public void setOnDone(Runnable onDone) {
        this.onDone = onDone;
    }

}
