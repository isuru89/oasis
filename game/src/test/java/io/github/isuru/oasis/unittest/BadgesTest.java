package io.github.isuru.oasis.unittest;

import org.junit.jupiter.api.Test;

/**
 * @author iweerarathna
 */
class BadgesTest extends AbstractTest {

    @Test
    void testBadges() throws Exception {
        beginTest("badge-test1");
    }

    @Test
    void testTimeBadges() throws Exception {
        beginTest("badge-time-test");
    }

    @Test
    void testBadgesFromPoints() throws Exception {
        beginTest("badge-test-points");
    }

    @Test
    void testAggBadges() throws Exception {
        beginTest("badge-agg-points");
    }

    @Test
    void testBadgeHisto() throws Exception {
        beginTest("badge-histogram");
    }
}
