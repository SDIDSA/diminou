package org.luke.diminou.data.binding;

import android.util.Log;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectSupplier;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;
import org.luke.diminou.data.observable.ChangeListener;
import org.luke.diminou.data.observable.Observable;

import java.util.ArrayList;

public class Binding<T> implements Observable<T> {
    private T value;

    private final ArrayList<ChangeListener<? super T>> listeners = new ArrayList<>();

    public Binding(ObjectSupplier<T> calc, Observable<?>...dependencies) {
        ChangeListener<Object> cl = (o, ov, nv) -> {
            T old = value;
            value = calc.get();
            for(ChangeListener<? super T> listener : listeners) {
                listener.changed(this, old, value);
            }
        };

        for(Observable<?> obs:dependencies) {
            obs.addListener(cl);
        }

        value = calc.get();
    }

    @Override
    public void set(T value) {
        ErrorHandler.handle(new IllegalAccessError("you can't set a binding, it is calculated"), "setting binding");
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        listener.changed(this, null, value);
        listeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    public BooleanBinding isEqualTo(Observable<T> other) {
        return new BooleanBinding(() -> get().equals(other.get()), this, other);
    }

    public BooleanBinding isEqualTo(T other) {
        return new BooleanBinding(() -> get().equals(other), this);
    }
}