package org.luke.diminou.abs.components.controls.scratches;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.abs.ColoredView;
import org.luke.diminou.abs.utils.ViewUtils;

public class Separator extends View implements ColoredView {
    private final App owner;

    private Orientation orientation;
    private float margin;

    private @ColorInt int color;

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

    public void setMargin(float margin) {
        this.margin = margin;
        apply();
    }

    private void apply() {
        boolean isVert = orientation == Orientation.VERTICAL;
        int oneDp = ViewUtils.dipToPx(1, owner);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(isVert ? oneDp : -1,isVert ? -1 : oneDp);

        int marginPx = (int) ViewUtils.dipToPx(margin, owner);
        params.leftMargin = isVert ? 0 : marginPx;
        params.rightMargin = params.leftMargin;
        params.topMargin = isVert ? marginPx : 0;
        params.bottomMargin = params.topMargin;

        setLayoutParams(params);
    }

    public void setColor(@ColorInt int color) {
        setBackgroundColor(color);
        this.color = color;
    }


    @Override
    public int getFill() {
        return color;
    }

    @Override
    public void setFill(int fill) {
        setColor(fill);
    }
}
