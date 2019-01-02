package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import io.github.isuru.oasis.services.utils.FlinkScheduler;
import io.github.isuru.oasis.services.utils.IGameController;
import io.github.isuru.oasis.services.utils.local.LocalScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(GameConfigs.class);

    @Autowired
    private IOasisDao oasisDao;

    @Autowired
    private IEventDispatcher eventDispatcher;

    @Bean
    public IGameController createGameController() {
        LOG.info("Creating remote game controllers...");
        IGameController gameController;

        boolean local = OasisUtils.getEnvOr("OASIS_MODE", "oasis.mode", "")
                .trim()
                .equalsIgnoreCase("local");

        if (local) {
            gameController = new LocalScheduler(oasisDao);
        } else {
            gameController = new FlinkScheduler(eventDispatcher);
        }
        return gameController;
    }

}
