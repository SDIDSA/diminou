package org.luke.diminou.app.pages.settings;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.locale.Locale;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.Logs;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.home.offline.OfflineHome;
import org.luke.diminou.app.pages.home.online.Home;

import java.util.ArrayList;

public class Settings extends Titled {
    private final ArrayList<SettingsGroup> groups;

    private final SettingsGroup game;
    public Settings(App owner) {
        super(owner, "settings");

        groups = new ArrayList<>();

        game = new SettingsGroup(owner, "game_settings");
        SettingsGroup display = new SettingsGroup(owner, "display_settings");
        SettingsGroup sound = new SettingsGroup(owner, "sound_settings");

        game.addSetting(new Setting(owner, "4_players_mode", Store::getFourMode,
                v -> Store.setFourMode(v, null), false, FourMode.names()));

        game.addSetting(new Setting(owner, "play_timer", Store::getTimer,
                v -> Store.setTimer(v, null), false, Timer.names()));

        display.addSetting(new Setting(owner, "app_theme", Store::getTheme,
                v -> Store.setTheme(v, s -> Platform.runAfter(() -> {
                    owner.applyTheme();
                    owner.reloadPage();
                }, 300)), false,
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

        addGroup(display);
        addGroup(sound);

        ColoredIcon logs = new ColoredIcon(owner, Style::getTextNormal, R.drawable.logs);
        logs.setSize(32);
        logs.setOnClick(() -> owner.loadPage(Logs.class));
        getPreTitle().addView(logs);
    }

    private void addGroup(SettingsGroup group) {
        content.addView(group);
        groups.add(group);
    }

    private void removeGroup(SettingsGroup group) {
        content.removeView(group);
        groups.remove(group);
    }

    private SettingsGroup getForKey(String key) {
        for(SettingsGroup group : groups) {
            if(group.getKey().equals(key)) return group;
        }

        return null;
    }

    @Override
    public void setup() {
        super.setup();
        removeGroup(game);
        if(!owner.isOnline()) {
            addGroup(game);
        }
        SettingsGroup open = getForKey((String) owner.getData("open_cat"));
        if(open != null) Platform.runBack(() -> {
            while(!open.isLaidOut()) {
                Platform.sleep(5);
            }
            Platform.runLater(open::open);
        });
        owner.putData("open_cat", null);
    }

    @Override
    public boolean onBack() {
        SettingsGroup open = SettingsGroup.openGroup;
        if(SettingsGroup.openGroup != null)
            SettingsGroup.openGroup.close();
        owner.loadPage(owner.isOnline() ? Home.class : OfflineHome.class);
        if(open != null)
            owner.putData("open_cat", open.getKey());
        return true;
    }
}
