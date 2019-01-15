package io.github.isuru.oasis.services.services.caches;

import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheProxyManager {

    private static final Logger LOG = LoggerFactory.getLogger(CacheProxyManager.class);

    private final ICacheProxy cacheProxy;

    @Autowired
    public CacheProxyManager(Map<String, ICacheProxy> cacheProxyMap, OasisConfigurations configurations) {
        String cacheImpl = configurations.getCache().getImpl();
        String key = "cache" + StringUtils.capitalize(cacheImpl);
        cacheProxy = cacheProxyMap.computeIfAbsent(key, s -> cacheProxyMap.get("cacheNone"));
        LOG.info("Loaded cache implementation: " + cacheProxy.getClass().getName());
    }

    public ICacheProxy get() {
        return cacheProxy;
    }
}
