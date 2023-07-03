package org.luke.diminou.abs.components.controls.input;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.FontSizeAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.font.FontWeight;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.observable.StringObservable;
import org.luke.diminou.data.property.Property;
import org.luke.diminou.data.property.StringProperty;

public class InputField extends FrameLayout implements Input, Styleable {
    protected final EditText input;
    protected final App owner;
    private final HBox preInput;
    private final GradientDrawable background;
    private final StringProperty value;

    private final Label prompt;
    private final Label errorLabel;
    private final FrameLayout prompts;
    private final ParallelAnimation focus, unfocus;
    private final ParallelAnimation showError, hideError;
    private final Animation timer;

    private final String key;
    private final ColorIcon clear;
    private boolean success = false;
    private boolean error = false;
    private ColorIcon showPassword = null;
    private boolean passShown = false;

    public InputField(App owner, String promptText) {
        this(owner, promptText, promptText);
    }

    public InputField(App owner, String promptText, String key) {
        super(owner);
        this.key = key;
        this.owner = owner;
        background = new GradientDrawable();
        setRadius(7);

        value = new StringProperty();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        prompt = new Label(owner, promptText);
        prompt.setFont(new Font(14f, FontWeight.MEDIUM));
        prompt.setMaxLines(1);
        prompt.setLines(1);

        errorLabel = new Label(owner, "");
        errorLabel.setFont(new Font(14f, FontWeight.MEDIUM));
        errorLabel.setMaxLines(1);
        errorLabel.setLines(1);

        int errorBy = -ViewUtils.dipToPx(30, owner);
        errorLabel.setTranslationY(-errorBy);
        errorLabel.setAlpha(0);

        showError = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(errorLabel, 1))
                .addAnimation(new TranslateYAnimation(errorLabel, 0))
                .addAnimation(new AlphaAnimation(prompt, 0))
                .addAnimation(new TranslateYAnimation(prompt, errorBy))
                .setInterpolator(Interpolator.EASE_OUT);

        hideError = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(errorLabel, 0))
                .addAnimation(new TranslateYAnimation(errorLabel, -errorBy))
                .addAnimation(new AlphaAnimation(prompt, 1))
                .addAnimation(new TranslateYAnimation(prompt, 0))
                .setInterpolator(Interpolator.EASE_OUT);

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

        preInput = new HBox(owner);
        preInput.setVerticalGravity(Gravity.CENTER);
        preInput.addView(input);
        ViewUtils.setPadding(preInput, 15, 0, 15, 0, owner);

        prompts = new FrameLayout(owner);
        prompts.setAlpha(.5f);
        prompts.setClickable(false);
        prompts.setFocusable(false);
        ViewUtils.setPadding(prompts, 15, 20, 15, 20, owner);

        prompts.addView(prompt);
        prompts.addView(errorLabel);

        addView(preInput);
        addView(prompts);

        timer = new ValueAnimation(2000, 0, 1) {
            @Override
            public void updateValue(float v) {
                //do nothing
            }
        }.setFps(5).setOnFinished(() -> {
            showError.stop();
            hideError.start();
        });

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
                unfocus.stop();
                focus.start();
            } else {
                focus.stop();
                unfocus.start();
            }
        });

        clear = new ColorIcon(owner, R.drawable.clear);
        clear.setHeight(20);
        clear.setWidth(20);
        clear.setVisibility(INVISIBLE);
        clear.setOnClick(() -> setValue(""));

        valueProperty().addListener((obs, ov, nv) -> {
            clear.setVisibility(nv.isEmpty() ? INVISIBLE : VISIBLE);
            clearError();
            if (ov != null && ov.isEmpty() && !nv.isEmpty()) {
                unfocus.stop();
                focus.start();
            }
        });

        addPost(clear);

        InputUtils.bindToProperty(input, value);

        setBackground(background);
        applyStyle(owner.getStyle());
    }

    public void setTextMinWidth(int width) {
        prompts.setLayoutParams(new LayoutParams(ViewUtils.dipToPx(width, owner), ViewGroup.LayoutParams.WRAP_CONTENT));
        preInput.setLayoutParams(new LayoutParams(ViewUtils.dipToPx(width, owner), ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    public void setFont(Font font) {
        input.setTypeface(font.getFont());
        prompt.setTypeface(font.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
        prompt.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
    }

    private void addPost(ColorIcon icon) {
        icon.setSize(35);
        icon.setFocusable(false);
        ViewUtils.setPaddingUnified(icon, 6, owner);
        preInput.addView(icon);
    }

    public void addPre(View view) {
        preInput.addView(view, 0);
    }

    public void removeClear() {
        preInput.removeView(clear);
    }

    public void setHidden(boolean hidden) {
        if (hidden) {
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            showPassword = new ColorIcon(owner, R.drawable.show_password);
            showPassword.setColor(Color.WHITE);
            addPost(showPassword);

            showPassword.setOnClick(() -> {
                int selection = input.getSelectionStart();
                if (passShown) {
                    showPassword.setImageResource(R.drawable.show_password);
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passShown = false;
                } else {
                    showPassword.setImageResource(R.drawable.hidden_password);
                    input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passShown = true;
                }
                input.setSelection(selection);
            });

            applyStyle(owner.getStyle().get());
        }
    }

    public StringObservable valueProperty() {
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

    public void setInputType(int inputType) {
        input.setInputType(inputType);
    }

    public void setRadius(float radius) {
        background.setCornerRadius(ViewUtils.dipToPx(radius, owner));
    }

    public void setBackgroundColor(int color) {
        background.setColor(color);
    }

    public void setBorderColor(int color) {
        background.setStroke(ViewUtils.dipToPx(1, owner), color);
    }

    public void setPromptText(String key) {
        prompt.setKey(key);
    }

    public String getKey() {
        return key;
    }

    @Override
    public void setError(String errorKey, String plus) {
        hideError.stop();

        showError.start();
        timer.start();

        error = true;
        errorLabel.setKey(errorKey);

        if (plus != null)
            errorLabel.addParam(0, plus);

        applyStyle(owner.getStyle());
    }

    public void setSuccess(String successKey, String plus) {
        hideError.stop();

        showError.start();
        timer.start();

        success = true;
        errorLabel.setKey(successKey);

        if (plus != null)
            errorLabel.addParam(0, plus);

        applyStyle(owner.getStyle());
    }

    public void setError(String errorKey) {
        setError(errorKey, null);
    }

    public void setSuccess(String successKey) {
        setSuccess(successKey, null);
    }

    public void clearError() {
        showError.stop();
        timer.stop();

        hideError.start();

        error = false;
        success = false;
        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getBackgroundPrimary());
        setBorderColor(style.getTextMuted());

        input.setTextColor(style.getTextNormal());
        @ColorInt int textColor = error ? style.getTextDanger() : success ? style.getTextPositive() : style.getChannelsDefault();
        prompt.setTextColor(textColor);
        errorLabel.setTextColor(textColor);

        clear.setColor(style.getChannelsDefault());

        if (showPassword != null) showPassword.setColor(style.getChannelsDefault());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
