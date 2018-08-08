package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.utils.UserRole;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.Pojos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author iweerarathna
 */
class UtilsTest {

    @Test
    void testAuth() throws Exception {
        AuthUtils.get().init();

        AuthUtils.TokenInfo tokenInfo = new AuthUtils.TokenInfo();
        tokenInfo.setUser(123);
        tokenInfo.setExp(System.currentTimeMillis() + 2000000);
        String t = AuthUtils.get().issueToken(tokenInfo);
        Assertions.assertNotNull(t);

        AuthUtils.TokenInfo parsed = AuthUtils.get().verifyToken(t);
        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(123, parsed.getUser());
        Assertions.assertFalse(UserRole.hasRole(parsed.getRole(), UserRole.ADMIN));
        Assertions.assertFalse(UserRole.hasRole(parsed.getRole(), UserRole.CURATOR));
        Assertions.assertTrue(UserRole.hasRole(parsed.getRole(), UserRole.PLAYER));
        Assertions.assertTrue(parsed.getExp() > 0);
    }

    @Test
    void testMaps() {
        Map<String, Object> map = Maps.create().build();
        Assertions.assertEquals(map.size(), 0);

        map = Maps.create("user", 1);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(map.get("user"), 1);

        map = Maps.create().put("a", 1).put("b", 2).build();
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(map.get("a"), 1);
        Assertions.assertEquals(map.get("b"), 2);
    }

    @Test
    void testPojos() {
        Assertions.assertEquals("a", Pojos.compareWith("a", "b"));
        Assertions.assertEquals("a", Pojos.compareWith("a", "a"));
        Assertions.assertEquals("b", Pojos.compareWith("b", "a"));
        Assertions.assertEquals("b", Pojos.compareWith("b", null));
        Assertions.assertEquals("b", Pojos.compareWith("b", ""));
        Assertions.assertEquals("a", Pojos.compareWith(null, "a"));


        Assertions.assertEquals(1, (int) Pojos.compareWith(1, 2));
        Assertions.assertEquals(2, (int) Pojos.compareWith(2, 2));
        Assertions.assertEquals(2, (int) Pojos.compareWith(2, 1));
        Assertions.assertEquals(2, (int) Pojos.compareWith(2, null));
        Assertions.assertEquals(1, (int) Pojos.compareWith(null, 1));
    }

}
