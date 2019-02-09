package io.github.isuru.oasis.game;


import org.junit.Test;

/**
 * @author iweerarathna
 */
public class BadgesTest extends AbstractTest {

    @Test
    public void testBadges() throws Exception {
        beginTest("badge-test1");
    }

    @Test
    public void testTimeBadges() throws Exception {
        beginTest("badge-time-test");
    }

    @Test
    public void testBadgesFromPoints() throws Exception {
        beginTest("badge-test-points");
    }

    @Test
    public void testAggBadges() throws Exception {
        beginTest("badge-agg-points");
    }

    @Test
    public void testBadgeHisto() throws Exception {
        beginTest("badge-histogram");
    }

    @Test
    public void testBadgeHistoSep() throws Exception {
        beginTest("badge-histogram-sep");
    }
}
