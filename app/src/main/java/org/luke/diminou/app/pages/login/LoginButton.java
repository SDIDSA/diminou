package org.luke.diminou.app.pages.login;

import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.button.ColoredButton;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;

public class LoginButton extends ColoredButton {
    public LoginButton(App owner, String text, @DrawableRes int iconRes) {
        super(owner, Style::getTextNormal, Style::getBackgroundPrimary, text);

        ColoredIcon icon = new ColoredIcon(owner,
                Style::getBackgroundPrimary,
                iconRes);
        icon.setSize(24);
        addPostLabel(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addPostLabel(icon);
    }
}
