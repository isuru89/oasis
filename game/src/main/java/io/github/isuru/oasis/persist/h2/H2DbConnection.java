package io.github.isuru.oasis.persist.h2;

import io.github.isuru.oasis.OasisConfigurations;
import io.github.isuru.oasis.persist.IDbConnection;
import io.github.isuru.oasis.persist.JdbcConnection;

/**
 * @author iweerarathna
 */
public class H2DbConnection extends JdbcConnection {

    public H2DbConnection() {
    }

    @Override
    public IDbConnection init(OasisConfigurations configurations) {
        setConnectionStr(configurations.getDbProperties().getUrl());

        System.out.println(configurations.getDbProperties().getUrl());
        getJdbi().useHandle(handle -> {
            String schemaSql = loadScript(JdbcConnection.BASE_R + "/schema.sql");
            handle.createScript(schemaSql).execute();
        });
        return this;
    }
}
