package io.github.isuru.oasis.services.configs;

import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
public class LdapConfigs {

    @NotNull
    private String url;

    private String baseDN;

    @NotNull
    private String bindDN;
    @NotNull
    private String bindPassword;

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getBindDN() {
        return bindDN;
    }

    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
