package org.luke.diminou.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.luke.diminou.abs.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConcurrentArrayList<T> extends ArrayList<T> {
    private final Semaphore mutex = new Semaphore(1);

    @Override
    public boolean add(T t) {
        acquire();
        boolean res = super.add(t);
        release();
        return res;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        acquire();
        boolean res = super.remove(o);
        release();
        return res;
    }

    @Override
    public T remove(int index) {
        acquire();
        T res = super.remove(index);
        release();
        return res;
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super T> filter) {
        acquire();
        boolean res = super.removeIf(filter);
        release();
        return res;
    }

    @Override
    public void forEach(@NonNull Consumer<? super T> action) {
        acquire();
        super.forEach(action);
        release();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        acquire();
        boolean res = super.addAll(c);
        release();
        return res;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        acquire();
        boolean res = super.addAll(index, c);
        release();
        return res;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        acquire();
        boolean res = super.removeAll(c);
        release();
        return res;
    }
    
    private void acquire() {
        mutex.acquireUninterruptibly();
    }

    private void release() {
        mutex.release();
    }
}
