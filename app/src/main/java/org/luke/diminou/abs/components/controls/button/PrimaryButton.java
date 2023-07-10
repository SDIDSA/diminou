package org.luke.diminou.abs.components.controls.button;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;

public class PrimaryButton extends ColoredButton {
    public PrimaryButton(App owner, String text) {
        super(owner, Style::getBackgroundPrimary, Style::getTextNormal, text);
    }
}
