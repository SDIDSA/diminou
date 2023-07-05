package org.luke.diminou.abs.components.layout.overlay;

import androidx.core.graphics.Insets;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;

public abstract class PartialSlideOverlay extends SlideOverlay {
    public PartialSlideOverlay(App owner, double heightFactor) {
        super(owner);
        list.setCornerRadius(10);
        setElevation(ViewUtils.dipToPx(20, owner));
        setHeightFactor(heightFactor);
    }

    public PartialSlideOverlay(App owner, int height) {
        super(owner);
        list.setCornerRadius(10);
        setElevation(ViewUtils.dipToPx(20, owner));
        setHeight(height);
    }

    @Override
    public void setHeightFactor(double heightFactor) {
        super.setHeightFactor(heightFactor);
    }

    @Override
    public final void applySystemInsets(Insets insets) {
        //IGNORE
    }

    @Override
    protected void setHeight(int height) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        int marg = ViewUtils.dipToPx(15, owner);
        params.bottomMargin = owner.getSystemInsets().bottom + marg;
        params.rightMargin = marg;
        params.leftMargin = marg;
        list.setLayoutParams(params);
    }
}
