package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.utils.Maps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.util.List;

/**
 * @author iweerarathna
 */
public class RouteControlTest extends RoutingTest {

    @Test
    void testStartGame() throws Exception {
        long gameId = -1;
        String token = getAdminToken();

        List<GameDef> gameDefs = shouldBeSuccess(defApi.listGames());
        gameId = gameDefs.get(0).getId();

//        try (InputStream inputStream = new FileInputStream("../scripts/examples/oasis.yml")) {
//            Yaml yaml = new Yaml();
//            OasisGameDef oasisGameDef = yaml.loadAs(inputStream, OasisGameDef.class);
//
//            GameOptionsDto optionsDto = new GameOptionsDto();
//            gameId = shouldBeSuccess(defApi.addGame(token,
//                    new AddGameDto(oasisGameDef.getGame(), optionsDto))).getId();
//
//            for (KpiDef kpiDef : oasisGameDef.getKpis()) {
//                shouldBeSuccess(defApi.addKpi(token, gameId, kpiDef));
//            }
//            for (PointDef pointDef : oasisGameDef.getPoints()) {
//                if (pointDef.getName().equals(EventNames.POINT_RULE_BADGE_BONUS_NAME)
//                        || pointDef.getName().equals(EventNames.POINT_RULE_MILESTONE_BONUS_NAME)
//                        || pointDef.getName().equals(EventNames.POINT_RULE_COMPENSATION_NAME)) {
//                    continue;
//                }
//                shouldBeSuccess(defApi.addPointRule(token, gameId, pointDef));
//            }
//            for (BadgeDef badgeDef : oasisGameDef.getBadges()) {
//                shouldBeSuccess(defApi.addBadgeRule(token, gameId, badgeDef));
//            }
//            for (MilestoneDef def : oasisGameDef.getMilestones()) {
//                shouldBeSuccess(defApi.addMilestoneRule(token, gameId, def));
//            }
//        }

        shouldBeSuccess(getApi().gameStart(token, gameId));

        shouldBeSuccess(defApi.listKpis(gameId));
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        startServer();
        runBeforeApi();

        try {
            if (getDao() != null) {
//                getDao().executeRawCommand("TRUNCATE OA_DEFINITION", Maps.create().build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void afterAll() {
        try {
            if (getDao() != null) {
                getDao().executeRawCommand("TRUNCATE OA_USER", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM_SCOPE", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM_USER", Maps.create().build());
                //getDao().executeRawCommand("TRUNCATE OA_DEFINITION", Maps.create().build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Spark.stop();
    }

}
