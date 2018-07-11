package io.github.isuru.oasis.persist;

import io.github.isuru.oasis.OasisConfigurations;
import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.persist.h2.H2DbConnection;

import java.io.File;

/**
 * @author iweerarathna
 */
public class PersistFactory {

    public static IDbConnection createDbConnection(OasisConfigurations configurations) {
        DbProperties dbProperties = configurations.getDbProperties();
        if (dbProperties == null) {
            return NoDbConnection.INSTANCE;
        } else {
            File file = new File(dbProperties.getUrl());
            if (file.exists()) {
                // nyql
                return new NyQLDbConnection().init(configurations);
            } else {
                // jdbc
                return new H2DbConnection().init(configurations);
            }
        }
    }

}
