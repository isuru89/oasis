package io.github.isuru.oasis.model.collect;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private final A value0;
    private final B value1;

    private Pair(A value0, B value1) {
        this.value0 = value0;
        this.value1 = value1;
    }

    public static <A extends Serializable, B extends Serializable> Pair<A, B> of(A value0, B value1) {
        return new Pair<>(value0, value1);
    }

    public A getValue0() {
        return value0;
    }

    public B getValue1() {
        return value1;
    }
}
