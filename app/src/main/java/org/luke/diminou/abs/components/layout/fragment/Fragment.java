package org.luke.diminou.abs.components.layout.fragment;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.layout.linear.VBox;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Fragment extends VBox {
    private final App owner;
    private static final HashMap<Class<? extends Fragment>, Fragment> cache = new HashMap<>();
    private FragmentPane parent;

    public Fragment(App owner) {
        super(owner);
        this.owner = owner;
        setSpacing(10);
    }

    public static Fragment getInstance(App owner, Class<? extends Fragment> type) {
        Fragment found = cache.get(type);
        if (found == null) {
            try {
                found = type.getConstructor(App.class).newInstance(owner);
                cache.put(type, found);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (type.isInstance(found)) {
            return type.cast(found);
        } else {
            return null;
        }
    }

    public static void clearCache(Class<? extends Fragment> type) {
        List<Class<? extends Fragment>> concerned =
                cache.keySet().stream().filter(c -> Objects.equals(c.getSuperclass(), type)).
                        collect(Collectors.toList());
        concerned.forEach(cache::remove);
    }

    public static void clearCache() {
        cache.clear();
    }

    protected FragmentPane parent() {
        if (parent == null) parent = (FragmentPane) getParent();

        return parent;
    }

    public void nextInto(Class<? extends Fragment> pageType) {
        parent().nextInto(pageType);
    }

    public void previousInto(Class<? extends Fragment> pageType) {
        parent().previousInto(pageType);
    }

    @Override
    public App getOwner() {
        return owner;
    }

    public void setup(boolean direction) {

    }

    public void destroy(boolean direction) {

    }
}
