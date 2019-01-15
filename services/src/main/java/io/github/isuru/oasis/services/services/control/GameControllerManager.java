package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.IGameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GameControllerManager {

    private static final Logger LOG = LoggerFactory.getLogger(GameControllerManager.class);

    private final IGameController gameController;

    @Autowired
    public GameControllerManager(Map<String, IGameController> gameControllerMap, OasisConfigurations configurations) {
        boolean local = OasisUtils.getEnvOr("OASIS_MODE", "oasis.mode", configurations.getMode())
                .trim()
                .equalsIgnoreCase("local");

        if (local) {
            LOG.info("Activating local game controller service...");
            gameController = gameControllerMap.get("schedulerLocal");
        } else {
            LOG.info("Activating remote game controller service...");
            gameController = gameControllerMap.get("schedulerRemote");
        }
    }

    public IGameController get() {
        return gameController;
    }
}
