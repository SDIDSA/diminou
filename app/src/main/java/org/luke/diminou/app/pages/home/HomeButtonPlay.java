package org.luke.diminou.app.pages.home;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.font.Font;

public class HomeButtonPlay extends HomeButton {
    public HomeButtonPlay(App owner, String text) {
        super(owner, text, 120);
        setFont(new Font(18));
    }
}
