package org.luke.diminou.abs.components.layout.linear;

import android.widget.LinearLayout;

import org.luke.diminou.abs.App;

public class HBox extends LinearBox {
    public HBox(App owner) {
        super(owner);
        setOrientation(LinearLayout.HORIZONTAL);
    }
}
