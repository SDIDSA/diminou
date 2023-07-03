package org.luke.diminou.data.observable;

import org.luke.diminou.data.binding.DoubleBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;

public interface DoubleObservable extends Observable<Double> {
    DoubleBinding add(DoubleObservable other);
    DoubleBinding subtract(DoubleObservable other);
    DoubleBinding multiply(DoubleObservable other);
    DoubleBinding divide(DoubleObservable other);
    BooleanBinding isEqualTo(DoubleObservable other);
    BooleanBinding isGreaterThan(DoubleObservable other);
    BooleanBinding isLesserThan(DoubleObservable other);
}
