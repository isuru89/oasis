package io.github.isuru.oasis.model.db;

import io.github.isuru.oasis.model.configs.ConfigKeys;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.utils.OasisUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class DbProperties {

    private final String daoName;

    private String queryLocation;
    private String url;
    private String username;
    private String password;
    private Map<String, Object> otherOptions;

    public DbProperties(String daoName) {
        this.daoName = daoName;
    }

    public String getDaoName() {
        return daoName;
    }

    public String getQueryLocation() {
        return queryLocation;
    }

    public void setQueryLocation(String queryLocation) {
        this.queryLocation = queryLocation;
    }

    public Map<String, Object> getOtherOptions() {
        return otherOptions;
    }

    public void setOtherOptions(Map<String, Object> otherOptions) {
        this.otherOptions = otherOptions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static DbProperties fromProps(Configs configs) throws FileNotFoundException {
        String name = configs.getStr("oasis.db.name", "default");
        DbProperties dbProps = new DbProperties(name);
        String oasisJdbcUrl = System.getenv(EnvKeys.OASIS_JDBC_URL);
        if (oasisJdbcUrl != null && !oasisJdbcUrl.isEmpty()) {
            dbProps.setUrl(oasisJdbcUrl);
        } else {
            dbProps.setUrl(configs.getStrReq(ConfigKeys.KEY_JDBC_URL));
        }
        dbProps.setUsername(configs.getStrReq(ConfigKeys.KEY_JDBC_USERNAME));
        dbProps.setPassword(configs.getStrReq(ConfigKeys.KEY_JDBC_PASSWORD));
        File scriptsDir = new File(configs.getStrReq(ConfigKeys.KEY_JDBC_SCRIPTS_PATH));
        if (scriptsDir.exists()) {
            dbProps.setQueryLocation(scriptsDir.getAbsolutePath());
        } else {
            throw new FileNotFoundException("The given scripts dir '" + scriptsDir.getAbsolutePath()
                    + "' does not exist!");
        }

        Map<String, Object> map = OasisUtils.filterKeys(configs.getProps(), "oasis.db.pool.");
        if (map != null && !map.isEmpty()) {
            dbProps.setOtherOptions(map);
        }
        return dbProps;
    }
}
