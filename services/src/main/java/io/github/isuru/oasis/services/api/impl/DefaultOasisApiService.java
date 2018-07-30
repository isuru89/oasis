package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.backend.FlinkServices;

/**
 * @author iweerarathna
 */
public class DefaultOasisApiService implements IOasisApiService {

    private IGameDefService gameDefService;
    private IGameService gameService;
    private IProfileService profileService;
    private ILifecycleService lifecycleService;

    public DefaultOasisApiService(IOasisDao oasisDao, FlinkServices flinkServices) {
        gameDefService = new GameDefService(oasisDao);
        profileService = new ProfileService(oasisDao);
        lifecycleService = new LifeCycleService(oasisDao, this, flinkServices);
    }

    @Override
    public IGameDefService getGameDefService() {
        return gameDefService;
    }

    @Override
    public IGameService getGameService() {
        return gameService;
    }

    @Override
    public IProfileService getProfileService() {
        return profileService;
    }

    @Override
    public ILifecycleService getLifecycleService() {
        return lifecycleService;
    }

    @Override
    public IEventsService getEventService() {
        return null;
    }
}
