package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.PointsAdditional;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
class ApiDefTest extends AbstractApiTest {

    private static IOasisDao oasisDao;
    private static IOasisApiService apiService;

    @Test
    void testGameDefApi() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        GameDef gameDef = new GameDef();
        gameDef.setName("game-of-code");
        gameDef.setDisplayName("Game Of Code");
        Long gameId = gameDefService.createGame(gameDef);
        Assertions.assertTrue(gameId > 0);

        GameDef gameDef2 = new GameDef();
        gameDef2.setName("game-of-code-2");
        gameDef2.setDisplayName("Game Of Code - v2");
        Long gameId2 = gameDefService.createGame(gameDef2);
        Assertions.assertTrue(gameId2 > 0);
        Assertions.assertTrue(gameId2 > gameId);

        List<GameDef> gameDefs = gameDefService.listGames();
        Assertions.assertNotNull(gameDefs);
        Assertions.assertEquals(gameDefs.size(), 2);

        assertEmpty(gameDefService.listPointDefs(gameId));
        assertEmpty(gameDefService.listMilestoneDefs(gameId));
        assertEmpty(gameDefService.listKpiCalculations(gameId));
        assertEmpty(gameDefService.listBadgeDefs(gameId));

        GameDef game = gameDefService.readGame(gameId);
        Assertions.assertNotNull(game);
        Assertions.assertTrue(game.getId() > 0);
        Assertions.assertEquals(game.getName(), gameDef.getName());
        Assertions.assertEquals(game.getDisplayName(), gameDef.getDisplayName());
        Assertions.assertEquals(game.getConstants(), gameDef.getConstants());

        Map<String, Object> vars = new HashMap<>();
        vars.put("O_THRESHOLD", 50);
        Assertions.assertTrue(gameDefService.addGameConstants(gameId, vars));

        game = gameDefService.readGame(gameId);
        Assertions.assertNotNull(game);
        Assertions.assertTrue(game.getId() > 0);
        Assertions.assertNotNull(game.getConstants());
        Assertions.assertEquals(game.getConstants().size(), vars.size());
        Assertions.assertEquals(game.getConstants().get("O_THRESHOLD"), vars.get("O_THRESHOLD"));

        Assertions.assertTrue(gameDefService.removeGameConstants(gameId, Collections.singletonList("O_THRESHOLD")));
        game = gameDefService.readGame(gameId);
        Assertions.assertNotNull(game.getConstants());
        Assertions.assertEquals(0, game.getConstants().size());

