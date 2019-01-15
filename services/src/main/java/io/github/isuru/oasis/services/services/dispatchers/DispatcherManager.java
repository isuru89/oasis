package io.github.isuru.oasis.services.services.dispatchers;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import io.github.isuru.oasis.services.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Component
public class DispatcherManager {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherManager.class);

    private final IEventDispatcher eventDispatcher;

    @Autowired
    public DispatcherManager(Map<String, IEventDispatcher> dispatcherMap,
                             OasisConfigurations configurations) {
        String impl = Commons.firstNonNull(configurations.getDispatcherImpl(), "local");
        String key = "dispatcher" + StringUtils.capitalize(impl);
        eventDispatcher = dispatcherMap.get(key);
        LOG.info("Loaded dispatcher impl: " + eventDispatcher.getClass().getName());
    }

    @PostConstruct
    private void init() throws IOException {
        LOG.info("Initializing " + eventDispatcher.getClass().getSimpleName() + "...");
        eventDispatcher.init();
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
