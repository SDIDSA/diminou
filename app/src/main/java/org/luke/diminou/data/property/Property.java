package org.luke.diminou.data.property;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.data.ConcurrentArrayList;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.observable.Observable;

import java.util.Objects;

public class Property<T> implements Observable<T> {
    private final ConcurrentArrayList<ChangeListener<? super T>> listeners = new ConcurrentArrayList<>();
    private T value;
    private Observable<T> boundTo;
    private boolean bound;
    private ChangeListener<T> onBoundChanged;

    public Property() {
    }

    public Property(T value) {
        this.value = value;
    }

    public boolean isBound() {
        return bound;
    }

    public void bind(Observable<T> bindTo) {
        if (bound) {
            ErrorHandler.handle(new IllegalStateException("you can't bind this property because it's already bound"), "binding property");
        }
        boundTo = bindTo;

        onBoundChanged = (obs, ov, nv) -> set(nv);

        boundTo.addListener(onBoundChanged);
        bound = true;
    }

    public void unbind() {
        if (bound) {
            boundTo.removeListener(onBoundChanged);
            boundTo = null;
            bound = false;
        }
    }

    @Override
    public void set(T value) {
        T ov = this.value;
        this.value = value;
        if (!Objects.equals(ov, value)) {
            listeners.forEach(l -> l.changed(this, ov, value));
        }
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        listener.changed(this, value, value);
        listeners.add(listener);
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }
}
