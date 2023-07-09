package org.luke.diminou.abs.components.controls.button;

import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.transformationMethods.AllCaps;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;

public class HomeButton extends ColoredButton {
    public HomeButton(App owner, String text, int width) {
        super(owner, Style::getTextNormal, Style::getBackgroundPrimary, text);
        setTransformationMethod(new AllCaps());
        setLayoutParams(new LinearLayout.LayoutParams(ViewUtils.dipToPx(width, owner), ViewUtils.dipToPx(50, owner)));

        ViewUtils.setPaddingUnified(this, 0, owner);

        setFont(new Font(20));
        setLetterSpacing(.1f);

        setClipToOutline(false);
        setClipToPadding(false);
        applyStyle(owner.getStyle());
    }

    public HomeButton(App owner, String text) {
        this(owner, text, 255);
    }
}
