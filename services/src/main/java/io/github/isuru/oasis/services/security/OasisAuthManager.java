package io.github.isuru.oasis.services.security;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OasisAuthManager {

    private static final Logger LOG = LoggerFactory.getLogger(OasisAuthManager.class);

    private final OasisAuthenticator authenticator;

    @Autowired
    public OasisAuthManager(Map<String, OasisAuthenticator> authenticatorMap,
                            OasisConfigurations configurations) {
        String type = Commons.firstNonNull(configurations.getAuth().getType(), "none");
        String key = StringUtils.capitalize(type);

        LOG.info("Loading authentication type: {}", type);
        authenticator = authenticatorMap.get(key);
    }

    public OasisAuthenticator get() {
        return authenticator;
    }
}
