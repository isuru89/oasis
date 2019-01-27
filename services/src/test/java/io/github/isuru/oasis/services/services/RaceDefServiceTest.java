package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.services.exception.InputValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RaceDefServiceTest extends BaseDefServiceTest {

    private long gameId;

    private LeaderboardDef l1;
    private LeaderboardDef l2;

    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        gameId = savedGame.getId();

        // adds a new leaderboard refs
        {
            l1 = new LeaderboardDef();
            l1.setName("Leaderboard-1");
            l1.setDisplayName("Sample 1");
            l1.setRuleIds(Arrays.asList("a", "b"));
            l1.setOrderBy("desc");
            l1.setAggregatorType("SUM");
            long l = ds.addLeaderboardDef(gameId, l1);
            l1 = ds.readLeaderboardDef(l);
        }

        {
            l2 = new LeaderboardDef();
            l2.setName("Leaderboard-2");
            l2.setDisplayName("Sample 2");
            l2.setExcludeRuleIds(Collections.singletonList("x"));
            l2.setOrderBy("asc");
            l2.setAggregatorType("MAX");
            long l = ds.addLeaderboardDef(gameId, l2);
            l2 = ds.readLeaderboardDef(l);
        }

        Assert.assertEquals(3, ds.listLeaderboardDefs(gameId).size());
    }

    @Test
    public void testRaceAddFailures() throws Exception {

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);

            // invalid leaderboard ids
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("lb1", "Leaderboard-1", -1)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("lb1", "Leaderboard-1", 0)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("lb1", "Leaderboard-1", 9999)))
                    .isInstanceOf(InputValidationException.class);

            {
                // not having specified of top N
                RaceDef race1 = create("lb1", "Leaderboard-1", l1.getId().intValue());
                Assertions.assertThatThrownBy(() -> ds.addRace(gameId, race1))
                        .isInstanceOf(InputValidationException.class);

                {
                    RaceDef r2 = clone(race1);
                    r2.setTop(0);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);
                }

                // cannot have both point expr and point map
                {
                    RaceDef r2 = clone(race1);
                    r2.setTop(10);
                    r2.setRankPointsExpression("$points");
                    Map<Integer, Double> awards = new HashMap<>();
                    awards.put(1, 1000.0);
                    awards.put(2, 500.0);
                    r2.setRankPoints(awards);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);
                }
            }
        }
    }

    @Test
    public void testRaceAdds() throws Exception {
        int size = getTotalCount(gameId);

        RaceDef def = create("reputations", "Total Reputation", l1.getId().intValue());
        def.setTop(10);
        def.setRankPointsExpression("(10 - $rank) * 10");

        {
            long defId = addAssert(gameId, def);

            RaceDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addRace(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = getTotalCount(gameId);

            // with description and display name
            RaceDef cloned = clone(def);
            cloned.setName("reputation-new");
            cloned.setDisplayName("Total Reputation - Updated");
            cloned.setDescription("Sum of reputation a user has gathered forever.");

            long kpiId = addAssert(gameId, cloned);
            readAssert(kpiId, cloned);

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add same kpi to a different game must be successful
            GameDef gameNew = createSavedGame("so-updated", "Updated Stackoverflow");
            int sizeNew = getTotalCount(gameNew.getId());


            LeaderboardDef ln1 = new LeaderboardDef();
            ln1.setName("Leaderboard-1");
            ln1.setDisplayName("Sample 1");
            ln1.setRuleIds(Arrays.asList("a", "b"));
            ln1.setOrderBy("desc");
            ln1.setAggregatorType("SUM");
            long l = ds.addLeaderboardDef(gameNew.getId(), ln1);
            ln1 = ds.readLeaderboardDef(l);

            RaceDef clone = clone(def);
            clone.setLeaderboardId(ln1.getId().intValue());
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            checkTotalCount(gameNew.getId(), sizeNew + 1);
        }
    }

    @Test
    public void testRaceTypeAdds() throws Exception {
        // write tests for different types of points

        {
            int size = getTotalCount(gameId);

            // points with a condition
            RaceDef def = create("reputation", "Increase reputation on Votes", l1.getId().intValue());
            def.setTop(20);
            addRankPointMap(def);

            RaceDef addedDef = readAssert(addAssert(gameId, def), def);
            Assert.assertEquals("weekly", addedDef.getTimeWindow());
            Assert.assertEquals("TEAM_SCOPE", addedDef.getFromScope());

            checkTotalCount(gameId, size + 1);
        }

        {
            // points with a condition class
            int size = getTotalCount(gameId);

            RaceDef def = create("reputation-down", "Decrease reputation on down votes");
            def.setTop(10);
            def.setRankPointsExpression("$points");
            def.setTimeWindow("daily");
            def.setFromScope("team_scope");

            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }

        {
            // create a point definition with awarding additional points to a different user
            int size = getTotalCount(gameId);

            RaceDef def = create("bounty-reward", "Award Reputation for Bounty question");
            def.setTop(10);
            def.setRankPointsExpression("(10-$rank) * 20");
            def.setTimeWindow("monthly");
            def.setFromScope("team");

            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }

        {
            // a single event may generate multiple points
            // a rule can be created to award bonus points based on total points of a event
            int size = getTotalCount(gameId);

            RaceDef def = create("reputation-bonus", "Bonus reputation on up votes");

            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }
    }

    @Test
    public void testRaceDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableRace(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableRace(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableRace(9999L));
        }

        int defSize = getTotalCount(gameId);

        RaceDef def1 = create("votes", "Total Votes", l1.getId().intValue());
        def1.setTop(10);
        addRankPointMap(def1);
        RaceDef def2 = create("stars", "Total Stars", l2.getId().intValue());
        def2.setTop(20);
        addRankPointMap(def2);

        RaceDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        RaceDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        checkTotalCount(gameId, defSize + 2);

        {
            // disable def-1
            Assert.assertTrue(ds.disableRace(addedDef1.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize + 1);

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableRace(addedDef2.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize);

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // after disabling, user should be able to add new with a same name again
            RaceDef clone = clone(def1);
            readAssert(addAssert(gameId, clone), clone);
        }
    }

    private int getTotalCount(long gameId) throws Exception {
        return ds.listRaces(gameId).size();
    }

    private void checkTotalCount(long gameId, int expected) throws Exception {
        Assertions.assertThat(getTotalCount(gameId)).isEqualTo(expected);
    }

    private long addAssert(long gameId, RaceDef def) throws Exception {
        long l = ds.addRace(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private RaceDef readAssert(long id) throws Exception {
        RaceDef def = ds.readRace(id);
        Assert.assertNotNull(def);
        Assert.assertEquals(id, def.getId().longValue());
        return def;
    }

    private RaceDef readAssert(long id, RaceDef check) throws Exception {
        RaceDef addedDef = ds.readRace(id);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(id, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        Assert.assertEquals(check.getDescription(), addedDef.getDescription());
        Assert.assertEquals(check.getLeaderboardId(), addedDef.getLeaderboardId());
        Assert.assertEquals(check.getTop(), addedDef.getTop());
        Assert.assertEquals(check.getRankPointsExpression(), addedDef.getRankPointsExpression());
        Assert.assertEquals(check.getFromScope(), addedDef.getFromScope());
        Assert.assertEquals(check.getTimeWindow(), addedDef.getTimeWindow());
        if (check.getRankPoints() != null) {
            Assertions.assertThat(addedDef.getRankPoints())
                    .isNotNull()
                    .hasSize(check.getRankPoints().size());
            // @TODO check map for map integrity
        } else {
            Assert.assertNull(addedDef.getRankPoints());
        }
        return addedDef;
    }

    private RaceDef clone(RaceDef other) {
        RaceDef def = new RaceDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        def.setFromScope(other.getFromScope());
        def.setLeaderboardId(other.getLeaderboardId());
        def.setRankPointsExpression(other.getRankPointsExpression());
        def.setRankPoints(other.getRankPoints());
        def.setTimeWindow(other.getTimeWindow());
        def.setTop(other.getTop());
        return def;
    }

    private RaceDef create(String name, String displayName) {
        return create(name, displayName, 0);
    }

    private RaceDef create(String name, String displayName, int leaderboardId) {
        RaceDef def = new RaceDef();
        def.setName(name);
        def.setDisplayName(displayName);
        def.setLeaderboardId(leaderboardId);
        return def;
    }

    private void addRankPointMap(RaceDef def) {
        Map<Integer, Double> awards = new HashMap<>();
        awards.put(1, 1000.0);
        awards.put(2, 500.0);
        def.setRankPoints(awards);
    }


}
