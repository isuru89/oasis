package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.Event;

/**
 * @author iweerarathna
 */
public interface IErrorHandler<E> {

    void onError(Throwable ex, Event e, E rule);

}
