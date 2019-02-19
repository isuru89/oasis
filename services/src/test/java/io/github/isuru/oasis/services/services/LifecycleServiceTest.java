package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LifecycleServiceTest extends AbstractServiceTest {

    @Autowired
    private LifecycleImplManager lifecycleImplManager;

    @Autowired
    private IGameDefService gameDefService;

    private long mainGameId;

    @Before
    public void before() throws Exception {
        resetSchema();

        ILifecycleService iLifecycleService = lifecycleImplManager.get();
        Assertions.assertThat(iLifecycleService).isInstanceOf(LocalLifeCycleServiceImpl.class);

        GameDef def = new GameDef();
        def.setName("so");
        def.setDisplayName("Stackoverflow");
        mainGameId = gameDefService.createGame(def, new GameOptionsDto());
        Assert.assertTrue(mainGameId > 0);
    }

    @Test
    public void testStartGame() throws Exception {
        Future<?> start = lifecycleImplManager.get().start(mainGameId);

        try {
            start.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            ;
        }

        lifecycleImplManager.get().stop(mainGameId);
    }

}
