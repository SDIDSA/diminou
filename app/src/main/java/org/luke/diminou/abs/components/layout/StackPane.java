package org.luke.diminou.abs.components.layout;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;

public class StackPane extends FrameLayout {
    private final App owner;

    private final GradientDrawable background;

    private final GradientDrawable foreground;
    public StackPane(App owner) {
        super(owner);
        this.owner = owner;

        background = new GradientDrawable();
        foreground = new GradientDrawable();
        setBackground(background);
        setForeground(foreground);
    }

    public void setPadding(float padding) {
        ViewUtils.setPaddingUnified(this, padding, owner);
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

    @Override
    public void addView(View child) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.addView(child);
    }

    @Override
    public void removeView(View view) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.removeAllViews();
    }

    @Override
    public void addView(View child, int index) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"), "adding view to stackPane");
        super.addView(child, index);
    }
}
