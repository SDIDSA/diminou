package org.luke.diminou.data.observable;

public interface ChangeListener<T> {
    void changed(Observable<? extends T> property, T oldVal, T newVal);
}
