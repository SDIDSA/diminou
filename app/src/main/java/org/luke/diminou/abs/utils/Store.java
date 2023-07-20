package org.luke.diminou.abs.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.functional.StringConsumer;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.app.pages.settings.Timer;

import java.util.concurrent.Semaphore;

public class Store {
    private static SharedPreferences settingsD;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String USERNAME = "username";
    private static final String AVATAR = "avatar";
    private static final String THEME = "theme";
    private static final String SCALE = "scale";
    private static final String LANGUAGE = "language";
    private static final String FOUR_MODE = "four_mode";
    private static final String TIMER = "timer";
    private static final String LOGS = "logs";
    private static final String AMBIENT = "ambient";
    private static final String MENU_SOUNDS = "menu_sounds";
    private static final String GAME_SOUNDS = "game_sounds";


    public static void init(App owner) {
        if(settingsD == null)
            settingsD = owner.getPreferences(Context.MODE_PRIVATE);
    }

    private static final Semaphore mutex = new Semaphore(1);
    private static String getSetting(String key, String def) {
        mutex.acquireUninterruptibly();
        String val = settingsD.getString(key, def);
        mutex.release();
        return val;
    }

    private static void setSetting(String key, String value, StringConsumer onSuccess) {
        Platform.runBack(() -> {
            mutex.acquireUninterruptibly();
            SharedPreferences.Editor editor = settingsD.edit();
            editor.putString(key, value);
            boolean success = editor.commit();
            mutex.release();
            if(success && onSuccess != null)
                Platform.runLater(() -> {
                    try {
                        onSuccess.accept(value);
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "storing data at " + key);
                    }
                });
        });
    }

    private static void removeSetting(String key, Runnable onSuccess) {
        Platform.runBack(() -> {
            mutex.acquireUninterruptibly();
            SharedPreferences.Editor editor = settingsD.edit();
            editor.remove(key);
            boolean success = editor.commit();
            mutex.release();
            if(success && onSuccess != null)
                Platform.runLater(() -> {
                    try {
                        onSuccess.run();
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "storing data at " + key);
                    }
                });
        });
    }

    public static String getAccessToken() {
        return getSetting(ACCESS_TOKEN, "");
    }

    public static void setAccessToken(String token, StringConsumer onSuccess) {
        setSetting(ACCESS_TOKEN, token, onSuccess);
    }

    public static void removeAccessToken(Runnable onSuccess) {
        removeSetting(ACCESS_TOKEN, onSuccess);
    }

    public static void setTheme(String value, StringConsumer onSuccess) {
        setSetting(THEME, value, onSuccess);
    }

    public static String getTheme() {
        return getSetting(THEME, Style.THEME_SYSTEM).toLowerCase();
    }

    public static void setScale(String value, StringConsumer onSuccess) {
        setSetting(SCALE, value, onSuccess);
    }

    public static String getScale() {
        return getSetting(SCALE, "1.0");
    }

    public static void setUsername(String value, StringConsumer onSuccess) {
        setSetting(USERNAME, value, onSuccess);
    }

    public static String getUsername() {
        String device = Build.MODEL.substring(0, 7);
        String saved = getSetting(USERNAME, device);
        return saved.isBlank() ? device : saved;
    }

    public static void setAvatar(String value, StringConsumer onSuccess) {
        setSetting(AVATAR, value, onSuccess);
    }

    public static String getAvatar() {
        String avatar = getSetting(AVATAR, "");
        if(avatar.isBlank()) {
            avatar = Avatar.randomShape().name();
            setAvatar(avatar, null);
        }
        return avatar;
    }


    public static void setFourMode(String value, StringConsumer onSuccess) {
        setSetting(FOUR_MODE, value, onSuccess);
    }

    public static String getFourMode() {
        return getSetting(FOUR_MODE, FourMode.ASK_EVERYTIME.getText());
    }

    public static void setLanguage(String value, StringConsumer onSuccess) {
        setSetting(LANGUAGE, value, onSuccess);
    }

    public static String getLanguage() {
        return getSetting(LANGUAGE, "en_us");
    }

    public static void setLogs(String value, StringConsumer onSuccess) {
        setSetting(LOGS, value, onSuccess);
    }

    public static String getLogs() {
        return getSetting(LOGS, "");
    }

    public static void setTimer(String value, StringConsumer onSuccess) {
        setSetting(TIMER, value, onSuccess);
    }

    public static String getTimer() {
        return getSetting(TIMER, Timer.DEFAULT.getText());
    }

    public static void setAmbient(String value, StringConsumer onSuccess) {
        setSetting(AMBIENT, value, onSuccess);
    }

    public static String getAmbient() {
        return getSetting(AMBIENT, "on");
    }

    public static void setMenuSounds(String value, StringConsumer onSuccess) {
        setSetting(MENU_SOUNDS, value, onSuccess);
    }

    public static String getMenuSounds() {
        return getSetting(MENU_SOUNDS, "on");
    }

    public static void setGameSounds(String value, StringConsumer onSuccess) {
        setSetting(GAME_SOUNDS, value, onSuccess);
    }

    public static String getGameSounds() {
        return getSetting(GAME_SOUNDS, "on");
    }
}
