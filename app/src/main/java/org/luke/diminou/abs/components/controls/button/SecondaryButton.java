package org.luke.diminou.abs.components.controls.button;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;

public class SecondaryButton extends ColoredButton {
    public SecondaryButton(App owner, String text) {
        super(owner, Style::getBackgroundTertiary, Style::getTextNormal, text);
    }
}
