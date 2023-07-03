package org.luke.diminou.data.property;

import org.luke.diminou.data.binding.IntegerBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;
import org.luke.diminou.data.observable.IntegerObservable;

public class IntegerProperty extends Property<Integer> implements IntegerObservable {
    public IntegerProperty(Integer v) {
        super(v);
    }

    public IntegerProperty() {
        super();
    }

    public IntegerBinding add(IntegerObservable other) {
        return new IntegerBinding(() -> get() + other.get(), this, other);
    }

    public IntegerBinding subtract(IntegerObservable other) {
        return new IntegerBinding(() -> get() - other.get(), this, other);
    }

    public IntegerBinding multiply(IntegerObservable other) {
        return new IntegerBinding(() -> get() * other.get(), this, other);
    }

    public IntegerBinding divide(IntegerObservable other) {
        return new IntegerBinding(() -> get() / other.get(), this, other);
    }

    public BooleanBinding isEqualTo(IntegerObservable other) {
        return new BooleanBinding(() -> get() == other.get(), this, other);
    }

    public BooleanBinding isGreaterThan(IntegerObservable other) {
        return new BooleanBinding(() -> get() > other.get(), this, other);
    }

    public BooleanBinding isLesserThan(IntegerObservable other) {
        return new BooleanBinding(() -> get() < other.get(), this, other);
    }

    public IntegerBinding add(int other) {
        return new IntegerBinding(() -> get() + other, this);
    }

    public IntegerBinding subtract(int other) {
        return new IntegerBinding(() -> get() - other, this);
    }

    public IntegerBinding multiply(int other) {
        return new IntegerBinding(() -> get() * other, this);
    }

    public IntegerBinding divide(int other) {
        return new IntegerBinding(() -> get() / other, this);
    }

    public BooleanBinding isEqualTo(int other) {
        return new BooleanBinding(() -> get() == other, this);
    }

    public BooleanBinding isGreaterThan(int other) {
        return new BooleanBinding(() -> get() > other, this);
    }

    public BooleanBinding isLesserThan(int other) {
        return new BooleanBinding(() -> get() < other, this);
    }
}
