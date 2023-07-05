package org.luke.diminou.abs.style;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.Assets;
import org.luke.diminou.abs.utils.ErrorHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class Style {
    public static final String THEME_SYSTEM = "theme_system";
    public static final String THEME_DARK = "theme_dark";
    public static final String THEME_LIGHT = "theme_light";
    private final HashMap<String, Integer> colors;
    private final boolean dark;

    private Style(HashMap<String, Integer> colors, boolean dark) {
        this.colors = new HashMap<>(colors);
        this.dark = dark;
    }

    public Style(App owner, String styleName, boolean dark) {
        this.dark = dark;
        colors = new HashMap<>();
        try {
            JSONObject data = new JSONObject(Objects.requireNonNull(Assets.readAsset(owner, "themes/" + styleName + ".json")));
            Iterator<String> keys = data.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                colors.put(key, parseColor(data.getString(key)));
            }
        } catch (JSONException x) {
            ErrorHandler.handle(x, "loading style ".concat(styleName));
        }
    }

    @ColorInt
    private static int parseColor(String rgba) {
        char[] chars = rgba.toCharArray();

        String argb = "#";

        argb += chars[7];
        argb += chars[8];

        argb += chars[1];
        argb += chars[2];
        argb += chars[3];
        argb += chars[4];
        argb += chars[5];
        argb += chars[6];

        return Color.parseColor(argb);
    }

    public boolean isDark() {
        return dark;
    }

    public boolean isLight() {
        return !dark;
    }
    @NonNull
    public Style copy() {
        return new Style(colors, dark);
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getTextNormal() {
        return colors.get("textNormal");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getTextMuted() {
        return colors.get("textMuted");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getTextPositive() {
        return colors.get("textPositive");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getTextWarning() {
        return colors.get("textWarning");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getTextDanger() {
        return colors.get("textDanger");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getBackgroundPrimary() {
        return colors.get("backgroundPrimary");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getBackgroundTertiary() {
        return colors.get("backgroundTertiary");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getChannelsDefault() {
        return colors.get("channelsDefault");
    }

    @ColorInt
    @SuppressWarnings("ConstantConditions")
    public int getSecondaryButtonBack() {
        return colors.get("secondaryButtonBack");
    }
}
