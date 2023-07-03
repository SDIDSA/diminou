package org.luke.diminou.data.binding;

import org.luke.diminou.data.binding.boolean_type.BooleanWhen;
import org.luke.diminou.data.binding.string_type.StringWhen;
import org.luke.diminou.data.observable.BooleanObservable;

public class Bindings {
    public static BooleanWhen whenBoolean(BooleanObservable condition) {
        return new BooleanWhen(condition);
    }

    public static StringWhen whenString(BooleanObservable condition) {
        return new StringWhen(condition);
    }
}
