package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.services.control.ChallengeCheck;
import io.github.isuru.oasis.services.utils.Pojos;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class JobServiceTest extends AbstractServiceTest {

    private static final long ONE_DAY = 3600L * 24 * 1000;

    @Autowired
    private IJobService jobService;

    @Autowired
    private IGameDefService gameDefService;

    private GameDef gameDef;
    private ChallengeDef cUser;
    private ChallengeDef cTeam;
    private ChallengeDef cTeamScope;

    @Before
    public void beforeEach() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("Stackoverflow");
        gameDef.setDisplayName("Stackoverflow Game");
        long gameId = gameDefService.createGame(gameDef, new GameOptionsDto());
        gameDef = gameDefService.readGame(gameId);

        long ts = System.currentTimeMillis();
        {
            ChallengeDef c1 = new ChallengeDef();
            c1.setGameId(gameId);
            c1.setName("first-ask-user-123");
            c1.setDisplayName("First question asked by User-123");
            c1.setForUserId(123L);
            c1.setForEvents(Collections.singleton("so.question.ask"));
            c1.setConditions(Collections.singletonList("user == 123"));
            c1.setExpireAfter(ts + (ONE_DAY * 3));
            long cid1 = gameDefService.addChallenge(gameId, c1);
            cUser = gameDefService.readChallenge(cid1);
        }

        {
            ChallengeDef c1 = new ChallengeDef();
            c1.setGameId(gameId);
            c1.setName("first-ask-team-456");
            c1.setDisplayName("First question asked by a user in Team-456");
            c1.setForTeamId(456L);
            c1.setForEvents(Collections.singleton("so.question.ask"));
            c1.setExpireAfter(ts + (ONE_DAY * 5));
            long cid1 = gameDefService.addChallenge(gameId, c1);
            cTeam = gameDefService.readChallenge(cid1);
        }

        {
            ChallengeDef c1 = new ChallengeDef();
            c1.setGameId(gameId);
            c1.setName("first-ask-teamscope-789");
            c1.setDisplayName("First question asked by a user in TeamScope-789");
            c1.setForTeamScopeId(789L);
            c1.setForEvents(Collections.singleton("so.question.ask"));
            c1.setConditions(Arrays.asList("teamScope == 789", "user == 123"));
            c1.setExpireAfter(ts + (ONE_DAY * 7));
            long cid1 = gameDefService.addChallenge(gameId, c1);
            cTeamScope = gameDefService.readChallenge(cid1);
        }
    }

    @Test
    public void testSubmitJob() throws Exception {
        {
            SubmittedJob job = createJob(cUser);
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cUser.getId());
            assertJob(job, addedJob);
        }

        {
            SubmittedJob job = createJob(cTeam);
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cTeam.getId());
            assertJob(job, addedJob);
        }

        {
            SubmittedJob job = createJob(cTeamScope);
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cTeamScope.getId());
            assertJob(job, addedJob);
        }

        {
            long ts = System.currentTimeMillis();

            // there should be 3 running jobs after one day
            Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY))
                    .isNotNull().isNotEmpty()
                    .hasSize(3);

            // there should be 2 running jobs after 4 days
            Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY * 4))
                    .isNotNull().isNotEmpty()
                    .hasSize(2);

            // there should be 1 running jobs after 6 days
            Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY * 6))
                    .isNotNull().isNotEmpty()
                    .hasSize(1);

            // there should be 0 running jobs after 7 days
            Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY * 8))
                    .isNotNull().isEmpty();
        }
    }

    @Test
    public void testStopJob() throws Exception {
        {
            SubmittedJob job = createJob(cUser);
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cUser.getId());
            assertJob(job, addedJob);
        }
        {
            SubmittedJob job = createJob(cTeam);
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cTeam.getId());
            assertJob(job, addedJob);
        }

        {
            // disable user job now
            Assert.assertTrue(jobService.stopJobByDef(cUser.getId()));

            // listing should not return disabled job
            {
                long ts = System.currentTimeMillis();

                // there should be 1 running job after one day
                Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY))
                        .isNotNull().isNotEmpty()
                        .hasSize(1);
            }

            // reading should not return too
            Assert.assertNull(jobService.readJob(cUser.getId()));
        }

        {
            // disable team job now using jobId
            SubmittedJob job = jobService.readJob(cTeam.getId());
            Assert.assertTrue(jobService.stopJob(job.getJobId()));

            // listing should not return disabled job
            {
                long ts = System.currentTimeMillis();

                // there should be 0 running jobs after one day
                Assertions.assertThat(jobService.listHadRunningJobs(ts + ONE_DAY))
                        .isNotNull()
                        .isEmpty();
            }

            // reading should not return too
            Assert.assertNull(jobService.readJob(cTeam.getId()));
        }


    }

    @Test
    public void testStateSerialization() throws Exception {
        {
            SubmittedJob job = createJob(cUser);
            ChallengeCheck check = createCheck(cUser, 2);
            job.setStateData(Pojos.serialize(check));
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cUser.getId());
            assertJob(job, addedJob);

            // check returned check class has same props
            ChallengeCheck addedCheck = Pojos.deserialize(addedJob.getStateData());
            Assertions.assertThat(addedCheck).hasFieldOrPropertyWithValue("winners", 2);
            Assert.assertEquals(check.getConditions().size(), addedCheck.getConditions().size());
            Assert.assertEquals(check.getEventNames().size(), addedCheck.getEventNames().size());
            assertChallenge(cUser, addedCheck.getDef());
        }

        {
            SubmittedJob job = createJob(cTeam);
            ChallengeCheck check = createCheck(cTeam, 0);
            job.setStateData(Pojos.serialize(check));
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cTeam.getId());
            assertJob(job, addedJob);

            // check returned check class has same props
            ChallengeCheck addedCheck = Pojos.deserialize(addedJob.getStateData());
            Assertions.assertThat(addedCheck).hasFieldOrPropertyWithValue("winners", 0);
            Assertions.assertThat(addedCheck.getConditions())
                    .isNotNull()
                    .isEmpty();
            Assert.assertEquals(check.getEventNames().size(), addedCheck.getEventNames().size());
            assertChallenge(cTeam, addedCheck.getDef());

            // update winner count
            ChallengeCheck updatedCheck = createCheck(cTeam, 10);
            Assert.assertTrue(jobService.updateJobState(cTeam.getId(), Pojos.serialize(updatedCheck)));

            // now the new check object must have 10 winners
            ChallengeCheck savedCheck = Pojos.deserialize(jobService.readJob(cTeam.getId()).getStateData());
            Assertions.assertThat(savedCheck).hasFieldOrPropertyWithValue("winners", 10);
            Assertions.assertThat(savedCheck.getConditions())
                    .isNotNull()
                    .isEmpty();
            Assert.assertEquals(check.getEventNames().size(), savedCheck.getEventNames().size());
            assertChallenge(cTeam, savedCheck.getDef());
        }

        {
            SubmittedJob job = createJob(cTeamScope);
            ChallengeCheck check = createCheck(cTeamScope, 1234);
            job.setStateData(Pojos.serialize(check));
            Assert.assertTrue(jobService.submitJob(job) > 0);

            SubmittedJob addedJob = jobService.readJob(cTeamScope.getId());
            assertJob(job, addedJob);

            // check returned check class has same props
            ChallengeCheck addedCheck = Pojos.deserialize(addedJob.getStateData());
            Assertions.assertThat(addedCheck).hasFieldOrPropertyWithValue("winners", 1234);
            Assert.assertNotNull(addedCheck.getConditions());
            Assert.assertEquals(check.getConditions().size(), addedCheck.getConditions().size());
            Assert.assertEquals(check.getEventNames().size(), addedCheck.getEventNames().size());
            assertChallenge(cTeamScope, addedCheck.getDef());
        }
    }

    private void assertChallenge(ChallengeDef check, ChallengeDef addedDef) {
        Assert.assertNotNull(addedDef);
        Assert.assertEquals(check.getId().longValue(), addedDef.getId().longValue());
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
    }

    private ChallengeCheck createCheck(ChallengeDef def, int winnerCount) throws NoSuchFieldException, IllegalAccessException {
        ChallengeCheck challengeCheck = new ChallengeCheck(def);
        Field winners = challengeCheck.getClass().getDeclaredField("winners");
        winners.setAccessible(true);
        winners.setInt(challengeCheck, winnerCount);
        return challengeCheck;
    }

    private void assertJob(SubmittedJob expected, SubmittedJob actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getId() > 0);
        Assert.assertEquals(expected.getDefId(), actual.getDefId());
        Assert.assertEquals(expected.getJobId(), actual.getJobId());
        Assert.assertEquals(expected.getSnapshotDir(), actual.getSnapshotDir());
        Assert.assertArrayEquals(expected.getStateData(), actual.getStateData());
        Assert.assertEquals(expected.getToBeFinishedAt(), actual.getToBeFinishedAt());
        Assert.assertEquals(expected.getJarId(), actual.getJarId());
    }

    private SubmittedJob createJob(ChallengeDef def) {
        SubmittedJob job = new SubmittedJob();
        job.setDefId(def.getId());
        job.setJobId(UUID.randomUUID().toString());
        job.setToBeFinishedAt(def.getExpireAfter());
        job.setJarId("jarOf" + def.getName());
        job.setActive(true);
        return job;
    }
}
