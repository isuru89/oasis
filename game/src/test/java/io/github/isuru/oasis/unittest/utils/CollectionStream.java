package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.game.EventSource;
import io.github.isuru.oasis.model.Event;

import java.util.Collection;

public class CollectionStream<T extends Event> implements EventSource<T> {

    private Collection<T> collection;

    public CollectionStream(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        for (T item : collection) {
            ctx.collect(item);
        }
    }

    @Override
    public void cancel() {

    }
}
