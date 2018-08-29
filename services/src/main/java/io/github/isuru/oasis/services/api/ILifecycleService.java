package io.github.isuru.oasis.services.api;

/**
 * @author iweerarathna
 */
public interface ILifecycleService {

    boolean start(long gameId) throws Exception;

    boolean stop(long defId) throws Exception;

    boolean startChallenge(long challengeId) throws Exception;

    boolean resumeGame(long gameId) throws Exception;

    boolean resumeChallenge(long challengeId) throws Exception;
}
