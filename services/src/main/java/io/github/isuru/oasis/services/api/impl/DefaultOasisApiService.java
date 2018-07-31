package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.api.IStatService;
import io.github.isuru.oasis.services.backend.FlinkServices;

/**
 * @author iweerarathna
 */
public class DefaultOasisApiService implements IOasisApiService {

    private IGameDefService gameDefService;
    private IGameService gameService;
    private IProfileService profileService;
    private ILifecycleService lifecycleService;
    private IStatService statService;
    private IEventsService eventsService;

    public DefaultOasisApiService(IOasisDao oasisDao, FlinkServices flinkServices) {
        gameDefService = new GameDefService(oasisDao, this);
        profileService = new ProfileService(oasisDao, this);
        gameService = new GameService(oasisDao, this);
        lifecycleService = new LifeCycleService(oasisDao, this, flinkServices);
        eventsService = new EventsService(oasisDao, this);
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
        return eventsService;
    }

    @Override
    public IStatService getStatService() {
        return statService;
    }

}
