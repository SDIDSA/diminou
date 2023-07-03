package org.luke.diminou.data.binding.boolean_type;

import org.luke.diminou.data.observable.BooleanObservable;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.BooleanProperty;

import java.util.ArrayList;

public class BooleanWhen  {
    private final BooleanObservable condition;
    private BooleanObservable then;
    private BooleanObservable otherwise;

    private final ArrayList<Observable<?>> dependencies = new ArrayList<>();

    public BooleanWhen(BooleanObservable condition) {
        this.condition = condition;
        dependencies.add(condition);
    }

    public BooleanWhen then(BooleanObservable then) {
        this.then = then;
        dependencies.add(then);
        return this;
    }

    public BooleanWhen then(Boolean then) {
        this.then = new BooleanProperty(then);
        return this;
    }

    public BooleanBinding otherwise(BooleanObservable otherwise) {
        this.otherwise = otherwise;
        dependencies.add(otherwise);

        return generateBinding();
    }

    public BooleanBinding otherwise(Boolean otherwise) {
        this.otherwise = new BooleanProperty(otherwise);
        return generateBinding();
    }

    private BooleanBinding generateBinding() {
        return new BooleanBinding(() -> condition.get() ? then.get() : otherwise.get(), dependencies.toArray(new Observable[0]));
    }
}
