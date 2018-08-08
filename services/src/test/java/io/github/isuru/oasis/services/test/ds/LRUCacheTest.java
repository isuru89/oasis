package io.github.isuru.oasis.services.test.ds;

import io.github.isuru.oasis.services.utils.LRUCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author iweerarathna
 */
class LRUCacheTest {

    @Test
    void testCache() {
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        Assertions.assertEquals(2, cache.size());

        cache.put("c", 3);
        Assertions.assertEquals(2, cache.size());
        Assertions.assertFalse(cache.containsKey("a"));
        Assertions.assertTrue(cache.containsKey("b"));
        Assertions.assertTrue(cache.containsKey("c"));
    }

}
