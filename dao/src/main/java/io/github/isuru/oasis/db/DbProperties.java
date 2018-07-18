package io.github.isuru.oasis.db;

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
}
