package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;

/**
 * @author iweerarathna
 */
public interface IOasisApiService {

    IOasisDao getDao();

    IGameDefService getGameDefService();

    IGameService getGameService();

    IProfileService getProfileService();

    ILifecycleService getLifecycleService();

    IEventsService getEventService();

    IStatService getStatService();

    IMetaphorService getMetaphorService();
}
