package org.luke.diminou.abs.components;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class Page extends FrameLayout implements Styleable {
    private static final HashMap<Class<? extends Page>, Page> cache = new HashMap<>();
    protected final App owner;

    public Page(App owner) {
        super(owner);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setClipChildren(false);

        this.owner = owner;
    }

    public static boolean hasInstance(Class<? extends Page> type) {
        return cache.containsKey(type);
    }

    public static Page getInstance(App owner, Class<? extends Page> type) {
        Page found = cache.get(type);
        if (found == null) {
            try {
                found = type.getConstructor(App.class).newInstance(owner);
                cache.put(type, found);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            if (found.getParent() != null) {
                if(owner.getLoaded() != found) {
                    ((ViewGroup) found.getParent()).removeView(found);
                }
            }

        }
        if (type.isInstance(found)) {
            return type.cast(found);
        } else {
            return null;
        }
    }

    public void setup() {

    }

    public void destroy() {

    }

    public static void clearCache() {
        cache.clear();
    }

    public static void clearCache(Class<? extends Page> type) {
        cache.remove(type);
    }

    protected void setPadding(int padding) {
        ViewUtils.setPaddingUnified(this, padding, owner);
    }

    public abstract boolean onBack();

    public abstract void applyInsets(Insets insets);
}
