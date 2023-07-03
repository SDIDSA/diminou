package org.luke.diminou.abs.components.controls.input.checkBox;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class CheckBox extends FrameLayout implements Styleable {

    private final App owner;
    private final GradientDrawable background;

    private final ColorIcon checkMark;
    private boolean checked = false;

    public CheckBox(App owner) {
        super(owner);
        this.owner = owner;

        background = new GradientDrawable();
        background.setColor(Color.TRANSPARENT);
        background.setCornerRadius(ViewUtils.dipToPx(5, owner));

        setSize(20);

        setBackground(background);

        checkMark = new ColorIcon(owner, R.drawable.check);
        checkMark.setSize(20);

        addView(checkMark);

        setChecked(false);

        applyStyle(owner.getStyle());
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        setAlpha(checked ? 1 : .6f);
        checkMark.setAlpha(checked ? 1f : 0f);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setSize(float size) {
        int sizePx = ViewUtils.dipToPx(size, owner);

        setLayoutParams(new ViewGroup.LayoutParams(sizePx, sizePx));
    }

    @Override
    public void applyStyle(Style style) {
        background.setStroke(ViewUtils.dipToPx(2, owner), style.getTextNormal());
        checkMark.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
