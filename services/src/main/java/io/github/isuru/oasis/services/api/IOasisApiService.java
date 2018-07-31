package io.github.isuru.oasis.services.api;

/**
 * @author iweerarathna
 */
public interface IOasisApiService {

    IGameDefService getGameDefService();

    IGameService getGameService();

    IProfileService getProfileService();

    ILifecycleService getLifecycleService();

    IEventsService getEventService();

    IStatService getStatService();

}
