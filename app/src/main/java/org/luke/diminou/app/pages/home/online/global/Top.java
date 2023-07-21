package org.luke.diminou.app.pages.home.online.global;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.LinearHeightAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.button.SecondaryButton;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.components.controls.input.InputField;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.pages.settings.Settings;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.io.File;

public class Top extends VBox implements Styleable {
    private final InputField newUn;

    private final ColoredLabel username;

    private final AvatarDisplay pfp;
    private final Coins coins;

    private final Animation showEditUn, hideEditUn;
    public Top(App owner) {
        super(owner);

        HBox editun = new HomePanel(owner);
        editun.setLayoutParams(new LinearLayout.LayoutParams(-1, 0));
        editun.setAlpha(0);

        setClipChildren(false);

        newUn = new InputField(owner, "Enter Username");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, ViewUtils.dipToPx(60, owner));
        params.weight = 1;
        params.rightMargin = ViewUtils.dipToPx(10, owner);
        newUn.setLayoutParams(params);

        SecondaryButton saveUn = new SecondaryButton(owner, "Done");
        saveUn.setLayoutParams(
                new LinearLayout.LayoutParams(ViewUtils.dipToPx(100, owner), ViewUtils.dipToPx(60, owner)));

        editun.addView(newUn);
        editun.addView(saveUn);

        HBox top = new HomePanel(owner);


        pfp = new AvatarDisplay(owner, 64);

        ViewUtils.setMarginRight(pfp, owner, 10);

        username = new ColoredLabel(owner, "", Style::getTextNormal);
        username.setFont(new Font(16));

        ColoredIcon editUsername = new ColoredIcon(owner, Style::getTextNormal, R.drawable.edit);
        editUsername.setSize(16);

        ViewUtils.setMarginRight(username, owner, 10);

        HBox preUsername = new HBox(owner);
        preUsername.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        preUsername.setGravity(Gravity.CENTER_VERTICAL);
        preUsername.addView(username);
        preUsername.addView(editUsername);

        coins = new Coins(owner);

        VBox info = new VBox(owner);
        info.setLayoutParams(new LinearLayout.LayoutParams(-2, ViewUtils.dipToPx(64, owner)));
        info.addView(preUsername);
        info.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        info.addView(coins);

        ColoredIcon settings = new ColoredIcon(owner, Style::getTextNormal, R.drawable.settings);
        settings.setSize(48);
        ViewUtils.setPaddingUnified(settings, 7, owner);

        top.addView(pfp);
        top.addView(info);
        top.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        top.addView(settings);

        ViewUtils.setMargin(top, getOwner(), 10, 0, 10, 0);
        ViewUtils.setMargin(editun, getOwner(), 10, 10, 10, 10);

        addView(editun);
        addView(top);

        top.setTranslationY(-ViewUtils.dipToPx(10, owner));

        showEditUn = new ParallelAnimation(300)
                .addAnimation(new LinearHeightAnimation(editun,  ViewUtils.dipToPx(80, owner)))
                .addAnimation(new AlphaAnimation(editun, 1))
                .addAnimation(new TranslateYAnimation(top, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        hideEditUn = new ParallelAnimation(300)
                .addAnimation(new LinearHeightAnimation(editun, 0))
                .addAnimation(new AlphaAnimation(editun, 0))
                .addAnimation(new TranslateYAnimation(top, -ViewUtils.dipToPx(10, owner)))
                .setInterpolator(Interpolator.EASE_OUT);

        preUsername.setOnClickListener(e -> {
            newUn.setValue(username.getText().toString());
            saveUn.setKey("Cancel");
            hideEditUn.stop();
            showEditUn.start();
        });

        AvatarViewChange avatarViewChange = new AvatarViewChange(owner,
                () -> new AvatarOverlay(owner,
                        ((BitmapDrawable)pfp.getImg().getDrawable()).getBitmap()).show(),
                () -> owner.pickImage(media -> {
                    File f = ImageProxy.mediaToFile(owner, media);
                    Session.changeAvatar(f, res -> {
                        if(res.has("err")) {
                            owner.toast("Operation failed...");
                        }else {
                            owner.toast("Avatar changed");
                        }
                    });
                }));

        pfp.setOnClick(avatarViewChange::show);

        saveUn.setOnClick(() -> {
            owner.hideKeyboard();
            if(newUn.getValue().equals(username.getText().toString())) {
                hideEditUn.start();
            }else {
                saveUn.startLoading();
                Session.changeUsername(newUn.getValue(), res -> {
                    if(res.has("err")) {
                        owner.toast(res.getString("err"));
                    }else {
                        owner.toast("Username changed");
                    }
                    hideEditUn.start();
                    saveUn.stopLoading();
                });
            }
        });

        newUn.valueProperty().addListener((obs, ov, nv) -> {
            if(nv.equals(username.getText().toString())) {
                saveUn.setKey("Cancel");
            }else {
                saveUn.setKey("Save");
            }
        });

        settings.setOnClick(() -> owner.loadPage(Settings.class));

        applyStyle(owner.getStyle());
    }

    public void setup(User user) {
        pfp.setUser(user);

        user.usernameProperty().addListener((obs, ov, nv) -> username.setText(nv));

        user.coinsProperty().addListener((obs, ov, nv) -> coins.setValue(nv));
    }

    @Override
    public void applyStyle(Style style) {
        newUn.setBackgroundColor(style.getBackgroundTertiary());
        newUn.setBorderColor(Color.TRANSPARENT);
        pfp.setOnlineBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
