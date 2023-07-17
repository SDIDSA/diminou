package org.luke.diminou.abs.components.controls.input;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.Property;

public class MinimalInputField extends StackPane implements Styleable {
    protected final EditText input;
    protected final App owner;
    private final GradientDrawable background;
    private final Property<String> value;
    private final HBox preInput;

    public MinimalInputField(App owner, String promptText) {
        super(owner);
        this.owner = owner;
        background = new GradientDrawable();
        setRadius(7);

        value = new Property<>("");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        input = new EditText(owner);
        input.setHint(promptText);
        input.setLayoutParams(params);
        input.setTypeface(Font.DEFAULT.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        input.setShowSoftInputOnFocus(true);
        input.setBackground(null);
        input.setMaxLines(1);
        input.setLines(1);
        input.setSingleLine(true);

        preInput = new HBox(owner);
        preInput.setVerticalGravity(Gravity.CENTER);
        preInput.addView(input);
        ViewUtils.setPadding(preInput, 15, 0, 0, 0, owner);

        addView(preInput);

        InputUtils.bindToProperty(input, value);

        setBackground(background);
        applyStyle(owner.getStyle());
    }

    public void addPostInput(View view) {
        preInput.addView(view);
    }

    public void setFont(Font font) {
        input.setTypeface(font.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
    }

    public Observable<String> valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        input.setText(value);
        input.setSelection(input.getText().length());
    }

    public void setRadius(float radius) {
        background.setCornerRadius(ViewUtils.dipToPx(radius, owner));
    }

    public void setBackgroundColor(int color) {
        background.setColor(color);
    }

    @Override
    public void setBackground(int color) {
        setBackgroundColor(color);
    }

    public void setBorderColor(int color) {
        background.setStroke(ViewUtils.dipToPx(1, owner), color);
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getBackgroundPrimary());
        setBorderColor(style.getTextMuted());

        input.setTextColor(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
