package org.luke.diminou.app.pages.settings;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.locale.Locale;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.Logs;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.Home;
import org.luke.diminou.app.pages.home.HomeButton;

public class Settings extends Titled {

    public Settings(App owner) {
        super(owner, "settings");

        /*


        content.addView(new ColoredSeparator(owner, Orientation.HORIZONTAL, 0, Style::getTextMuted));


         */

        SettingsGroup game = new SettingsGroup(owner, "game_settings");
        SettingsGroup display = new SettingsGroup(owner, "display_settings");
        SettingsGroup sound = new SettingsGroup(owner, "sound_settings");

        game.addSetting(new Setting(owner, "4_players_mode", Store::getFourMode,
                v -> Store.setFourMode(v, null), false, FourMode.names()));

        game.addSetting(new Setting(owner, "play_timer", Store::getTimer,
                v -> Store.setTimer(v, null), false, Timer.names()));

        display.addSetting(new Setting(owner, "app_theme", Store::getTheme,
                v -> Store.setTheme(v, s -> owner.applyTheme()), false,
                Style.THEME_SYSTEM, Style.THEME_DARK, Style.THEME_LIGHT));

        display.addSetting(new Setting(owner, "ui_scale",
                Store::getScale,
                v -> Store.setScale(v, s -> ViewUtils.scale = Float.parseFloat(s)),
                true, "0.75", "1.0", "1.25"));

        display.addSetting(new Setting(owner, "language", Store::getLanguage,
                v -> Store.setLanguage(v, s -> {
                    owner.setLocale(Locale.forName(v));
                    owner.reloadPage();
                }), false, "en_us", "fr_fr", "ar_ar"));

        sound.addSetting(new Setting(owner, "ambient_music", Store::getAmbient,
                v -> Store.setAmbient(v, s -> {
                    if(s.equals("on")) owner.unmuteAmbient();
                    else owner.muteAmbient();
                }), false, "on", "off"));

        sound.addSetting(new Setting(owner, "game_sounds", Store::getGameSounds,
                v -> Store.setGameSounds(v, null), false, "on", "off"));

        sound.addSetting(new Setting(owner, "other_sounds", Store::getMenuSounds,
                v -> Store.setMenuSounds(v, null), false, "on", "off"));

        content.addView(game);
        content.addView(display);
        content.addView(sound);

        /*
        Button logs = new HomeButton(owner, "logs", 100);
        logs.setOnClick(() -> owner.loadPage(Logs.class));

        getPreTitle().addView(logs);
         */
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }
}
