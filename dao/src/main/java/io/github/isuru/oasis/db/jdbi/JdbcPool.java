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

        if (properties.getOtherOptions() != null) {
            Properties props = new Properties();
            props.putAll(properties.getOtherOptions());
            config.setDataSourceProperties(props);
        }
        return new HikariDataSource(config);
    }

}
