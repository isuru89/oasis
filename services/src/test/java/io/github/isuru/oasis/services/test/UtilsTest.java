package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TokenInfo;
import io.github.isuru.oasis.services.utils.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;

/**
 * @author iweerarathna
 */
class UtilsTest extends AbstractApiTest {

    @Test
    void givenUsingPlainJava_whenGeneratingRandomStringUnbounded_thenCorrect() {
        String generatedString = RandomStringUtils.randomAlphanumeric(10);

        System.out.println(generatedString);
    }

    @Test
    void testChecks() throws InputValidationException {
        int a = 4;
        int b = 1;
        assertFail(() -> Checks.greaterThanZero(0, "lg"), InputValidationException.class);
        assertFail(() -> Checks.greaterThanZero(-1, "lg"), InputValidationException.class);
        Checks.greaterThanZero(1, "lg");

        assertFail(() -> Checks.nonNullOrEmpty((List)null, "list"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty((String) null, "str"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty((Map<?, ?>) null, "map"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty(new LinkedHashMap<>(), "map"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty("", "str"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty(" ", "str"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty(" \t", "str"), InputValidationException.class);
        assertFail(() -> Checks.nonNullOrEmpty(new ArrayList<>(), "arlist"), InputValidationException.class);
        assertFail(() -> Checks.nonNull(null, "obj"), InputValidationException.class);
        assertFail(() -> Checks.nonNegative(-2, "obj"), InputValidationException.class);
        assertFail(() -> Checks.validate(a < b, "obj"), InputValidationException.class);

        Checks.validate(1 < 3, "");
        Checks.validate(true, "");
        Checks.nonNegative(0, "");
        Checks.nonNegative(1, "");
        Checks.nonNull(new Object(), "");
        Checks.nonNull(3, "");
        Checks.nonNullOrEmpty(Maps.create("a", 1), "");
        Checks.nonNullOrEmpty(Arrays.asList(1, 2), "");
        Checks.nonNullOrEmpty("str", "");
    }


    @Test
    void testAuth() throws Exception {
        Properties properties = new Properties();
        Configs configs;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("oasis/configs/oasis.properties")) {
            properties.load(inputStream);
            configs = Configs.from(properties);
        }
        AuthUtils.get().init(configs);

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUser(123);
        String t = AuthUtils.get().issueToken(tokenInfo);
        Assertions.assertNotNull(t);

        TokenInfo parsed = AuthUtils.get().verifyToken(t);
        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(123, parsed.getUser());
        Assertions.assertFalse(UserRole.hasRole(parsed.getRole(), UserRole.ADMIN));
        Assertions.assertFalse(UserRole.hasRole(parsed.getRole(), UserRole.CURATOR));
        Assertions.assertTrue(UserRole.hasRole(parsed.getRole(), UserRole.PLAYER));
        //Assertions.assertTrue(parsed.getExp() > 0);
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
