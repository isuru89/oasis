package io.github.isuru.oasis.services.services;

import java.util.concurrent.Future;

/**
 * @author iweerarathna
 */
public interface ILifecycleService {

    Future<?> start(long gameId) throws Exception;

    boolean stop(long gameId) throws Exception;

    boolean startChallenge(long challengeId) throws Exception;

    boolean stopChallenge(long challengeId) throws Exception;

    boolean resumeGame(long gameId) throws Exception;

    boolean resumeChallenge(long challengeId) throws Exception;
}
