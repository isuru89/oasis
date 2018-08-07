package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.utils.Maps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author iweerarathna
 */
class RoutingPermissionTest extends RoutingTest {

    @Test
    void testEcho() throws Exception {
        Map<String, Object> body = shouldBeSuccess(getApi().echo());
        Assertions.assertTrue(body.containsKey("message"));
    }

    @Test
    void testAuth() throws Exception {
        {
            String up = "admin@oasis.com:admin1";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            String up = "curator@oasis.com:curatorx";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            String up = "player@oasis.com:";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            String up = "admin2@oasis.com:admin";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            String up = "admin2@oasis.com:admin1";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            // by giving user name should fail
            String up = "admin:admin";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            shouldForbidden(getApi().login("Basic " + encoded));
        }
        {
            String up = "admin@oasis.com:admin";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            Map<String, Object> map = shouldBeSuccess(getApi().login("Basic " + encoded));
            Assertions.assertNotNull(map);
            Object token = map.get("token");
            Assertions.assertNotNull(token);

            map = shouldBeSuccess(getApi().logout("Bearer " + token));
            Assertions.assertNotNull(map);
            Assertions.assertEquals(true, map.get("success"));
        }
        {
            String up = "curator@oasis.com:curator";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            Map<String, Object> map = shouldBeSuccess(getApi().login("Basic " + encoded));
            Assertions.assertNotNull(map);
            Object token = map.get("token");
            Assertions.assertNotNull(token);

            map = shouldBeSuccess(getApi().logout("Bearer " + token));
            Assertions.assertNotNull(map);
            Assertions.assertEquals(true, map.get("success"));
        }
        {
            String up = "player@oasis.com:player";
            String encoded = Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
            Map<String, Object> map = shouldBeSuccess(getApi().login("Basic " + encoded));
            Assertions.assertNotNull(map);
            Object token = map.get("token");
            Assertions.assertNotNull(token);

            map = shouldBeSuccess(getApi().logout("Bearer " + token));
            Assertions.assertNotNull(map);
            Assertions.assertEquals(true, map.get("success"));
        }
    }

    @BeforeAll
    static void runBefore() throws Exception {
        startServer();
        runBeforeApi();
        createUsers();
    }

    @AfterAll
    static void stop() {
        try {
            getDao().executeRawCommand("TRUNCATE OA_USER", Maps.create().build());
            getDao().executeRawCommand("TRUNCATE OA_TEAM", Maps.create().build());
            getDao().executeRawCommand("TRUNCATE OA_SCOPE", Maps.create().build());
            getDao().executeRawCommand("TRUNCATE OA_TEAM_USER", Maps.create().build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Spark.stop();
    }

}
