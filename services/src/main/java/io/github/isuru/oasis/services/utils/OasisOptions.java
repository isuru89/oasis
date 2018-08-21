package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.backend.FlinkServices;

/**
 * @author iweerarathna
 */
public class OasisOptions {

    private FlinkServices flinkServices;
    private IGameController gameController;
    private Configs configs;

    public Configs getConfigs() {
        return configs;
    }

    public void setConfigs(Configs configs) {
        this.configs = configs;
    }

    public FlinkServices getFlinkServices() {
        return flinkServices;
    }

    public void setFlinkServices(FlinkServices flinkServices) {
        this.flinkServices = flinkServices;
    }

    public IGameController getGameController() {
        return gameController;
    }

    public void setGameController(IGameController gameController) {
        this.gameController = gameController;
    }
}
