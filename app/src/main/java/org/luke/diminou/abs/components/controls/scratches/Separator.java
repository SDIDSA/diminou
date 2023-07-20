package org.luke.diminou.abs.components.controls.scratches;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;

public class Separator extends View {
    private final App owner;

    private Orientation orientation;
    private final float margin;

    public Separator(App owner, Orientation orientation, float margin) {
        super(owner);
        this.owner = owner;

        setAlpha(.4f);

        this.orientation = orientation;
            this.margin = margin;
        apply();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        apply();
    }

    private void apply() {
        boolean isVert = orientation == Orientation.VERTICAL;
        int oneDp = ViewUtils.dipToPx(1, owner);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(isVert ? oneDp : -1,isVert ? -1 : oneDp);

        int marginPx = ViewUtils.dipToPx(margin, owner);
        params.leftMargin = isVert ? 0 : marginPx;
        params.rightMargin = params.leftMargin;
        params.topMargin = isVert ? marginPx : 0;
        params.bottomMargin = params.topMargin;

        setLayoutParams(params);
    }

    public void setColor(@ColorInt int color) {
        setBackgroundColor(color);
    }


}
