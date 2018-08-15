package io.github.isuru.oasis.services.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author iweerarathna
 */
class ShopTest extends AbstractApiTest {

    @Test
    void testShop() throws Exception {

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
