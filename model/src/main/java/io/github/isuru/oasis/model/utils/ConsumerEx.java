package io.github.isuru.oasis.model.utils;

/**
 * @author iweerarathna
 */
public interface ConsumerEx<T> {

    Object consume(T input) throws Exception;

}
