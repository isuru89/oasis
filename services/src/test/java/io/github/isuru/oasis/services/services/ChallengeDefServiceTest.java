package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.crud.TeamProfileAddDto;
import io.github.isuru.oasis.services.dto.crud.TeamScopeAddDto;
import io.github.isuru.oasis.services.dto.crud.UserProfileAddDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ChallengeDefServiceTest extends BaseDefServiceTest {

    @Autowired
    private IProfileService ps;


    @Before
    public void beforeEach() throws Exception {
        verifyDefsAreEmpty();
    }

    @Test
    public void testChallengeAddFailures() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        {
            // invalid or insufficient parameters
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(0L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(-1L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(9999L, create(null, null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create("", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create(null, null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create("  ", null)))
                    .isInstanceOf(InputValidationException.class);

            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create("scholar", "")))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create("scholar", null)))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(
                    () -> ds.addChallenge(gameId, create("scholar", "\t ")))
                    .isInstanceOf(InputValidationException.class);

            {
                // non existing user for challenge must fail
                ChallengeDef def = create("answer-123-by-user", "Answer Q123 by userX");
                def.setForUser("isuru@domain.com");
                Assertions.assertThatThrownBy(() -> ds.addChallenge(gameId, def))
                        .isInstanceOf(InputValidationException.class);
            }

            {
                // non existing team for challenge must fail
                ChallengeDef def = create("answer-123-by-user", "Answer Q123 by userX");
                def.setForTeam("nonexist-team");
                Assertions.assertThatThrownBy(() -> ds.addChallenge(gameId, def))
                        .isInstanceOf(InputValidationException.class);
            }

            {
                // non existing team scope for challenge must fail
                ChallengeDef def = create("answer-123-by-user", "Answer Q123 by userX");
                def.setForTeamScope("nonexist-teamscope");
                Assertions.assertThatThrownBy(() -> ds.addChallenge(gameId, def))
                        .isInstanceOf(InputValidationException.class);
            }
        }
    }

    @Test
    public void testChallengeAdds() throws Exception {
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int size = getTotalCount(gameId);

        ChallengeDef def = create("First-Fix", "First Fix in Code");
        {
            long defId = addAssert(gameId, def);

            ChallengeDef addedDef = readAssert(defId);
            Assert.assertEquals(defId, addedDef.getId().longValue());
            Assert.assertEquals(def.getName(), addedDef.getName());
            Assert.assertEquals(def.getDisplayName(), addedDef.getDisplayName());

            // one more should be added
            checkTotalCount(gameId, size + 1);
        }

        {
            // add kpi with same name in to the same game should throw an error
            Assertions.assertThatThrownBy(() -> ds.addChallenge(gameId, clone(def)))
                    .isInstanceOf(DbException.class);
        }

        {
            size = getTotalCount(gameId);

            // with description and display name
            ChallengeDef cloned = clone(def);
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

            ChallengeDef clone = clone(def);
            long otherId = addAssert(gameNew.getId(), clone);
            readAssert(otherId, clone);
            checkTotalCount(gameNew.getId(), sizeNew + 1);
        }
    }

    @Test
    public void testChallengeTypeAdds() throws Exception {
        // write tests for different types of challenges
        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();

        // initialize user, team and a team scope
        TeamScopeAddDto teamScopeAddDto = new TeamScopeAddDto();
        teamScopeAddDto.setName("North");
        teamScopeAddDto.setDisplayName("The North");
        long scopeId = ps.addTeamScope(teamScopeAddDto);
        Assert.assertTrue(scopeId > 0);

        TeamProfileAddDto teamProfileAddDto = new TeamProfileAddDto();
        teamProfileAddDto.setName("winterfell");
        teamProfileAddDto.setTeamScope((int)scopeId);
        long teamId = ps.addTeam(teamProfileAddDto);
        Assert.assertTrue(teamId > 0);

        UserProfileAddDto userProfileAddDto = new UserProfileAddDto();
        userProfileAddDto.setName("ned stark");
        userProfileAddDto.setEmail("ned@winterfell.com");
        userProfileAddDto.setExtId(30001L);
        userProfileAddDto.setMale(true);
        long userId = ps.addUserProfile(userProfileAddDto);
        Assert.assertTrue(userId > 0);

        Assert.assertTrue(ps.addUserToTeam(userId, teamId, UserRole.CURATOR, false));

        {
            int size = getTotalCount(gameId);

            // create challenge for user
            ChallengeDef def = create("answer-123-by-ned", "Answer Q123 by Ned");
            def.setForUser("ned@winterfell.com");
            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }

        {
            int size = getTotalCount(gameId);

            // create challenge for user
            ChallengeDef def = create("answer-123-by-winterfell", "Answer Q123 by Winterfell");
            def.setForTeam("winterfell");
            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }


        {
            int size = getTotalCount(gameId);

            // create challenge for user
            ChallengeDef def = create("answer-123-by-ts", "Answer Q123 by North");
            def.setForTeamScope("North");
            readAssert(addAssert(gameId, def), def);

            checkTotalCount(gameId, size + 1);
        }
    }

    @Test
    public void testChallengeDisable() throws Exception {
        {
            // invalid disable params
            Assertions.assertThatThrownBy(() -> ds.disableChallenge(0L))
                    .isInstanceOf(InputValidationException.class);
            Assertions.assertThatThrownBy(() -> ds.disableChallenge(-1L))
                    .isInstanceOf(InputValidationException.class);

            // non existing
            Assert.assertFalse(ds.disableChallenge(9999L));
        }

        GameDef savedGame = createSavedGame("so", "Stackoverflow");
        long gameId = savedGame.getId();
        int defSize = getTotalCount(gameId);

        ChallengeDef def1 = create("votes", "Total Votes");
        ChallengeDef def2 = create("stars", "Total Stars");

        ChallengeDef addedDef1 = readAssert(addAssert(gameId, def1), def1);
        ChallengeDef addedDef2 = readAssert(addAssert(gameId, def2), def2);
        checkTotalCount(gameId, defSize + 2);

        {
            // disable def-1
            Assert.assertTrue(ds.disableChallenge(addedDef1.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize + 1);

            // ... but read does
            readAssert(addedDef1.getId());
        }

        {
            // disable def-2
            Assert.assertTrue(ds.disableChallenge(addedDef2.getId()));

            // listing should not return disabled ones...
            checkTotalCount(gameId, defSize);

            // ... but read does
            readAssert(addedDef2.getId());
        }

        {
            // @TODO after disabling, user should be able to add new with a same name again

        }
    }

    private int getTotalCount(long gameId) throws Exception {
        return ds.listChallenges(gameId).size();
    }

    private void checkTotalCount(long gameId, int expected) throws Exception {
        Assertions.assertThat(getTotalCount(gameId)).isEqualTo(expected);
    }

    private long addAssert(long gameId, ChallengeDef def) throws Exception {
        long l = ds.addChallenge(gameId, def);
        Assert.assertTrue(l > 0);
        return l;
    }

    private ChallengeDef readAssert(long id) throws Exception {
        ChallengeDef def = ds.readChallenge(id);
        Assert.assertNotNull(def);
        Assert.assertEquals(id, def.getId().longValue());
        return def;
    }

    private ChallengeDef readAssert(long id, ChallengeDef check) throws Exception {
        ChallengeDef addedDef = ds.readChallenge(id);
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(id, addedDef.getId().longValue());
        Assert.assertEquals(check.getName(), addedDef.getName());
        Assert.assertEquals(check.getDisplayName(), addedDef.getDisplayName());
        Assert.assertEquals(check.getDescription(), addedDef.getDescription());

        Assert.assertEquals(check.getExpireAfter(), addedDef.getExpireAfter());
        Assert.assertEquals(check.getPoints(), addedDef.getPoints(), 0.1);
        Assert.assertEquals(check.getForTeam(), addedDef.getForTeam());
        Assert.assertEquals(check.getForTeamScope(), addedDef.getForTeamScope());
        Assert.assertEquals(check.getForUser(), addedDef.getForUser());
        Assert.assertEquals(check.getWinnerCount(), addedDef.getWinnerCount());
        Assert.assertEquals(check.getForTeamId(), addedDef.getForTeamId());
        Assert.assertEquals(check.getForUserId(), addedDef.getForUserId());
        Assert.assertEquals(check.getForTeamScopeId(), addedDef.getForTeamScopeId());
        Assert.assertEquals(check.getStartAt(), addedDef.getStartAt());

        if (check.getForEvents() == null) {
            Assert.assertNull(addedDef.getForEvents());
        } else {
            Assertions.assertThat(addedDef.getForEvents())
                    .isNotNull()
                    .hasSize(check.getForEvents().size());
        }

        if (check.getConditions() == null) {
            Assert.assertNull(addedDef.getConditions());
        } else {
            Assertions.assertThat(addedDef.getConditions())
                    .isNotNull()
                    .hasSize(check.getConditions().size());
        }
        return addedDef;
    }

    private ChallengeDef clone(ChallengeDef other) {
        ChallengeDef def = new ChallengeDef();
        def.setName(other.getName());
        def.setDisplayName(other.getDisplayName());
        def.setDescription(other.getDescription());
        return def;
    }

    private ChallengeDef create(String name, String displayName) {
        ChallengeDef def = new ChallengeDef();
        def.setName(name);
        def.setDisplayName(displayName);
        return def;
    }


}
