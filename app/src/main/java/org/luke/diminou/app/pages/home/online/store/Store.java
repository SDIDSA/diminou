package org.luke.diminou.app.pages.home.online.store;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;

public class Store extends HomeFragment {
    public Store(App owner) {
        super(owner);

        setGravity(Gravity.CENTER);
        addView(new ColoredLabel(owner, "Store fragment", Style::getTextNormal));
    }
}