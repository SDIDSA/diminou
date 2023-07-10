package org.luke.diminou.abs.components.controls.image;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import org.luke.diminou.abs.components.layout.StackPane;

import androidx.annotation.DrawableRes;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.utils.ViewUtils;

public class Image extends androidx.appcompat.widget.AppCompatImageView {
    protected final App owner;
    private Runnable onClick;
    private Runnable onDoubleClick;

    private final GradientDrawable clip;

    public Image(App owner, @DrawableRes int res) {
        super(owner);
        this.owner = owner;

        setAdjustViewBounds(true);
        setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        clip = new GradientDrawable();
        setBackground(clip);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);

        if(res != -1) setImageResource(res);

        setFocusable(false);
    }

    private int res;
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        res = resId;
    }

    public int getRes() {
        return res;
    }

    public Orientation getOrientation() {
        return getWidth() > getHeight() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    @Override
    public void setBackgroundColor(int color) {
        clip.setColor(color);
    }

    public void setBorder(int color) {
        clip.setStroke(ViewUtils.dipToPx(1, owner), color);
    }

    public Image(App owner) {
        this(owner, -1);
    }

    public void setCornerRadius(float radius) {
        clip.setCornerRadius(ViewUtils.dipToPx(radius, owner));
    }

    private long lastClick = 0;
    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
        setFocusable(true);
        setOnClickListener(e -> {
            if(System.currentTimeMillis() - lastClick < 400) {
                fireDouble();
            }else {
                fire();
            }
            lastClick = System.currentTimeMillis();
        });
    }

    public void setOnDoubleClick(Runnable onDoubleClick) {
        this.onDoubleClick = onDoubleClick;
    }

    public void setHeight(float height) {
        getLayoutParams().height = Math.max(ViewUtils.dipToPx(height, owner), 0);
        requestLayout();
    }

    public void setWidth(float width) {
        getLayoutParams().width = Math.max(0, ViewUtils.dipToPx(width, owner));
        requestLayout();
    }

    public int width() {
        return getLayoutParams().width;
    }

    public int height() {
        return getLayoutParams().height;
    }

    public void center() {
        StackPane.LayoutParams params = new StackPane.LayoutParams(getLayoutParams().width,getLayoutParams().height);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
    }

    public void setSize(float size) {
        setWidth(size);
        setHeight(size);
    }

    public void fire() {
        if (onClick != null)
            onClick.run();
    }

    public void fireDouble() {
        if (onDoubleClick != null)
            onDoubleClick.run();
    }

    public App getOwner() {
        return owner;
    }
}
