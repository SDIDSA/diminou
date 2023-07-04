package org.luke.diminou.data.observable;

public interface Observable<T> {
    void set(T value);
    T get();
    void addListener(ChangeListener<? super T> listener);
    void removeListener(ChangeListener<? super T> listener);
    void clearListeners();
}
