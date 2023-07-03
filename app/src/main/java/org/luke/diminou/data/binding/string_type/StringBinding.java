package org.luke.diminou.data.binding.string_type;

import org.luke.diminou.abs.utils.functional.ObjectSupplier;
import org.luke.diminou.data.binding.Binding;
import org.luke.diminou.data.binding.IntegerBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.observable.StringObservable;

public class StringBinding extends Binding<String> implements StringObservable {
    public StringBinding(ObjectSupplier<String> calc, Observable<?>... dependencies) {
        super(calc, dependencies);
    }

    public BooleanBinding isEmpty() {
        return new BooleanBinding(() -> get().isEmpty(), this);
    }

    public StringBinding concat(StringObservable other) {
        return new StringBinding(() -> get().concat(other.get()), this, other);
    }
    public IntegerBinding lengthProperty() {
        return new IntegerBinding(() -> get().length(), this);
    }
}
