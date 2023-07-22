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

import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.utils.ViewUtils;

public class Button extends StackPane {
    private final App owner;
    private final GradientDrawable background;
    private final RippleDrawable ripple;
    private final Label label;
    private Runnable onClick;

    protected final HBox content;

    private final Loading loading;

    public Button(App owner, String text) {
        super(owner);
        this.owner = owner;
        background = new GradientDrawable();
        setRadius(7);
        ripple = new RippleDrawable(ColorStateList.valueOf(Color.TRANSPARENT), background, background);
        setBackground(ripple);
        ViewUtils.setPaddingUnified(this, 15, owner);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        label = new Label(owner, text);
        label.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        label.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));

        content = new HBox(owner);
        content.setGravity(Gravity.CENTER);
        ViewUtils.alignInFrame(content, Gravity.CENTER);

        content.addView(label);

        loading = new Loading(owner, 10);
        addView(content);

        setOnTouchListener((view, event) -> {
            if (!isClickable()) {
                return true;
            }
            if (view != this) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    ripple.setHotspot(event.getX(), event.getY());
                    setPressed(true);
                }
                case MotionEvent.ACTION_UP -> {
                    Rect rect = new Rect();
                    getHitRect(rect);
                    if (event.getX() > 0 && event.getX() < rect.width() &&
                            event.getY() > 0 && event.getY() < rect.height()) {
                        performClick();
                    }
                    setPressed(false);
                }
            }
            return true;
        });

        setFont(new Font(16));

        setOnClickListener(e -> {
            if (onClick != null) {
                onClick.run();
            }
        });

        setClickable(true);
        setFocusable(false);
    }

    public void startLoading() {
        getLayoutParams().height = getHeight();
        setClickable(false);
        loading.startLoading();
        removeAllViews();
        addView(loading);
    }

    public void stopLoading() {
        setClickable(true);
        removeAllViews();
        addView(content);
        loading.stopLoading();
    }

    public void setLetterSpacing(float spacing) {
        label.setLetterSpacing(spacing);
    }

    public void addPostLabel(View view) {
        content.addView(view);
    }

    private void setRippleColor(int touchColor) {
        ripple.setColor(ColorStateList.valueOf(touchColor));
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
        setRippleColor(App.adjustAlpha(getComplementaryColor(owner.getStyle().get().getBackgroundPrimary()), .6f));
    }

    public void setTransformationMethod(TransformationMethod method) {
        label.setTransformationMethod(method);
    }

    public void setTextFill(int color) {
        label.setTextColor(color);
        loading.setFill(color);
    }

    public void setFont(Font font) {
        label.setFont(font);
    }

    public static int getComplementaryColor(int color) {
        double y = (299f * Color.red(color) + 587f * Color.green(color) + 114f * Color.blue(color)) / 1000f;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }


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

    public String getText() {
        return label.getText().toString();
    }

    public void setKey(String name) {
        label.setKey(name);
    }

    public void setDisabled(boolean b) {
        setEnabled(!b);
    }
}