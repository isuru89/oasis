package io.github.isuru.oasis.services.services.scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DailySchedulerTest extends AbstractSchedulerTest {

    @Before
    public void before() throws Exception {
        long gameId = createGame();

        loadUserData();

        initPool(5);

        createPoints(gameId);
        createRaces(gameId);
    }

    @After
    public void after() {
        closePool();
    }

    @Test
    public void testDailyScheduler() {

    }
}
