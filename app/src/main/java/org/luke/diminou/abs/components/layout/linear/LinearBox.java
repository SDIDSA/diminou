package org.luke.diminou.abs.components.layout.linear;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class LinearBox extends LinearLayout {
    private final Property<Float> spacing;
    private final App owner;

    private final GradientDrawable background;

    private final GradientDrawable foreground;

    public LinearBox(App owner) {
        super(owner);
        this.owner = owner;

        background = new GradientDrawable();
        foreground = new GradientDrawable();
        setBackground(background);
        setForeground(foreground);

        spacing = new Property<>(0f);
        spacing.addListener((obs, ov, nv) -> applySpacing(nv));
    }

    private void applySpacing(double spacing) {
        int rs = ViewUtils.dipToPx(spacing, owner);
        GradientDrawable divider = (GradientDrawable) ResourcesCompat.getDrawable(owner.getResources(), R.drawable.divider_shape,null);
        if(divider==null) {
            return;
        }
        if(this instanceof HBox) {
            divider.setSize(rs, 1);
        } else {
            divider.setSize(1, rs);
        }
        setDividerPadding(0);
        setDividerDrawable(divider);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }

    public void setSpacing(float spacing) {
        this.spacing.set(spacing);
    }

    public App getOwner() {
        return owner;
    }

    public void setPadding(float padding) {
        ViewUtils.setPaddingUnified(this, padding, owner);
    }

    public double getSpacing() {
        return spacing.get();
    }

    public void setBackground(int color) {
        background.setColor(color);
    }

    private int foregroundColor = Color.TRANSPARENT;
    public void setForeground(int color) {
        foreground.setColor(color);
        foregroundColor = color;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public void setCornerRadius(float radius) {
        background.setCornerRadius(ViewUtils.dipToPx(radius, owner));
        foreground.setCornerRadius(ViewUtils.dipToPx(radius, owner));
    }

    public void setCornerRadiusTop(float radius) {
        int val = ViewUtils.dipToPx(radius, owner);
        background.setCornerRadii(new float[]{
                val, val,
                val, val,
                0, 0,
                0, 0
        });
        foreground.setCornerRadii(new float[]{
                val, val,
                val, val,
                0, 0,
                0, 0
        });
    }

    public void setCornerRadiusBottom(float radius) {
        int val = ViewUtils.dipToPx(radius, owner);
        background.setCornerRadii(new float[]{
                0, 0,
                0, 0,
                val, val,
                val, val
        });
        foreground.setCornerRadii(new float[]{
                0, 0,
                0, 0,
                val, val,
                val, val
        });
    }

    public void setCornerRadiusRight(float radius) {
        int val = ViewUtils.dipToPx(radius, owner);
        background.setCornerRadii(new float[]{
                0, 0,
                val, val,
                val, val,
                0, 0
        });
        foreground.setCornerRadii(new float[]{
                0, 0,
                val, val,
                val, val,
                0, 0
        });
    }

    public void setCornerRadiusLeft(float radius) {
        int val = ViewUtils.dipToPx(radius, owner);
        background.setCornerRadii(new float[]{
                val, val,
                0, 0,
                0, 0,
                val, val
        });
        foreground.setCornerRadii(new float[]{
                val, val,
                0, 0,
                0, 0,
                val, val
        });
    }

    public void setBorderColor(int color) {
        background.setStroke(ViewUtils.dipToPx(1, owner), color);
    }
}
