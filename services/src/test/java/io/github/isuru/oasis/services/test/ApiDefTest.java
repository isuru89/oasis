package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.model.ShopItem;
import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.GameOptionsDto;
import javafx.util.Pair;
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

    @Test
    void testGameDefApi() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        GameOptionsDto optionsDto = new GameOptionsDto();
        optionsDto.setAwardPointsForMilestoneCompletion(false);
        optionsDto.setAwardPointsForBadges(false);
        optionsDto.setAllowPointCompensation(false);

        GameDef gameDef = new GameDef();
        gameDef.setName("game-of-code");
        gameDef.setDisplayName("Game Of Code");
        Long gameId = gameDefService.createGame(gameDef, optionsDto);
        Assertions.assertTrue(gameId > 0);

        GameDef gameDef2 = new GameDef();
        gameDef2.setName("game-of-code-2");
        gameDef2.setDisplayName("Game Of Code - v2");
        Long gameId2 = gameDefService.createGame(gameDef2, optionsDto);
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
    void testGameDefApiWithSpecialPointRules() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        {
            // ALL three rules enabled...
            GameOptionsDto optionsDto = new GameOptionsDto();
            optionsDto.setAwardPointsForMilestoneCompletion(true);
            optionsDto.setAwardPointsForBadges(true);
            optionsDto.setAllowPointCompensation(true);
            long gameId = createGame("game-3", "Game 3", optionsDto);

            assertToSize(gameDefService.listPointDefs(gameId), 3);
            Assertions.assertTrue(gameDefService.disableGame(gameId));
            assertEmpty(gameDefService.listPointDefs(gameId));
        }
        {
            // ALL three rules enabled...
            GameOptionsDto optionsDto = new GameOptionsDto();
            optionsDto.setAwardPointsForMilestoneCompletion(true);
            optionsDto.setAwardPointsForBadges(false);
            optionsDto.setAllowPointCompensation(true);
            long gameId = createGame("game-2", "Game 2", optionsDto);

            assertToSize(gameDefService.listPointDefs(gameId), 2);
            Assertions.assertTrue(gameDefService.disableGame(gameId));
            assertEmpty(gameDefService.listPointDefs(gameId));
        }
        {
            // ALL three rules enabled...
            GameOptionsDto optionsDto = new GameOptionsDto();
            optionsDto.setAwardPointsForMilestoneCompletion(false);
            optionsDto.setAwardPointsForBadges(false);
            optionsDto.setAllowPointCompensation(true);
            long gameId = createGame("game-1", "Game 1", optionsDto);

            assertToSize(gameDefService.listPointDefs(gameId), 1);
            Assertions.assertTrue(gameDefService.disableGame(gameId));
            assertEmpty(gameDefService.listPointDefs(gameId));
        }
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
        long id = gameDefService.addKpiCalculation(gameId, kpiDef);

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

        long id = gameDefService.addPointDef(gameId, def);
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
        long id = gameDefService.addMilestoneDef(gameId, def);
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
        long id = gameDefService.addBadgeDef(gameId, def);
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

        List<LeaderboardDef> defLbs = gameDefService.listLeaderboardDefs(gameId);
        Assertions.assertEquals(1, defLbs.size());

        LeaderboardDef def = new LeaderboardDef();
        def.setName("top-resolver");
        def.setDisplayName("Top Resolvers");
        def.setOrderBy("desc");
        def.setRuleIds(Arrays.asList("ticket-resolved", "ticket-closed"));
        long lid = gameDefService.addLeaderboardDef(gameId, def);
        Assertions.assertTrue(lid > 0);

        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        Assertions.assertNotNull(leaderboardDefs);
        Assertions.assertEquals(2, leaderboardDefs.size());

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
        List<LeaderboardDef> leaderboardDefList = gameDefService.listLeaderboardDefs(gameId);
        Assertions.assertEquals(1, leaderboardDefList.size());

        Assertions.assertTrue(gameDefService.disableGame(gameId));
        Assertions.assertTrue(gameDefService.listLeaderboardDefs(gameId).isEmpty());

        // non existing leaderboard id should throw exception
        try {
            gameDefService.readLeaderboardDef(Integer.MAX_VALUE - 1000);
            Assertions.fail("Non existing leaderboard read should fail!");
        } catch (Exception ex) {
            // ok ignored
        }
    }

    @Test
    void testShopItems() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-shop-test", "Testing shop");
        Assertions.assertTrue(gameId > 0);

        long item1Id;
        {
            ShopItem shopItem = new ShopItem();
            shopItem.setTitle("Tango");
            shopItem.setDescription("Consumes a target tree to gain 7 health regeneration for 16 seconds.");
            shopItem.setLevel(1);
            shopItem.setScope("Consumable");
            shopItem.setPrice(90);

            long sid = gameDefService.addShopItem(gameId, shopItem);
            item1Id = sid;
            Assertions.assertTrue(sid > 0);

            List<ShopItem> items = gameDefService.listShopItems(gameId);
            Assertions.assertNotNull(items);
            Assertions.assertEquals(1, items.size());
            Assertions.assertEquals(sid, (long) items.get(0).getId());

            ShopItem item = gameDefService.readShopItem(sid);
            Assertions.assertNotNull(item);
            Assertions.assertTrue(item.getId() > 0);
            Assertions.assertEquals(item.getTitle(), shopItem.getTitle());
            Assertions.assertEquals(item.getDescription(), shopItem.getDescription());
            Assertions.assertEquals(item.getLevel(), shopItem.getLevel());
            Assertions.assertEquals(item.getScope(), shopItem.getScope());
            Assertions.assertEquals(item.getPrice(), shopItem.getPrice());
            Assertions.assertNull(item.getExpirationAt());
            Assertions.assertNull(item.getImageRef());
        }

        ShopItem magicStick = new ShopItem();
        magicStick.setTitle("Magic Stick");
        magicStick.setDescription("Instantly restores 15 health and mana per charge stored.");
        magicStick.setPrice(200);
        magicStick.setScope("Arcane");
        magicStick.setLevel(3);
        magicStick.setExpirationAt(System.currentTimeMillis() + 8400000);
        magicStick.setImageRef("/images/item/magic_stick.png");

        long sid = gameDefService.addShopItem(gameId, magicStick);
        Assertions.assertTrue(sid > 0);

        List<ShopItem> items = gameDefService.listShopItems(gameId);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());

        ShopItem item = gameDefService.readShopItem(sid);
        Assertions.assertNotNull(item);
        Assertions.assertTrue(item.getId() > 0);
        Assertions.assertNotNull(item.getImageRef());
        Assertions.assertNotNull(item.getExpirationAt());
        Assertions.assertEquals(magicStick.getExpirationAt(), item.getExpirationAt());
        Assertions.assertEquals(magicStick.getImageRef(), item.getImageRef());

        Assertions.assertTrue(gameDefService.disableShopItem(item1Id));

        items = gameDefService.listShopItems(gameId);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size());

        // @TODO check read deleted shop item

        Assertions.assertTrue(gameDefService.disableShopItem(sid));
        Assertions.assertTrue(gameDefService.disableGame(gameId));
    }

    @Test
    void testChallenges() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        long gameId = createGame("game-challenge-test", "Testing challenges");
        Assertions.assertTrue(gameId > 0);

        ChallengeDef def = new ChallengeDef();
        def.setName("holiday-surprise");
        def.setDisplayName("Holiday Surprise");
        def.setForEvents(Collections.singletonList("ticket-resolve"));
        def.setConditions(Collections.singletonList("true"));
        def.setExpireAfter(System.currentTimeMillis() + 8400000);
        def.setWinnerCount(100);
        def.setPoints(200);

        long cid = gameDefService.addChallenge(gameId, def);
        Assertions.assertTrue(cid > 0);

        List<ChallengeDef> challengeDefs = gameDefService.listChallenges(gameId);
        Assertions.assertNotNull(challengeDefs);
        Assertions.assertEquals(1, challengeDefs.size());

        ChallengeDef challenge = gameDefService.readChallenge(cid);
        Assertions.assertNotNull(challenge);
        Assertions.assertEquals(cid, (long) challenge.getId());
        Assertions.assertEquals(def.getName(), challenge.getName());
        Assertions.assertEquals(def.getDisplayName(), challenge.getDisplayName());
        Assertions.assertEquals(def.getPoints(), challenge.getPoints());
        Assertions.assertEquals(def.getForEvents().size(), challenge.getForEvents().size());
        Assertions.assertEquals(def.getConditions().size(), challenge.getConditions().size());
        Assertions.assertEquals(def.getForEvents().iterator().next(),
                challenge.getForEvents().iterator().next());
        Assertions.assertEquals(def.getConditions().iterator().next(),
                challenge.getConditions().iterator().next());
        Assertions.assertEquals(def.getExpireAfter(), challenge.getExpireAfter());
        Assertions.assertEquals(def.getWinnerCount(), challenge.getWinnerCount());
        Assertions.assertNull(challenge.getStartAt());

        Assertions.assertTrue(gameDefService.disableChallenge(cid));
        Assertions.assertTrue(gameDefService.disableGame(gameId));
    }

    private long createGame(String name, String displayName) throws Exception {
        GameOptionsDto optionsDto = new GameOptionsDto();
        optionsDto.setAllowPointCompensation(false);
        optionsDto.setAwardPointsForBadges(false);
        optionsDto.setAwardPointsForMilestoneCompletion(false);
        return createGame(name, displayName, optionsDto);
    }

    private long createGame(String name, String displayName, GameOptionsDto optionsDto) throws Exception {
        GameDef def = new GameDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return apiService.getGameDefService().createGame(def, optionsDto);
    }

    @BeforeEach
    @AfterEach
    void testBeforeAnyTest() throws Exception {
        IGameDefService gameDefService = apiService.getGameDefService();
        assertEmpty(gameDefService.listGames());
        assertEmpty(gameDefService.listBadgeDefs());
        assertEmpty(gameDefService.listKpiCalculations());
        assertEmpty(gameDefService.listMilestoneDefs());
        assertEmpty(gameDefService.listPointDefs(1));
    }

    private void assertEmpty(List<?> list) {
        Assertions.assertNotNull(list);
        Assertions.assertEquals(0, list.size());
    }

    private void assertToSize(List<?> list, int size) {
        Assertions.assertNotNull(list);
        Assertions.assertEquals(size, list.size());
    }

    @BeforeEach
    void teardownTest() throws Exception {
        clearTables("OA_DEFINITION");
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        dbStart();
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        dbClose("OA_SHOP_ITEM");
    }

}
