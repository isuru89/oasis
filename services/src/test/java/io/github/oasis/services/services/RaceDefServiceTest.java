/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services;

import io.github.oasis.model.db.DbException;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.model.defs.LeaderboardDef;
import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.model.defs.ScopingType;
import io.github.oasis.services.exception.InputValidationException;
import io.github.oasis.services.utils.Checks;
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
    public void testRaceAddFailures() {

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
                    () -> ds.addRace(gameId, create("lb1", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("lb1", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addRace(gameId, create("lb1", "\t ")))
                    .isInstanceOf(InputValidationException.class);

            {
                // not having specified of top N
                RaceDef race1 = create("lb1", "Leaderboard-1", l1.getId().intValue());
                Assertions.assertThatThrownBy(() -> ds.addRace(gameId, race1))
                        .isInstanceOf(InputValidationException.class);

                {
                    // invalid top value
                    RaceDef r2 = clone(race1);
                    r2.setTop(0);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);
                }

                {
                    // cannot define both top and min point threshold value
                    RaceDef r2 = clone(race1);
                    r2.setTop(50);
                    r2.setMinPointThreshold(100.0);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class)
                            .hasMessage(String.format(Checks.MSG_HAVING_BOTH, "top", "minPointThreshold"));
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

                // field 'fromScope' cannot null or empty, and must be one of valid values
                {
                    RaceDef r2 = clone(race1);
                    r2.setTop(10);
                    addRankPointMap(r2);
                    r2.setFromScope(null);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r3 = clone(r2);
                    r3.setFromScope("  ");
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r3))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r4 = clone(r2);
                    r4.setFromScope("unknown");
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r4))
                            .isInstanceOf(InputValidationException.class);
                }

                // field 'timeWindow' cannot be null or empty, must be one of valid values
                {
                    RaceDef r2 = clone(race1);
                    r2.setTop(10);
                    addRankPointMap(r2);
                    r2.setFromScope(ScopingType.TEAM_SCOPE.name());
                    r2.setTimeWindow(null);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r3 = clone(r2);
                    r3.setTimeWindow("  ");
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r3))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r4 = clone(r2);
                    r4.setTimeWindow("unknown");
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r4))
                            .isInstanceOf(InputValidationException.class);
                }

                // invalid leaderboard ids
                {
                    RaceDef r2 = clone(race1);
                    r2.setLeaderboardId(0);
                    r2.setTop(10);
                    r2.setRankPointsExpression("$points");
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r2))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r3 = clone(r2);
                    r3.setLeaderboardId(-1);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r3))
                            .isInstanceOf(InputValidationException.class);

                    RaceDef r4 = clone(r2);
                    r4.setLeaderboardId(9999);
                    Assertions.assertThatThrownBy(() -> ds.addRace(gameId, r4))
                            .isInstanceOf(InputValidationException.class);
                }
            }
        }
    }

    @Test
    public void testRaceAdds() throws Exception {
        int size = getTotalCount(gameId);

        RaceDef def = create("lb1", "Leaderboard 1", l1.getId().intValue());
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
            cloned.setName("lb1-new");
            cloned.setDisplayName("Leaderboard 1 - Updated");
            cloned.setDescription("Sum of total reputations.");

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
            ln1.setAggregatorType("sum");
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

            // creates with a default values
            RaceDef def = create("lb1", "Leaderboard 1", l1.getId().intValue());
            def.setTop(20);
            addRankPointMap(def);

            RaceDef addedDef = readAssert(addAssert(gameId, def), def);
            Assert.assertEquals("weekly", addedDef.getTimeWindow());
            Assert.assertEquals("TEAM_SCOPE", addedDef.getFromScope());

            checkTotalCount(gameId, size + 1);
        }

        {
            // create with team_scope and daily
            int size = getTotalCount(gameId);

            RaceDef def = create("lb1-down", "Leaderboard 2", l2.getId().intValue());
            def.setTop(10);
            def.setRankPointsExpression("$points");
            def.setTimeWindow("daily");
            def.setFromScope("team_scope");

            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }

        {
            // create with custom scope and timewindow
            int size = getTotalCount(gameId);

            RaceDef def = create("l2-v2", "Leaderboard 1", l2.getId().intValue());
            def.setTop(10);
            def.setRankPointsExpression("(10-$rank) * 20");
            def.setTimeWindow("monthly");
            def.setFromScope("team");

            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }

        {
            // create with upper case
            int size = getTotalCount(gameId);

            RaceDef def = create("l1-v2", "Leaderboard 2", l1.getId().intValue());
            def.setTop(10);
            def.setRankPointsExpression("(10-$rank) * 20");
            def.setTimeWindow("WEEKLY");
            def.setFromScope("GLOBAL");

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
        if (other.getRankPoints() != null) {
            def.setRankPoints(new HashMap<>(other.getRankPoints()));
        }
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
