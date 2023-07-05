package org.luke.diminou.abs.style;

import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.Property;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public interface Styleable {
    void applyStyle(Style style);
    void applyStyle(Property<Style> style);

    static void bindStyle(Styleable node, Property<Style> style) {
        bindStyleWeak(node, style);
    }

    ArrayList<WeakReference<Styleable>> bound_cache = new ArrayList<>();

    private static boolean isBound(Styleable node) {
        bound_cache.removeIf(n -> n.get() == null);
        for(WeakReference<Styleable> nodeRef : bound_cache) {
            if(nodeRef.get() == node) return true;
        }
        return false;
    }

    private static void bindStyleWeak(Styleable node, Property<Style> style) {
        node.applyStyle(style.get());
        if(isBound(node)) return;
        WeakReference<Styleable> weakNode = new WeakReference<>(node);
        ChangeListener<Style> listener = new ChangeListener<>() {
            @Override
            public void changed(Observable<? extends Style> obs, Style ov, Style nv) {
                if (weakNode.get() != null) {
                    if (nv != ov) {
                        weakNode.get().applyStyle(nv);
                    }
                } else {
                    style.removeListener(this);
                }
            }
        };
        style.addListener(listener);
        bound_cache.add(weakNode);
    }
}
