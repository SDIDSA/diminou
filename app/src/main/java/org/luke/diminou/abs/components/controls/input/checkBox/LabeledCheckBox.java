package org.luke.diminou.abs.components.controls.input.checkBox;

import android.view.Gravity;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class LabeledCheckBox extends HBox implements Styleable {

    private final CheckBox checkBox;
    private final Label label;
    public LabeledCheckBox(App owner, String text) {
        super(owner);
        setGravity(Gravity.CENTER);
        setPadding(7);

        checkBox = new CheckBox(owner);
        label = new Label(owner, text);
        label.setFont(new Font(16));

        addView(label);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(checkBox);

        setOnClickListener(e -> setChecked(!isChecked()));

        applyStyle(owner.getStyle());
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    @Override
    public void applyStyle(Style style) {
        label.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
