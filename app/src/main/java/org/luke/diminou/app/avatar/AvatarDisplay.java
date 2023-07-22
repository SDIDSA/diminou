package org.luke.diminou.app.avatar;

import android.graphics.drawable.GradientDrawable;

import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.components.layout.StackPane;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import androidx.annotation.ColorInt;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.property.Property;

public class AvatarDisplay extends StackPane implements Styleable {
    private final App owner;
    private final GradientDrawable background;

    private final Image img;
    private final GradientDrawable onlineBack;
    private final FrameLayout preOnline;
    private final Rectangle online;

    public static final int preSize = 64;

    private final ChangeListener<String> onUrl;

    private final ChangeListener<Boolean> onOnline;

    private User old;

    private ObjectConsumer<Boolean> onOnlineChanged;

    public AvatarDisplay(App owner, float sizeDp) {
        super(owner);
        this.owner = owner;

        int size = ViewUtils.dipToPx(sizeDp, owner);
        setLayoutParams(new LinearLayout.LayoutParams(size, size));

        background = new GradientDrawable();

        int radii = ViewUtils.dipToPx(7, owner);
        background.setCornerRadius(radii);
        setBackground(background);

        img = new Image(owner);
        img.setSize(sizeDp);
        ViewUtils.setPaddingUnified(img, 1, owner);
        img.setCornerRadius(10);

        float preOnlineSizeDp = sizeDp / 3f;
        int preOnlineSizePx = ViewUtils.dipToPx(preOnlineSizeDp, owner);
        float strokeWidthDp = preOnlineSizeDp / 4f;
        float onlineSizeDp = preOnlineSizeDp - 2 * strokeWidthDp;

        onlineBack = new GradientDrawable();
        onlineBack.setCornerRadius(preOnlineSizePx);

        preOnline = new FrameLayout(owner);
        preOnline.setBackground(onlineBack);
        preOnline.setLayoutParams(new LayoutParams(preOnlineSizePx, preOnlineSizePx));
        ViewUtils.alignInFrame(preOnline, Gravity.BOTTOM | Gravity.END);
        int by = ViewUtils.dipToPx(strokeWidthDp - 1, owner);
        preOnline.setTranslationY(by);
        preOnline.setTranslationX(by);

        online = new Rectangle(owner);
        online.setRadius(onlineSizeDp);
        online.setSize(onlineSizeDp, onlineSizeDp);
        ViewUtils.alignInFrame(online, Gravity.CENTER);

        preOnline.addView(online);

        addView(img);
        addView(preOnline);

        onUrl = (obs, ov, nv) -> ImageProxy.getImage(nv, img::setImageBitmap);
        onOnline = (obs, ov, nv) -> {
            applyStyle(owner.getStyle());
            if(onOnlineChanged != null) {
                try {
                    onOnlineChanged.accept(nv);
                } catch (Exception e) {
                    ErrorHandler.handle(e, "handling online change");
                }
            }
        };

        applyStyle(owner.getStyle());
    }

    public void setOnOnlineChanged(ObjectConsumer<Boolean> onOnlineChanged) {
        this.onOnlineChanged = onOnlineChanged;
    }

    public AvatarDisplay(App owner) {
        this(owner, preSize);
    }

    public Image getImg() {
        return img;
    }

    public void setOnClick(Runnable onClick) {
        img.setOnClick(onClick);
    }

    public void setValue(Avatar value) {
        img.setImageResource(value.getRes());
    }
    public void setUser(User user) {
        if(old != null) {
            old.avatarProperty().removeListener(onUrl);
            old.onlineProperty().removeListener(onOnline);
        }

        old = user;
        user.avatarProperty().addListener(onUrl);
        user.onlineProperty().addListener(onOnline);
    }

    public void setValue(String val) {
        setValue(Avatar.valueOf(val));
    }

    public void setOnlineBackground(@ColorInt int color) {
        onlineBack.setColor(color);
    }

    @Override
    public void applyStyle(Style style) {
        boolean isOnline = (old != null && old.isOnline());
        int borderColor = isOnline ? style.getTextPositive() : style.getTextMuted();
        preOnline.setVisibility(isOnline ? VISIBLE : INVISIBLE);

        background.setColor(style.getBackgroundPrimary());
        background.setStroke(ViewUtils.dipToPx(1, owner), borderColor);
        online.setFill(style.getTextPositive());
        onlineBack.setStroke(ViewUtils.dipToPx(1, owner), borderColor);
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
