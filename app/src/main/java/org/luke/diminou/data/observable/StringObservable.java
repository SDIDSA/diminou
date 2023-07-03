package org.luke.diminou.data.observable;

import org.luke.diminou.data.binding.IntegerBinding;
import org.luke.diminou.data.binding.boolean_type.BooleanBinding;
import org.luke.diminou.data.binding.string_type.StringBinding;

public interface StringObservable extends Observable<String> {
    StringBinding concat(StringObservable obs);
    BooleanBinding isEmpty();
    IntegerBinding lengthProperty();
}
