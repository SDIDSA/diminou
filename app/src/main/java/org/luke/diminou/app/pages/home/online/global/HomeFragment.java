package org.luke.diminou.app.pages.home.online.global;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.fragment.Fragment;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class HomeFragment extends Fragment implements Styleable {
    public HomeFragment(App owner) {
        super(owner);
        setSpacing(15);
        ViewUtils.setPaddingUnified(this, 10, owner);
        setClipToPadding(false);
        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getBackgroundTertiary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
