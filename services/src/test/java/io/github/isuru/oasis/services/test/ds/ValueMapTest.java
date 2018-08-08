package io.github.isuru.oasis.services.test.ds;

import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.test.AbstractApiTest;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.ValueMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * @author iweerarathna
 */
class ValueMapTest extends AbstractApiTest {

    @Test
    void testValueMap() throws InputValidationException {
        {
            ValueMap valueMap = new ValueMap(new HashMap<>());
            Assertions.assertFalse(valueMap.has("abc"));
            assertFail(() -> valueMap.getLongReq("nonextistinglong"), InputValidationException.class);
        }
        {
            ValueMap map = new ValueMap(Maps.create().put("a", 1L)
                    .put("b", "str")
                    .put("c", 1)
                    .put("d", 0.5f)
                    .build());
            Assertions.assertTrue(map.has("a"));
            Assertions.assertTrue(map.has("b"));
            Assertions.assertTrue(map.has("c"));
            Assertions.assertEquals(map.getLongReq("a"), 1L);
            Assertions.assertEquals(map.getStrReq("b"), "str");
            Assertions.assertEquals(map.getIntReq("c"), 1);
            Assertions.assertEquals(map.getFloatReq("d"), 0.5f);
            Assertions.assertEquals(map.getLong("a", 3L), 1L);
            Assertions.assertEquals(map.getStr("b", "abc"), "str");
            Assertions.assertEquals(map.getInt("c", 42), 1);
            Assertions.assertEquals(map.getFloat("d", 9.99f), 0.5f);
            Assertions.assertEquals(map.getInt("e", 4), 4);
            Assertions.assertEquals(map.getStr("f", "game"), "game");
            Assertions.assertEquals(map.getLong("g", 100L), 100L);
            Assertions.assertEquals(map.getFloat("h", 1.34f), 1.34f);

            assertFail(() -> map.getIntReq("nei"), InputValidationException.class);
            assertFail(() -> map.getFloatReq("nef"), InputValidationException.class);
            assertFail(() -> map.getStrReq("nes"), InputValidationException.class);
        }

    }


}
