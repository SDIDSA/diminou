package org.luke.diminou.app.pages.home.online;

import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.settings.Settings;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

public class Home extends Page {
    private final VBox root;
    private final VBox content;
    private final HBox top;

    private final ColoredLabel username;

    private final Image pfp;
    private final Coins coins;

    public Home(App owner) {
        super(owner);

        top = new HBox(owner);
        top.setCornerRadius(15);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setElevation(ViewUtils.dipToPx(10, owner));

        pfp = new Image(owner);
        pfp.setSize(64);
        pfp.setCornerRadius(10);

        pfp.setOnClick(() -> {
            owner.pickImage(media -> {
                owner.toast(media.getName());
            });
        });

        ViewUtils.setMarginRight(pfp, owner, 10);

        username = new ColoredLabel(owner, "", Style::getTextNormal);
        username.setFont(new Font(18));

        coins = new Coins(owner);

        VBox info = new VBox(owner);
        info.setLayoutParams(new LinearLayout.LayoutParams(-2, ViewUtils.dipToPx(64, owner)));
        info.addView(username);
        info.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        info.addView(coins);

        ColoredIcon settings = new ColoredIcon(owner, Style::getTextNormal, R.drawable.settings);
        settings.setSize(48);
        settings.setOnClick(() -> owner.loadPage(Settings.class));
        ViewUtils.setPaddingUnified(settings, 7, owner);

        top.addView(pfp);
        top.addView(info);
        top.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        top.addView(settings);

        content = new VBox(owner);

        root = new VBox(owner);

        root.addView(top);
        root.addView(content);

        addView(root);
        applyStyle(owner.getStyle());
    }

    @Override
    public void setup() {
        super.setup();

        User user = owner.getUser();
        ImageProxy.getImage(user.getAvatar(), pfp::setImageBitmap);
        username.setText(user.getUsername());
        coins.setValue(user.getCoins());

        root.setAlpha(0f);
        root.setTranslationY(-ViewUtils.by(owner));

        new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(root, 1))
                .addAnimation(new TranslateYAnimation(root, 0))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void applyInsets(Insets insets) {
        int add = ViewUtils.dipToPx(10, owner);
        ViewUtils.setMargin(top, owner, 10, ViewUtils.pxToDip(insets.top, owner) + 10, 10, 10);
        top.setPadding(add, add, add, add);
        content.setPadding(add, add, add, insets.bottom + add);
    }

    @Override
    public void applyStyle(Style style) {
        top.setBackground(style.getBackgroundPrimary());
        content.setBackground(style.getBackgroundTertiary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}