package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;

/**
 * @author iweerarathna
 */
public class DefaultOasisApiService implements IOasisApiService {

    private IGameDefService gameDefService;
    private IGameService gameService;
    private IProfileService profileService;

    public DefaultOasisApiService(IOasisDao oasisDao) {
        gameDefService = new GameDefService(oasisDao);
        profileService = new ProfileService(oasisDao);
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
}
