package io.github.isuru.oasis.db;

import java.io.Closeable;

/**
 * @author iweerarathna
 */
public interface IQueryRepo extends Closeable {

    void init(DbProperties dbProperties) throws Exception;

    String fetchQuery(String queryId) throws Exception;

}
