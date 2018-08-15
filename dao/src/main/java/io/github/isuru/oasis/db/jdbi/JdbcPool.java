package io.github.isuru.oasis.db.jdbi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.isuru.oasis.model.db.DbProperties;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author iweerarathna
 */
class JdbcPool {

    static DataSource createDataSource(DbProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        System.out.println(properties.getUsername() + " , " + properties.getPassword());

        Properties props = new Properties();
        if (properties.getOtherOptions() != null) {
            props.putAll(properties.getOtherOptions());
            config.setDataSourceProperties(props);
        } else {
            props.put("prepStmtCacheSize", 250);
            props.put("prepStmtCacheSqlLimit", 2048);
            props.put("cachePrepStmts", true);
            props.put("useServerPrepStmts", true);
        }

        DataSource dataSource;

        int retry = 10;
        while (retry > 0) {
            try {
                dataSource = new HikariDataSource(config);
                return dataSource;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            retry--;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        throw new IllegalStateException("Cannot initialize database connection!");
    }

}
