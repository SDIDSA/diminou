package org.luke.diminou.app.pages.game.pause;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Gravity;
import android.widget.ImageView;

import androidx.core.graphics.Insets;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.button.SecondaryButton;
import org.luke.diminou.abs.components.controls.input.Switch;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public class GamePause extends Overlay implements Styleable {
    private final VBox root;

    private Runnable onExit;

    private final ToggleIcon music, game, other;

    public GamePause(App owner) {
        super(owner);

        root = new VBox(owner);
        root.setPadding(15);
        root.setCornerRadius(10);
        root.setMinimumHeight(500);
        root.setLayoutParams(new LayoutParams(-1, -2));
        root.setGravity(Gravity.BOTTOM);

        Button sound = new SecondaryButton(owner, "sound");

        music = new ToggleIcon(owner, R.drawable.music);
        game = new ToggleIcon(owner, R.drawable.khabet_static);
        other = new ToggleIcon(owner, R.drawable.menu_sounds);

        music.setOnChange(owner::applyAmbient);
        game.setOnChange(b -> Store.setGameSounds(b ? "on" : "off", null));
        other.setOnChange(b -> Store.setMenuSounds(b ? "on" : "off", null));

        addOnShowing(() -> {
            music.apply(Store.getAmbient());
            game.apply(Store.getGameSounds());
            other.apply(Store.getMenuSounds());
        });

        sound.addPostLabel(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        sound.addPostLabel(music);
        sound.addPostLabel(game);
        sound.addPostLabel(other);
        ViewUtils.setMarginRight(music, owner, 10);
        ViewUtils.setMarginRight(game, owner, 10);

        Button theme = new SecondaryButton(owner, "app_theme");

        Switch themeSwitch = new Switch(owner, 32);

        themeSwitch.setOnChange(b -> themeSwitch.setIcon(b ? R.drawable.moon : R.drawable.sun));

        themeSwitch.setPostChange(b -> {
            Bitmap shot = owner.screenCap();
            ImageView view = new ImageView(owner);
            view.setImageBitmap(shot);
            view.setLayoutParams(new LayoutParams(owner.getScreenWidth(), owner.getScreenHeight()));
            owner.getRoot().addView(view);


            Rect clip = new Rect(0, 0, owner.getScreenWidth(), owner.getScreenHeight());
            view.setClipBounds(clip);

            new ValueAnimation(400, b ? 0 : owner.getScreenWidth(), b ? owner.getScreenWidth() : 0) {
                @Override
                public void updateValue(float v) {
                    if(b) clip.left = (int) v;
                    else clip.right = (int) v;
                    view.setClipBounds(clip);
                }
            }
                    .setOnFinished(() -> owner.getRoot().removeView(view))
            .setInterpolator(Interpolator.EASE_BOTH).start();

            Store.setTheme(b ? Style.THEME_DARK : Style.THEME_LIGHT, (s) ->
                    owner.applyTheme());


        });


        theme.addPostLabel(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        theme.addPostLabel(themeSwitch);

        Button exit = new SecondaryButton(owner, "leave");
        exit.setOnClick(() -> {
            if(onExit != null)
                onExit.run();
        });
        root.addView(theme);
        root.addView(sound);
        root.addView(exit);

        ViewUtils.setMarginTop(sound, owner, 15);
        ViewUtils.setMarginTop(exit, owner, 15);

        setClipToPadding(false);
        ViewUtils.setPaddingUnified(this, 15, owner);

        root.setScaleX(.5f);
        root.setScaleY(.5f);
        root.setAlpha(0);

        addToShow(new ScaleXYAnimation(root, 1));
        addToShow(new AlphaAnimation(root, 1));
        addToHide(new ScaleXYAnimation(root, .5f));
        addToHide(new AlphaAnimation(root, 0));

        addOnShowing(() -> owner.playMenuSound(R.raw.swap));

        ViewUtils.alignInFrame(root, Gravity.CENTER);

        addView(root);

        applyStyle(owner.getStyle());
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    @Override
    public void applySystemInsets(Insets insets) {

    }

    @Override
    public void applyStyle(Style style) {
        root.setBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
