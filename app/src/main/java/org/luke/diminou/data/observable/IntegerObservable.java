package org.luke.diminou.data.observable;

import org.luke.diminou.data.binding.IntegerBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;

public interface IntegerObservable extends Observable<Integer>{
    IntegerBinding add(IntegerObservable other);
    IntegerBinding subtract(IntegerObservable other);
    IntegerBinding multiply(IntegerObservable other);
    IntegerBinding divide(IntegerObservable other);
    BooleanBinding isEqualTo(IntegerObservable other);
    BooleanBinding isGreaterThan(IntegerObservable other);
    BooleanBinding isLesserThan(IntegerObservable other);
    IntegerBinding add(int other);
    IntegerBinding subtract(int other);
    IntegerBinding multiply(int other);
    IntegerBinding divide(int other);
    BooleanBinding isEqualTo(int other);
    BooleanBinding isGreaterThan(int other);
    BooleanBinding isLesserThan(int other);
}
