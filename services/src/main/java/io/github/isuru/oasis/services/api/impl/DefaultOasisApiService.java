package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.api.IStatService;
import io.github.isuru.oasis.services.utils.OasisOptions;

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

    private IOasisDao dao;

    public DefaultOasisApiService(IOasisDao oasisDao, OasisOptions oasisOptions, Configs configs) {
        this.dao = oasisDao;

        gameDefService = new GameDefService(this);
        profileService = new ProfileService(this);
        gameService = new GameService(this);
        if (configs.isLocal()) {
            lifecycleService = new LocalLifeCycleService(this, oasisOptions);
        } else {
            lifecycleService = new LifeCycleService(this, oasisOptions);
        }
        eventsService = new EventsService(this, oasisOptions);
        statService = new StatService(this);
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

    @Override
    public IOasisDao getDao() {
        return dao;
    }
}
