package org.luke.diminou.abs.components.layout.linear;

import android.widget.LinearLayout;

import org.luke.diminou.abs.App;

public class VBox extends LinearBox{
    public VBox(App owner) {
        super(owner);
        setOrientation(LinearLayout.VERTICAL);
    }
}
