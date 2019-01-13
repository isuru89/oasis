package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.services.dto.defs.AddGameDto;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.test.dto.AddResponse;
import io.github.isuru.oasis.services.test.dto.BoolResponse;
import io.github.isuru.oasis.services.utils.Maps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.util.List;

/**
 * @author iweerarathna
 */
class RouteDefTest extends RoutingTest {

    @Test
    void gameTest() throws Exception {
        GameDef gdef = new GameDef();
        gdef.setName("testing-game");
        gdef.setDisplayName("This is a test game");
        gdef.setConstants(Maps.create("MY_MAGIC_VALUE", 100));

        GameOptionsDto optionsDto = new GameOptionsDto();
        AddGameDto gameDto = new AddGameDto(gdef, optionsDto);

        {
            // no admin game add failure
            shouldForbidden(defApi.addGame(getNoUserToken(), gameDto));
            shouldForbidden(defApi.addGame(getCuratorToken(), gameDto));
            shouldForbidden(defApi.addGame(getPlayerToken(), gameDto));

            AddResponse addResponse = shouldBeSuccess(defApi.addGame(getAdminToken(), gameDto));
            long gid = addResponse.getId();
            Assertions.assertTrue(addResponse.isSuccess());
            Assertions.assertTrue(gid > 0);
            List<GameDef> gameDefs = shouldBeSuccess(defApi.listGames());
            Assertions.assertEquals(1, gameDefs.size());
            GameDef gameDef = gameDefs.get(0);
            Assertions.assertNotNull(gameDef.getId());
            Assertions.assertEquals(gameDef.getName(), gdef.getName());

            GameDef gd = shouldBeSuccess(defApi.readGame(gid));
            Assertions.assertEquals(gd.getId(), gameDef.getId());

            shouldForbidden(defApi.disableGame(getNoUserToken(), gid));
            shouldForbidden(defApi.disableGame(getCuratorToken(), gid));
            shouldForbidden(defApi.disableGame(getPlayerToken(), gid));
            BoolResponse boolResponse = shouldBeSuccess(defApi.disableGame(getAdminToken(), gid));
            Assertions.assertTrue(boolResponse.isSuccess());
        }
    }

    @Test
    void kpiTest() throws Exception {
        KpiDef kpiDef = new KpiDef();

        long gameId = addGame("kpi-test", "Testing kpi game");
        shouldForbidden(defApi.addKpi(getNoUserToken(), gameId, kpiDef));
        shouldForbidden(defApi.addKpi(getCuratorToken(), gameId, kpiDef));
        shouldForbidden(defApi.addKpi(getPlayerToken(), gameId, kpiDef));

    }

    long addGame(String name, String displayName) throws IOException {
        GameDef def = new GameDef();
        def.setName(name);
        def.setDisplayName(displayName);
        AddResponse addResponse = shouldBeSuccess(defApi.addGame(getAdminToken(),
                new AddGameDto(def, new GameOptionsDto())));
        return addResponse.getId();
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        startServer();
        runBeforeApi();
    }

    @AfterAll
    static void afterAll() {
        try {
            if (getDao() != null) {
                getDao().executeRawCommand("TRUNCATE OA_USER", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM_SCOPE", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_TEAM_USER", Maps.create().build());
                getDao().executeRawCommand("TRUNCATE OA_DEFINITION", Maps.create().build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Spark.stop();
    }

    @AfterEach
    void afterEach() {
        try {
            if (getDao() != null) {
                getDao().executeRawCommand("TRUNCATE OA_DEFINITION", Maps.create().build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
