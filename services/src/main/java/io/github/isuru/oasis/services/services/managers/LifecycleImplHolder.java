package io.github.isuru.oasis.services.services.managers;

import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.services.ILifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LifecycleImplHolder {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleImplHolder.class);

    private ILifecycleService activatedLifecycle;

    @Autowired
    public LifecycleImplHolder(Map<String, ILifecycleService> lifecycleServiceMap, OasisConfigurations configurations) {
        boolean local = OasisUtils.getEnvOr("OASIS_MODE", "oasis.mode", configurations.getMode())
                .trim()
                .equalsIgnoreCase("local");

        if (local) {
            LOG.info("Activating local lifecycle service...");
            activatedLifecycle = lifecycleServiceMap.get("localLifecycleService");
        } else {
            LOG.info("Activating remote lifecycle service...");
            activatedLifecycle = lifecycleServiceMap.get("remoteLifecycleService");
        }
    }

    public ILifecycleService get() {
        return activatedLifecycle;
    }

}
