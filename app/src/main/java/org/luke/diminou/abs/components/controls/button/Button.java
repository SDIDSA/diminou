package org.luke.diminou.abs.components.controls.button;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.method.TransformationMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.abs.ColoredView;
import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.font.FontWeight;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.BooleanProperty;

public class Button extends FrameLayout implements ColoredView {
    private final App owner;
    private final GradientDrawable background;
    private final RippleDrawable ripple;
    private final Loading loading;
    private final BooleanProperty disabled;
    private final Label label;
    private Runnable onClick;

    private @ColorInt int color;

    protected final HBox content;

    public Button(App owner, String text) {
        super(owner);
        this.owner = owner;
        background = new GradientDrawable();
        setRadius(7);
        ripple = new RippleDrawable(ColorStateList.valueOf(Color.TRANSPARENT), background, background);
        setBackground(ripple);
        ViewUtils.setPaddingUnified(this, 15, owner);

        disabled = new BooleanProperty(false);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        label = new Label(owner, text);
        label.setLayoutGravity(Gravity.CENTER);

        loading = new Loading(owner, 8);

        content = new HBox(owner);
        content.setGravity(Gravity.CENTER);

        content.addView(label);

        addView(content);

        loading.setColor(Color.WHITE);

        setOnTouchListener((view, event) -> {
            if (!isClickable()) {
                return true;
            }
            if (view != this) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ripple.setHotspot(event.getX(), event.getY());
                    setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    Rect rect = new Rect();
                    getHitRect(rect);
                    if (event.getX() > 0 && event.getX() < rect.width() &&
                            event.getY() > 0 && event.getY() < rect.height()) {
                        performClick();
                    }
                    setPressed(false);
                    break;
            }
            return true;
        });

        setFont(new Font(16));

        setOnClickListener(e -> {
            if (onClick != null) {
                onClick.run();
            }
        });

        disabled.addListener((obs, ov, nv) -> {
            setEnabled(!nv);
            setAlpha(nv ? .6f : 1f);
        });

        setClickable(true);
        setFocusable(false);
    }

    public void setLetterSpacing(float spacing) {
        label.setLetterSpacing(spacing);
    }

    public void setKey(String key) {
        label.setKey(key);
    }

    public void addPreLabel(View view) {
        content.addView(view, 0);
    }

    public void addPostLabel(View view) {
        content.addView(view);
    }

    private void setRippleColor(int touchColor) {
        ripple.setColor(ColorStateList.valueOf(touchColor));
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    public void startLoading() {
        if (indexOfChild(loading) == -1)
            addView(loading, 0);

        loading.startLoading();
        label.setAlpha(0);
        setClickable(false);
    }

    public void stopLoading() {
        if (indexOfChild(loading) != -1)
            removeView(loading);

        loading.stopLoading();
        label.setAlpha(1);
        setClickable(true);
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
        setFocusable(true);
    }

    public void setRadius(float radius) {
        background.setCornerRadius(ViewUtils.dipToPx(radius, owner));
    }

    public void setBackgroundColor(int color) {
        background.setColor(color);
        setRippleColor(App.adjustAlpha(getComplementaryColor(color), .6f));
        this.color = color;
    }

    public void setTransformationMethod(TransformationMethod method) {
        label.setTransformationMethod(method);
    }

    public void setBorder(float width, @ColorInt int color) {
        background.setStroke(ViewUtils.dipToPx(width, owner), color);
    }

    public void setTextFill(int color) {
        label.setTextColor(color);
    }

    public void setFont(Font font) {
        label.setFont(font);
    }

    public static int getComplementaryColor(int color) {
        double y = (299f * Color.red(color) + 587f * Color.green(color) + 114f * Color.blue(color)) / 1000f;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }


    @Override
    public int getFill() {
        return color;
    }

    @Override
    public void setFill(int fill) {
        setBackgroundColor(fill);
    }

    public void fire() {
        if(onClick != null)
            onClick.run();
    }

    public String getKey() {
        return label.getKey();
    }
}