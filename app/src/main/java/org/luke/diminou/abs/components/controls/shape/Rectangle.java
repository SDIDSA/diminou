package org.luke.diminou.abs.components.controls.shape;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.abs.ColoredView;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.property.DoubleProperty;
import org.luke.diminou.data.property.IntegerProperty;

public class Rectangle extends View implements ColoredView {
    private final GradientDrawable background;

    private final DoubleProperty width;
    private final DoubleProperty height;
    private final DoubleProperty radius;
    private final IntegerProperty strokeWidth;

    private final IntegerProperty fill;
    private final IntegerProperty stroke;

    public Rectangle(App owner) {
        super(owner);
        background = new GradientDrawable();
        setBackground(background);

        width = new DoubleProperty(0.0);
        height = new DoubleProperty(0.0);
        radius = new DoubleProperty(0.0);
        strokeWidth = new IntegerProperty(0);

        fill = new IntegerProperty(Color.TRANSPARENT);
        stroke = new IntegerProperty(Color.TRANSPARENT);

        ChangeListener<Double> sizeListener = (obs, ov, nv) -> {
            if (getParent() instanceof FrameLayout) {
                setLayoutParams(new FrameLayout.LayoutParams(ViewUtils.dipToPx(width.get(), owner), ViewUtils.dipToPx(height.get(), owner)));
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

    public Rectangle(App owner, double width, double height) {
        this(owner);

        setSize(width, height);
    }

    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public void setRadius(double radius) {
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

    public void setStroke(@ColorInt int stroke) {
        this.stroke.set(stroke);
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    public IntegerProperty strokeWidthProperty() {
        return strokeWidth;
    }

    public IntegerProperty fillProperty() {
        return fill;
    }

    public IntegerProperty strokeProperty() {
        return stroke;
    }
}
