package org.luke.diminou.abs.components.controls.text.font;

import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;

import androidx.annotation.NonNull;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;

import java.util.HashMap;
import java.util.Objects;

public class Font {

    public static final String DEFAULT_FAMILY_LATIN = "Aldrich";
    public static final String DEFAULT_FAMILY_ARABIC = "Readex Pro";
    public static String DEFAULT_FAMILY = DEFAULT_FAMILY_LATIN;
    public static final FontWeight DEFAULT_WEIGHT = FontWeight.NORMAL;
    public static final boolean DEFAULT_ITALIC = false;
    public static final float DEFAULT_SIZE = 14;

    public static Font DEFAULT = new Font();
    private static final HashMap<String, Typeface> base = new HashMap<>();
    private static final HashMap<Font, Typeface> cache = new HashMap<>();

    private String family;
    private float size;
    private FontWeight weight;
    private boolean italic;

    public Font(String family, float size, FontWeight weight, boolean italic) {
        this.family = family;
        this.size = size;
        this.weight = weight;
        this.italic = italic;
    }

    public Font(String family, float size, FontWeight weight) {
        this(family, size, weight, DEFAULT_ITALIC);
    }

    public Font(String family, float size) {
        this(family, size, DEFAULT_WEIGHT, DEFAULT_ITALIC);
    }

    public Font(String family) {
        this(family, DEFAULT_SIZE, DEFAULT_WEIGHT, DEFAULT_ITALIC);
    }

    public Font(float size) {
        this(DEFAULT_FAMILY, size, DEFAULT_WEIGHT, DEFAULT_ITALIC);
    }

    public Font(FontWeight weight) {
        this(DEFAULT_FAMILY, DEFAULT_SIZE, weight, DEFAULT_ITALIC);
    }

    public Font(float size, FontWeight weight) {
        this(DEFAULT_FAMILY, size, weight, DEFAULT_ITALIC);
    }

    public Font() {
        this(DEFAULT_FAMILY, DEFAULT_SIZE, DEFAULT_WEIGHT, DEFAULT_ITALIC);
    }

    public Font(float size, boolean italic) {
        this(DEFAULT_FAMILY, size, DEFAULT_WEIGHT, italic);
    }

    public Font(float size, boolean italic, FontWeight weight) {
        this(DEFAULT_FAMILY, size, weight, italic);
    }

    public static void init(App owner) {
        loadFont(DEFAULT_FAMILY_LATIN, owner);
        loadFont(DEFAULT_FAMILY_ARABIC, owner);
    }

    private static void loadFont(String name, App owner) {
        for (FontVar var : FontVar.values()) {
            if(var != FontVar.REGULAR) continue;
            try {
                String path = "fonts/" + name.replace(" ", "") + "-" + var.name.replace(" ", "") + ".ttf";

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    android.graphics.fonts.Font font = new android.graphics.fonts.Font.Builder(owner.getAssets(), path).build();
                    FontFamily family = new FontFamily.Builder(font).build();

                    String fallBack = "fonts/" + (name.equals(DEFAULT_FAMILY_ARABIC) ? DEFAULT_FAMILY_LATIN : DEFAULT_FAMILY_ARABIC).replace(" ", "") + "-" + var.name.replace(" ", "") + ".ttf";
                    android.graphics.fonts.Font fallbackFont = new android.graphics.fonts.Font.Builder(owner.getAssets(), fallBack).build();
                    FontFamily fallbackFamily = new FontFamily.Builder(fallbackFont).build();
                    Typeface typeface = new Typeface.CustomFallbackBuilder(family)
                            .addCustomFallback(fallbackFamily)  // Specify fallback family.
                            .setSystemFallback(DEFAULT_FAMILY_ARABIC)  // Set serif font family as the fallback.
                            .build();
                    base.put(name + " " + var.name, typeface);
                }else {
                    base.put(name + " " + var.name, Typeface.createFromAsset(owner.getAssets(), path));
                }
            } catch (Exception x) {
                ErrorHandler.handle(x, "loading font ".concat(name));
            }
        }
    }

    public Font setFamily(String family) {
        this.family = family;
        return this;
    }

    public Font setWeight(FontWeight weight) {
        this.weight = weight;
        return this;
    }

    public Font setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(family, italic, size, weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Font other = (Font) obj;
        return Objects.equals(family, other.family) && italic == other.italic
                && Float.floatToIntBits(size) == Float.floatToIntBits(other.size) && weight == other.weight;
    }

    public Typeface getFont() {
        Typeface found = cache.get(this);

        if (found == null) {
            found = Typeface.create(base.get(family + " " + FontVar.getVar(weight, italic)), Typeface.NORMAL);
            cache.put(this, found);
        }

        return found;
    }

    public Font copy() {
        return new Font(family, size, weight, italic);
    }

    public float getSize() {
        return size * ViewUtils.scale;
    }

    public Font setSize(float size) {
        this.size = size;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Font [family=" + family + ", size=" + size + ", weight=" + weight + ", italic=" + italic + "]";
    }

    private enum FontVar {
        LIGHT("Light", FontWeight.LIGHT, false),
        REGULAR("Regular", FontWeight.NORMAL, false),
        MEDIUM("Medium", FontWeight.MEDIUM, false),
        BOLD("Bold", FontWeight.BOLD, false),
        LIGHT_ITALIC("Light Italic", FontWeight.LIGHT, true),
        ITALIC("Italic", FontWeight.NORMAL, true),
        MEDIUM_ITALIC("Medium Italic", FontWeight.MEDIUM, true),
        BOLD_ITALIC("Bold Italic", FontWeight.BOLD, true);

        final String name;
        final FontWeight weight;
        final boolean italic;
        FontVar(String name, FontWeight weight, boolean italic) {
            this.name = name;
            this.weight = weight;
            this.italic = italic;
        }

        static String getVar(FontWeight weight, boolean italic) {
            for(FontVar var : values()) {
                if(weight == var.weight && italic == var.italic) return var.name;
            }

            return REGULAR.name;
        }
    }
}