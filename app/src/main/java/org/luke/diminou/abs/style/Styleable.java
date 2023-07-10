package org.luke.diminou.abs.style;

import android.os.Looper;
import android.view.View;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.data.ConcurrentArrayList;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.Property;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Styleable {
    void applyStyle(Style style);
    void applyStyle(Property<Style> style);

    static void bindStyle(Styleable node, Property<Style> style) {
        try {
            bindStyleWeak(node, style);
        }catch(Exception x) {
            ErrorHandler.handle(x, "binding style");
        }
    }

    ConcurrentArrayList<WeakReference<Styleable>> bound_cache = new ConcurrentArrayList<>();

    private static boolean isBound(Styleable node) {
        bound_cache.removeIf(n -> n.get() == null);
        AtomicBoolean res = new AtomicBoolean(false);
        bound_cache.forEach(nodeRef -> {
            if(nodeRef.get() == node) res.set(true);
        });
        return res.get();
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
                        if(Thread.currentThread() != Looper.getMainLooper().getThread()
                                && node instanceof View v
                                && v.isAttachedToWindow()) {
                            ErrorHandler.log();
                        }
                        weakNode.get().applyStyle(nv);
                    }
                } else {
                    Platform.runBack(() -> style.removeListener(this));
                }
            }
        };
        style.addListener(listener);
        bound_cache.add(weakNode);
    }
}
