package org.luke.diminou.abs.utils;

import android.os.Build;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.functional.StringConsumer;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.app.pages.settings.Timer;

import io.reactivex.rxjava3.core.Single;

public class Store {
    private static RxDataStore<Preferences> settings;
    private static final Preferences.Key<String> USERNAME = PreferencesKeys.stringKey("username");
    private static final Preferences.Key<String> AVATAR = PreferencesKeys.stringKey("avatar");
    private static final Preferences.Key<String> THEME = PreferencesKeys.stringKey("theme");
    private static final Preferences.Key<String> SCALE = PreferencesKeys.stringKey("scale");
    private static final Preferences.Key<String> LANGUAGE = PreferencesKeys.stringKey("language");
    private static final Preferences.Key<String> FOUR_MODE = PreferencesKeys.stringKey("four_mode");
    private static final Preferences.Key<String> TIMER = PreferencesKeys.stringKey("timer");
    private static final Preferences.Key<String> LOGS = PreferencesKeys.stringKey("logs");


    public static void init(App owner) {
        if(settings != null) settings.dispose();
        settings = new RxPreferenceDataStoreBuilder(owner, "settings").build();
    }

    public static void destroy() {
        settings.dispose();
    }

    private static String getSetting(Preferences.Key<String> key, String def) {
        try {
            return settings.data().map(prefs -> prefs.get(key)).first(def).blockingGet();
        }catch(Exception x) {
            return def;
        }
    }

    private static void setSetting(Preferences.Key<String> key, String value, StringConsumer onSuccess) {
        boolean success = false;
        while(!success) {
            try {
                String res = settings.updateDataAsync(prefsIn -> {
                    MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                    mutablePreferences.set(key, value);
                    return Single.just(mutablePreferences);
                }).blockingGet().get(key);
                if(onSuccess != null)
                    onSuccess.accept(res);
                success = true;
            }catch (Exception x) {
                ErrorHandler.handle(new RuntimeException("failed to store"), "storing data at " + key.getName() + ", retrying...");
            }
        }
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
}
