package org.luke.diminou.abs.components.controls.shape;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.abs.ColoredView;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.property.Property;

public class Rectangle extends View implements ColoredView {
    private final GradientDrawable background;

    private final Property<Float> width;
    private final Property<Float> height;
    private final Property<Float> radius;
    private final Property<Integer> strokeWidth;

    private final Property<Integer> fill;
    private final Property<Integer> stroke;

    public Rectangle(App owner) {
        super(owner);
        background = new GradientDrawable();
        setBackground(background);

        width = new Property<>(0f);
        height = new Property<>(0f);
        radius = new Property<>(0f);
        strokeWidth = new Property<>(0);

        fill = new Property<>(Color.TRANSPARENT);
        stroke = new Property<>(Color.TRANSPARENT);

        ChangeListener<Float> sizeListener = (obs, ov, nv) -> {
            if (getParent() instanceof FrameLayout) {
                setLayoutParams(new FrameLayout.LayoutParams(ViewUtils.dipToPx(width.get(), owner), ViewUtils.dipToPx(height.get(), owner)));
            } else if(getParent() instanceof LinearLayout) {
                setLayoutParams(new LinearLayout.LayoutParams(ViewUtils.dipToPx(width.get(), owner), ViewUtils.dipToPx(height.get(), owner)));
            } else {
                setLayoutParams(new ViewGroup.LayoutParams(ViewUtils.dipToPx(width.get(), owner), ViewUtils.dipToPx(height.get(), owner)));
            }
        };
        ChangeListener<Integer> strokeListener = (obs, ov, nv) -> background.setStroke(ViewUtils.dipToPx(strokeWidth.get(), owner), stroke.get());

        width.addListener(sizeListener);
        height.addListener(sizeListener);
        radius.addListener((obs, ov, nv) -> background.setCornerRadius(ViewUtils.dipToPx(nv, owner)));
        strokeWidth.addListener(strokeListener);

        fill.addListener((obs, ov, nv) -> background.setColor(nv));
        stroke.addListener((strokeListener));
    }

    public Rectangle(App owner, float width, float height) {
        this(owner);

        setSize(width, height);
    }

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setWidth(float width) {
        this.width.set(width);
    }

    public void setHeight(float height) {
        this.height.set(height);
    }

    public void setRadius(float radius) {
        this.radius.set(radius);
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth.set(strokeWidth);
    }

    @Override
    public int getFill() {
        return fill.get();
    }

    @Override
    public void setFill(@ColorInt int fill) {
        this.fill.set(fill);
    }
}
