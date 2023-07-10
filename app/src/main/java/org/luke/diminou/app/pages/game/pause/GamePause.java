package org.luke.diminou.app.pages.game.pause;

import android.view.Gravity;

import androidx.core.graphics.Insets;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
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

        themeSwitch.setPostChange(b ->
                Store.setTheme(b ? Style.THEME_DARK : Style.THEME_LIGHT, (s) ->
                    owner.applyTheme()));

        addOnShowing(() -> {
            if(owner.getStyle().get().isDark()) themeSwitch.enable();
            else themeSwitch.setIcon(R.drawable.sun);
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

        root.setTranslationY(ViewUtils.dipToPx(40, owner));

        addToShow(new ScaleXYAnimation(root, 1));
        addToShow(new TranslateYAnimation(root, 0));
        addToShow(new AlphaAnimation(root, 1));
        addToHide(new ScaleXYAnimation(root, .5f));
        addToHide(new TranslateYAnimation(root, ViewUtils.dipToPx(40, owner)));
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
