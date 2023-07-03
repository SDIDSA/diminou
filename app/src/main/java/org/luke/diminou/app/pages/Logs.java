package org.luke.diminou.app.pages;

import android.view.View;
import android.widget.ScrollView;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.app.pages.home.HomeButtonPlay;
import org.luke.diminou.app.pages.settings.Settings;

public class Logs extends Titled {
    private final Label logs;
    private final ScrollView sv;

    public Logs(App owner) {
        super(owner, "Logs");

        content.setClipChildren(true);
        content.setClipToPadding(true);

        sv = new ScrollView(owner);
        sv.setClipChildren(true);
        sv.setClipToOutline(true);

        logs = new Label(owner, "");
        logs.setFont(new Font(10));

        sv.addView(logs);

        Button clear = new HomeButtonPlay(owner, "clear");

        getPreTitle().addView(clear);

        clear.setOnClick(() -> Store.setLogs("", this::setup));

        content.addView(sv);
    }

    @Override
    public void setup() {
        super.setup();

        logs.setText(Store.getLogs());

        Platform.runAfter(() -> sv.fullScroll(View.FOCUS_DOWN), 100);
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Settings.class);
        return true;
    }
}
