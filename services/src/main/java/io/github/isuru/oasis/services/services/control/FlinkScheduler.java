package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import io.github.isuru.oasis.services.model.IGameController;
import io.github.isuru.oasis.services.services.dispatchers.DispatcherManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author iweerarathna
 */
@Component("schedulerRemote")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FlinkScheduler implements IGameController {

    private IEventDispatcher eventDispatcher;

    @Autowired
    public FlinkScheduler(DispatcherManager dispatcherManager) {
        this.eventDispatcher = dispatcherManager.getEventDispatcher();
    }

    @Override
    public void submitEvent(long gameId, String token, Map<String, Object> event) throws Exception {
        eventDispatcher.dispatch(gameId, event);
    }

    @Override
    public Future<?> startGame(long gameId) throws Exception {
        return null;
    }

    @Override
    public void startChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void stopChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void stopGame(long gameId) {

    }

    @Override
    public void resumeChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void resumeGame(GameDef gameDef) throws Exception {

    }

    @Override
    public void close() throws IOException {

    }
}
