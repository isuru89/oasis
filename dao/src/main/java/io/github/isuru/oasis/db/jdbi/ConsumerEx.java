package io.github.isuru.oasis.db.jdbi;

/**
 * @author iweerarathna
 */
public interface ConsumerEx<T> {

    void consume(T input) throws Exception;

}
