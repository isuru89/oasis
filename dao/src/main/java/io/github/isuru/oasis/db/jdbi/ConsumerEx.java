package io.github.isuru.oasis.db.jdbi;

/**
 * @author iweerarathna
 */
public interface ConsumerEx<T> {

    Object consume(T input) throws Exception;

}
