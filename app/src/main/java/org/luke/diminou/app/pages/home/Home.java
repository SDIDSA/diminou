package org.luke.diminou.app.pages.home;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.combine.SequenceAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.input.InputField;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.avatar.AvatarSelect;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.host.Host;
import org.luke.diminou.app.pages.join.Join;
import org.luke.diminou.app.pages.settings.Settings;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;

public class Home extends Page {
    private boolean destroyed = false;
    private final ColorIcon diminou;
    private final VBox root;
    public static boolean settingUp = true;

    private final InputField username;

    private final HomeButton settings;
    private final HomeButton exit;

    private final HBox play, profile;

    private final FrameLayout effects;

    public Home(App owner) {
        super(owner);

        effects = new FrameLayout(owner);
        effects.setLayoutDirection(LAYOUT_DIRECTION_LTR);
        addView(effects);

        if(settingUp)
            setEffects();

        diminou = new ColorIcon(owner, R.drawable.diminou);
        diminou.setWidth(250);
        ViewUtils.setMarginBottom(diminou, owner, 80);

        play = new HBox(owner);
        play.setClipToPadding(false);
        play.setGravity(Gravity.CENTER);

        profile = new HBox(owner);
        profile.setClipToPadding(false);
        profile.setGravity(Gravity.CENTER);
        profile.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewUtils.dipToPx(255, owner),
                        ViewUtils.dipToPx(AvatarDisplay.preSize, owner)));

        AvatarSelect avatar = new AvatarSelect(owner);
        ViewUtils.setMarginRight(avatar, owner, 15);

        username = new InputField(owner, "displayed_name");
        username.setFont(new Font(18));
        username.setValue(Store.getUsername());
        username.valueProperty().addListener((obs, ov, nv) -> Store.setUsername(nv, null));
        ViewUtils.spacer(username);


        HomeButton join = new HomeButtonPlay(owner, "join");
        ViewUtils.setMarginRight(join, owner, 15);
        HomeButton host = new HomeButtonPlay(owner, "host");
        settings = new HomeButton(owner, "settings");
        exit = new HomeButton(owner, "exit");

        play.addView(join);
        play.addView(host);

        profile.addView(avatar);
        profile.addView(username);

        root = new VBox(owner);
        root.setSpacing(15);
        root.setGravity(Gravity.CENTER);

        root.addView(diminou);
        root.addView(profile);
        root.addView(play);
        root.addView(settings);
        root.addView(exit);

        ConfirmExit confirmExit = new ConfirmExit(owner);

        join.setOnClick(() -> owner.loadPage(Join.class));
        //join.setOnClick(() -> owner.loadPage(Game.class));
        host.setOnClick(() -> owner.loadPage(Host.class));
        settings.setOnClick(() -> owner.loadPage(Settings.class));
        exit.setOnClick(confirmExit::show);

        addView(root);

        applyStyle(owner.getStyle());
    }

    @Override
    public void setup() {
        super.setup();
        destroyed = false;

        if(!settingUp) {
            effects.removeAllViews();
        }

        int by = ViewUtils.dipToPx(30, owner);

        hide(diminou, -by);
        hide(profile, -by);
        hide(play, -by);
        hide(settings, 0);
        hide(exit, by);

        floating.forEach(p -> removeView(p.getImg()));
        floating.clear();

        Platform.waitWhile(() -> settingUp, () -> {
            effects.removeAllViews();
            SequenceAnimation show = new SequenceAnimation(400)
                    .addAnimation(show(profile))
                    .addAnimation(show(play))
                    .addAnimation(show(diminou))
                    .addAnimation(show(settings))
                    .addAnimation(show(exit))
                    .setDelay(-300)
                    .setInterpolator(Interpolator.OVERSHOOT);
            show.start();
            Platform.runAfter(this::setupFloatingPieces, 400);
        });


    }

    private void hide(View view, int by) {
        view.setAlpha(0);
        view.setTranslationY(by);
        view.setScaleX(.8f);
        view.setScaleY(.8f);
    }

    private ParallelAnimation show(View view) {
        return new ParallelAnimation(400)
                .addAnimation(new TranslateYAnimation(view, 0))
                .addAnimation(new AlphaAnimation(view, 1))
                .addAnimation(new ScaleXYAnimation(view, 1));
    }

    @Override
    public boolean onBack() {
        exit.fire();
        return true;
    }

    @Override
    public void applyInsets(Insets insets) {
        root.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }
    private void setEffects() {
        VBox all = new VBox(owner);
        effects.addView(all);
        all.setPivotX(0);
        all.setPivotY(0);
        all.setSpacing(15);
        all.setAlpha(.4f);

        ArrayList<HBox> top = new ArrayList<>();
        ArrayList<HBox> bottom = new ArrayList<>();

        ArrayList<ColorIcon> left = new ArrayList<>();
        ArrayList<ColorIcon> right = new ArrayList<>();

        ArrayList<ColorIcon> center = new ArrayList<>();

        int rows = owner.getScreenHeight() / ViewUtils.dipToPx(60, owner);
        int cols = owner.getScreenWidth() / ViewUtils.dipToPx(55, owner);

        for (int i = 0; i < rows; i++) {
            HBox row = new HBox(owner);
            if(i < rows / 2) {
                top.add(row);
            }else {
                bottom.add(row);
            }
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dipToPx(110, owner)));
            row.setTranslationY(-i * ViewUtils.dipToPx(46, owner) + ViewUtils.dipToPx(-90, owner));
            for (int j = 0; j < cols; j++) {
                ColorIcon img = Piece.random().getImage(owner, 50);
                img.setRotation(45);
                img.setTranslationX(ViewUtils.dipToPx(30 + j * 29 - 69, owner));
                img.setTranslationY(ViewUtils.dipToPx(4f, owner));
                row.addView(img);

                if(j < cols / 2) {
                    left.add(img);
                }else if(j * 2 >= cols){
                    right.add(img);
                }else {
                    center.add(img);
                }
            }
            all.addView(row);
        }

        ParallelAnimation show = new ParallelAnimation(600)
                .setInterpolator(Interpolator.EASE_OUT);

        for(HBox row : top) {
            float old = row.getTranslationY();
            row.setTranslationY(old - owner.getScreenHeight() / 1.5f);
            show.addAnimation(new TranslateYAnimation(row, old));
        }

        for(HBox row : bottom) {
            float old = row.getTranslationY();
            row.setTranslationY(old + owner.getScreenHeight() / 1.5f);
            show.addAnimation(new TranslateYAnimation(row, old));
        }

        ParallelAnimation hide = new ParallelAnimation(600)
                .setInterpolator(Interpolator.EASE_IN);

        for(ColorIcon icon : left) {
            float old = icon.getTranslationX();
            hide.addAnimation(new TranslateXAnimation(icon, old - owner.getScreenWidth() / 1.5f));
        }

        for(ColorIcon icon : right) {
            float old = icon.getTranslationX();
            hide.addAnimation(new TranslateXAnimation(icon, old + owner.getScreenWidth() / 1.5f));
        }

        if(!center.isEmpty()) {
            for(ColorIcon icon : center) {
                hide.addAnimation(new AlphaAnimation(icon, 0)).addAnimation(new ScaleXYAnimation(icon, .7f));
            }
        }

        hide.setOnFinished(() -> {removeView(all); settingUp = false;});

        show.setOnFinished(() -> Platform.runAfter(hide::start, 1000));
        Platform.runAfter(show::start, 500);
    }

    private final ArrayList<FloatingPiece> floating = new ArrayList<>();
    private Thread floatingThread;
    private void setupFloatingPieces() {
        effects.removeAllViews();
        if(floatingThread != null && floatingThread.isAlive()) floatingThread.interrupt();
        floatingThread = new Thread(() -> {
            for(int i = 0; i < 6; i++) {
                createPiece();
                Platform.sleep(100);
            }
            while(!destroyed && !Thread.currentThread().isInterrupted()) {
                Platform.sleep(4000);
                if(destroyed || Thread.currentThread().isInterrupted()) return;
                try {
                    replacePiece(floating.get(0));
                }catch(Exception x) {
                    return;
                }
            }
        }, "floating_pieces_thread");
        floatingThread.start();
    }

    private void replacePiece(FloatingPiece toRemove) {
        toRemove.hide(() -> {
            Platform.runLater(() -> effects.removeView(toRemove.getImg()));
            floating.remove(toRemove);
            createPiece();
        });
    }

    private void createPiece() {
        FloatingPiece piece = new FloatingPiece(owner);
        floating.add(piece);
        Platform.runLater(() -> effects.addView(piece.getImg(), 0));
        piece.getImg().setOnClick(() -> replacePiece(piece));
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyed = true;
    }

    @Override
    public void applyStyle(Style style) {
        diminou.setFill(style.getTextNormal());
        username.setBorderColor(style.getTextMuted());
        owner.setBackgroundColor(style.getBackgroundTertiary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
