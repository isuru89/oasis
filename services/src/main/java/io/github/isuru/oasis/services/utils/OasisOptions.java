package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.services.backend.FlinkServices;
import io.github.isuru.oasis.services.model.IGameController;

/**
 * @author iweerarathna
 */
public class OasisOptions {

    private FlinkServices flinkServices;
    private IGameController gameController;
    private ICacheProxy cacheProxy;
    private Configs configs;

    public ICacheProxy getCacheProxy() {
        return cacheProxy;
    }

    public void setCacheProxy(ICacheProxy cacheProxy) {
        this.cacheProxy = cacheProxy;
    }

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
