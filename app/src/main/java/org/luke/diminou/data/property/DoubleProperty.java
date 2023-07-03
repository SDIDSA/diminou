package org.luke.diminou.data.property;

import org.luke.diminou.data.binding.DoubleBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;
import org.luke.diminou.data.observable.DoubleObservable;

public class DoubleProperty extends Property<Double> implements DoubleObservable {
    public DoubleProperty(Double v) {
        super(v);
    }

    public DoubleProperty() {
        super();
    }

    public DoubleBinding add(DoubleObservable other) {
        return new DoubleBinding(() -> get() + other.get(), this, other);
    }

    public DoubleBinding subtract(DoubleObservable other) {
        return new DoubleBinding(() -> get() - other.get(), this, other);
    }

    public DoubleBinding multiply(DoubleObservable other) {
        return new DoubleBinding(() -> get() * other.get(), this, other);
    }

    public DoubleBinding divide(DoubleObservable other) {
        return new DoubleBinding(() -> get() / other.get(), this, other);
    }

    public BooleanBinding isEqualTo(DoubleObservable other) {
        return new BooleanBinding(() -> get() == other.get(), this, other);
    }

    public BooleanBinding isGreaterThan(DoubleObservable other) {
        return new BooleanBinding(() -> get() > other.get(), this, other);
    }

    public BooleanBinding isLesserThan(DoubleObservable other) {
        return new BooleanBinding(() -> get() < other.get(), this, other);
    }
}
