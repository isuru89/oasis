package io.github.isuru.oasis.db.jdbi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.isuru.oasis.db.DbProperties;

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
        return new HikariDataSource(config);
    }

}