        Assertions.assertTrue(gameDefService.disableGame(gameId));
        Assertions.assertTrue(gameDefService.disableGame(gameId2));
    }

    @Test
    void testKpiDefTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-kpi-test", "Testing kpi");
        Assertions.assertTrue(gameId > 0);

        KpiDef kpiDef = new KpiDef();
        kpiDef.setEvent("submission");
        kpiDef.setField("totalCode");
        kpiDef.setExpression("addedLines + changedLines");
        String name = kpiDef.assignName();
        long id = gameDefService.addKpiCalculation(kpiDef);

        KpiDef kpi = gameDefService.readKpiCalculation(id);
        Assertions.assertEquals(kpi.getEvent(), kpiDef.getEvent());
        Assertions.assertEquals(kpi.getExpression(), kpiDef.getExpression());
        Assertions.assertEquals(kpi.getField(), kpiDef.getField());
        Assertions.assertEquals(kpi.getName(), name);

        assertToSize(gameDefService.listKpiCalculations(gameId), 1);
        assertEmpty(gameDefService.listBadgeDefs(gameId));
        assertEmpty(gameDefService.listPointDefs(gameId));
        assertEmpty(gameDefService.listMilestoneDefs(gameId));

        Assertions.assertTrue(gameDefService.disableKpiCalculation(id));
        Assertions.assertTrue(gameDefService.disableGame(gameId));
    }

    @Test
    void testPointDefTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-point-test", "Testing points");
        Assertions.assertTrue(gameId > 0);

        PointDef def = new PointDef();
        def.setName("defect-assigned");
        def.setDisplayName("Defect Assignment");
        def.setCondition("type == 'defect-assigned'");
        def.setEvent("jira-event");
        def.setAmount(2);
        PointsAdditional p1 = new PointsAdditional();
        p1.setName("defect-assigned-to");
        p1.setToUser("assginedTo");
        p1.setAmount(-3);
        def.setAdditionalPoints(Collections.singletonList(p1));

        long id = gameDefService.addPointDef(def);
        Assertions.assertTrue(id > 0);

        PointDef point = gameDefService.readPointDef(id);
        Assertions.assertNotNull(point);

        Assertions.assertEquals((long) point.getId(), id);
        Assertions.assertEquals(point.getName(), def.getName());
        Assertions.assertEquals(point.getDisplayName(), def.getDisplayName());
        Assertions.assertEquals(point.getCondition(), def.getCondition());
        Assertions.assertEquals(point.getEvent(), def.getEvent());
        Assertions.assertEquals(point.getSource(), def.getSource());
        Assertions.assertEquals(point.getAmount(), def.getAmount());
        Assertions.assertNotNull(point.getAdditionalPoints());
        Assertions.assertEquals(point.getAdditionalPoints().size(), 1);

        PointsAdditional additional = point.getAdditionalPoints().get(0);
        Assertions.assertEquals(additional.getName(), p1.getName());
        Assertions.assertEquals(additional.getToUser(), p1.getToUser());
        Assertions.assertEquals(additional.getAmount(), p1.getAmount());

        assertToSize(gameDefService.listPointDefs(gameId), 1);
        assertEmpty(gameDefService.listBadgeDefs(gameId));
        assertEmpty(gameDefService.listKpiCalculations(gameId));
        assertEmpty(gameDefService.listMilestoneDefs(gameId));

        Assertions.assertTrue(gameDefService.disablePointDef(id));
        Assertions.assertTrue(gameDefService.disableGame(gameId));
    }

    @Test
    void testMilestoneDefTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-milestone-test", "Testing milestones");
        Assertions.assertTrue(gameId > 0);

        MilestoneDef def = new MilestoneDef();
        def.setName("total-tickets-closed");
        def.setDisplayName("Total Tickets Closed");
        def.setAggregator("count");
        def.setCondition("status == 'ticket-resolved'");
        def.setAccumulatorType("long");
        def.setEvent("alm-helpdesk");
        Map<Integer, Object> levels = new HashMap<>();
        levels.put(1, 10);
        levels.put(2, 50);
        levels.put(3, 100);
        def.setLevels(levels);
        long id = gameDefService.addMilestoneDef(def);
        Assertions.assertTrue(id > 0);

        MilestoneDef ms = gameDefService.readMilestoneDef(id);
        Assertions.assertNotNull(ms);
        Assertions.assertNotNull(ms.getId());

        Assertions.assertEquals((long) ms.getId(), id);
        Assertions.assertEquals(ms.getName(), def.getName());
        Assertions.assertEquals(ms.getDisplayName(), def.getDisplayName());
        Assertions.assertEquals(ms.getAccumulator(), def.getAccumulator());
        Assertions.assertEquals(ms.getAccumulatorType(), def.getAccumulatorType());
        Assertions.assertEquals(ms.getAggregator(), def.getAggregator());
        Assertions.assertEquals(ms.getCondition(), def.getCondition());
        Assertions.assertEquals(ms.getEvent(), def.getEvent());
        Assertions.assertEquals(ms.getFrom(), def.getFrom());
        Assertions.assertNotNull(ms.getLevels());
        Assertions.assertTrue(ms.getLevels().size() > 0);
        Assertions.assertEquals(ms.getLevels().size(), def.getLevels().size());
        Assertions.assertEquals(ms.getLevels().get(1), def.getLevels().get(1));
        Assertions.assertEquals(ms.getLevels().get(2), def.getLevels().get(2));
        Assertions.assertEquals(ms.getLevels().get(3), def.getLevels().get(3));

        assertToSize(gameDefService.listMilestoneDefs(gameId), 1);
        assertEmpty(gameDefService.listBadgeDefs(gameId));
        assertEmpty(gameDefService.listPointDefs(gameId));
        assertEmpty(gameDefService.listKpiCalculations(gameId));

        Assertions.assertTrue(gameDefService.disableMilestoneDef(id));
        Assertions.assertTrue(gameDefService.disableGame(gameId));
    }

    @Test
    void testBadgeDefTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-badge-test", "Testing badges");
        Assertions.assertTrue(gameId > 0);

        BadgeDef def = new BadgeDef();
        def.setName("login");
        def.setDisplayName("First Login");
        def.setMaxBadges(1);
        def.setCondition("true");
        def.setEvent("login");
        long id = gameDefService.addBadgeDef(def);
        Assertions.assertTrue(id > 0);

        BadgeDef badge = gameDefService.readBadgeDef(id);
        Assertions.assertNotNull(badge);
        Assertions.assertNotNull(badge.getId());

        Assertions.assertEquals((long) badge.getId(), id);
        Assertions.assertEquals(badge.getName(), def.getName());
        Assertions.assertEquals(badge.getDisplayName(), def.getDisplayName());
        Assertions.assertEquals(badge.getCondition(), def.getCondition());
        Assertions.assertEquals(badge.getEvent(), def.getEvent());
        Assertions.assertEquals(badge.getMaxBadges(), def.getMaxBadges());

        assertToSize(gameDefService.listBadgeDefs(gameId), 1);
        assertEmpty(gameDefService.listKpiCalculations(gameId));
        assertEmpty(gameDefService.listPointDefs(gameId));
        assertEmpty(gameDefService.listMilestoneDefs(gameId));

        Assertions.assertTrue(gameDefService.disableBadgeDef(id));
        Assertions.assertTrue(gameDefService.disableGame(gameId));

        // non existing game id should throw exception
        try {
            gameDefService.readGame(Integer.MAX_VALUE - 1000);
            Assertions.fail("Non existing game read should fail!");
        } catch (Exception ex) {
            // ok ignored
        }
    }

    @Test
    void testLeaderboardTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-leaderboard-test", "Testing leaderboard");
        Assertions.assertTrue(gameId > 0);

        LeaderboardDef def = new LeaderboardDef();
        def.setName("top-resolver");
        def.setDisplayName("Top Resolvers");
        def.setOrderBy("desc");
        def.setRuleIds(Arrays.asList("ticket-resolved", "ticket-closed"));
        long lid = gameDefService.addLeaderboardDef(def);
        Assertions.assertTrue(lid > 0);

        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs();
        Assertions.assertNotNull(leaderboardDefs);
        Assertions.assertEquals(1, leaderboardDefs.size());

        LeaderboardDef leaderboard = gameDefService.readLeaderboardDef(lid);
        Assertions.assertNotNull(leaderboard);
        Assertions.assertEquals(lid, (long) leaderboard.getId());
        Assertions.assertEquals(def.getName(), leaderboard.getName());
        Assertions.assertEquals(def.getDisplayName(), leaderboard.getDisplayName());
        Assertions.assertEquals(def.getOrderBy(), leaderboard.getOrderBy());
        Assertions.assertNotNull(def.getRuleIds());
        Assertions.assertNull(def.getExcludeRuleIds());
        Assertions.assertEquals(def.getRuleIds().size(), leaderboard.getRuleIds().size());
        Assertions.assertTrue(def.getRuleIds().contains(leaderboard.getRuleIds().get(0)));
        Assertions.assertTrue(def.getRuleIds().contains(leaderboard.getRuleIds().get(1)));

        Assertions.assertTrue(gameDefService.disableLeaderboardDef(lid));
        List<LeaderboardDef> leaderboardDefList = gameDefService.listLeaderboardDefs();
        Assertions.assertTrue(leaderboardDefList.isEmpty());

        Assertions.assertTrue(gameDefService.disableGame(gameId));

        // non existing leaderboard id should throw exception
        try {
            gameDefService.readLeaderboardDef(Integer.MAX_VALUE - 1000);
            Assertions.fail("Non existing leaderboard read should fail!");
        } catch (Exception ex) {
            // ok ignored
        }
    }


    private long createGame(String name, String displayName) throws Exception {
        GameDef def = new GameDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return apiService.getGameDefService().createGame(def);
    }

    @BeforeEach
    @AfterEach
    void testBeforeAnyTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        assertEmpty(gameDefService.listGames());
        assertEmpty(gameDefService.listBadgeDefs());
        assertEmpty(gameDefService.listKpiCalculations());
        assertEmpty(gameDefService.listMilestoneDefs());
        assertEmpty(gameDefService.listPointDefs(0));
    }

    private void assertEmpty(List<?> list) {
        Assertions.assertNotNull(list);
        Assertions.assertEquals(0, list.size());
    }

    private void assertToSize(List<?> list, int size) {
        Assertions.assertNotNull(list);
        Assertions.assertEquals(size, list.size());
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        DbProperties properties = new DbProperties(OasisDbPool.DEFAULT);
        properties.setUrl("jdbc:mysql://localhost/oasis");
        properties.setUsername("isuru");
        properties.setPassword("isuru");
        File file = new File("./scripts/db");
        if (!file.exists()) {
            file = new File("../scripts/db");
            if (!file.exists()) {
                Assertions.fail("Database scripts directory is not found!");
            }
        }
        properties.setQueryLocation(file.getAbsolutePath());

        oasisDao = OasisDbFactory.create(properties);
        apiService = new DefaultOasisApiService(oasisDao);
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        System.out.println("Shutting down db connection.");
        try {
            oasisDao.executeRawCommand("TRUNCATE OA_DEFINITION", new HashMap<>());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oasisDao.close();
        apiService = null;
    }
}
