package org.luke.diminou.abs.components.controls.input;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.FontSizeAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.font.FontWeight;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.Property;

public class InputField extends StackPane implements Styleable {
    protected final EditText input;
    protected final App owner;
    private final GradientDrawable background;
    private final Property<String> value;

    private final Label prompt;
    private final ParallelAnimation focus, unfocus;

    private Runnable onFocus, onFocusLost;

    public InputField(App owner, String promptText) {
        super(owner);
        this.owner = owner;
        background = new GradientDrawable();
        setRadius(7);

        value = new Property<>("");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        prompt = new Label(owner, promptText);
        prompt.setMaxLines(1);
        prompt.setLines(1);

        input = new EditText(owner);
        input.setLayoutParams(params);
        input.setTypeface(Font.DEFAULT.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        input.setShowSoftInputOnFocus(true);
        ViewUtils.setPadding(input, 0, 30, 0, 10, owner);
        input.setBackground(null);
        input.setMaxLines(1);
        input.setLines(1);
        input.setSingleLine(true);

        HBox preInput = new HBox(owner);
        preInput.setVerticalGravity(Gravity.CENTER);
        preInput.addView(input);
        ViewUtils.setPadding(preInput, 15, 0, 15, 0, owner);

        StackPane prompts = new StackPane(owner);
        prompts.setAlpha(.5f);
        prompts.setClickable(false);
        prompts.setFocusable(false);
        ViewUtils.setPadding(prompts, 15, 18, 15, 22, owner);

        prompts.addView(prompt);

        addView(preInput);
        addView(prompts);

        focus = new ParallelAnimation(200)
                .addAnimation(new FontSizeAnimation(prompt, 12))
                .addAnimation(new TranslateYAnimation(prompts, -ViewUtils.dipToPx(10, owner)))
                .addAnimation(new AlphaAnimation(prompts, 1f))
                .setInterpolator(Interpolator.EASE_OUT);

        unfocus = new ParallelAnimation(200)
                .addAnimation(new FontSizeAnimation(prompt, 16))
                .addAnimation(new TranslateYAnimation(prompts, 0))
                .addAnimation(new AlphaAnimation(prompts, .5f))
                .setInterpolator(Interpolator.EASE_OUT);

        input.setOnFocusChangeListener((view, focused) -> {
            if (getValue().length() > 0)
                return;

            if (focused) {
                if(onFocus != null) {
                    onFocus.run();
                }
                unfocus.stop();
                focus.start();
            } else {
                if(onFocusLost != null) {
                    onFocusLost.run();
                }
                focus.stop();
                unfocus.start();
            }
        });

        valueProperty().addListener((obs, ov, nv) -> {
            if (ov != null && ov.isEmpty() && !nv.isEmpty()) {
                unfocus.stop();
                focus.start();
            }
        });

        InputUtils.bindToProperty(input, value);

        setBackground(background);
        applyStyle(owner.getStyle());
    }

    public void setOnFocus(Runnable onFocus) {
        this.onFocus = onFocus;
    }

    public void setOnFocusLost(Runnable onFocusLost) {
        this.onFocusLost = onFocusLost;
    }

    public void setFont(Font font) {
        input.setTypeface(font.getFont());
        prompt.setTypeface(font.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
        prompt.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
    }

    public Observable<String> valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        if (input.getText().length() == 0 && !isFocused()) {
            unfocus.stop();
            focus.start();
        }
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
        prompt.setTextColor(style.getChannelsDefault());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
