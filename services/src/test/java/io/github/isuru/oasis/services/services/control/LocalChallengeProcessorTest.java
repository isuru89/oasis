package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.model.IEventDispatcher;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.services.IJobService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.services.WithDataTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalChallengeProcessorTest extends WithDataTest {

    private static long startTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    private static long endTime = LocalDate.now().plusDays(31).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

    private static long currId = 1;

    private ChallengeDef oneWinnerAny;
    private ChallengeDef oneWinnerUser;
    private ChallengeDef oneWinnerTeam;
    private ChallengeDef oneWinnerScope;
    private ChallengeDef threeWinnerAny;
    private ChallengeDef threeWinnerUser;
    private ChallengeDef threeWinnerTeam;
    private ChallengeDef threeWinnerScope;

    private IJobService jobService;

    private IEventDispatcher eventDispatcher;

    private ExecutorService pool;

    private LocalChallengeProcessor processor;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private DataCache dataCache;

    private String tokenInt;

    @Before
    public void before() throws Exception {
        resetSchema();

        jobService = Mockito.mock(IJobService.class);
        eventDispatcher = Mockito.mock(IEventDispatcher.class);

        loadUserData();

        populateChallenges();

        pool = Executors.newFixedThreadPool(1);
        processor = new LocalChallengeProcessor(jobService, eventDispatcher);
        pool.submit(processor);
        pool.shutdown();

        tokenInt = dataCache.getInternalEventSourceToken().getToken();
        mockChallengeStatusServices();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChallengeAny() throws Exception {
        processor.submitChallenge(oneWinnerAny);

        processor.submitEvent(tokenInt, createEvent("so-event-b", 12, "ned-stark", ts()));
        Mockito.verify(jobService, Mockito.never()).updateJobState(Mockito.any(Long.class), Mockito.any());

        processor.submitEvent(tokenInt, createEvent("so-event-a", 24, "ned-stark", ts()));
        Mockito.verify(jobService, Mockito.never()).updateJobState(Mockito.any(Long.class), Mockito.any());

        processor.submitEvent(tokenInt, createEvent("so-event-a", 53, "ned-stark", ts()));
        processor.submitEvent(tokenInt, createEvent("so-event-a", 63, "ned-stark", ts()));
        processor.submitEvent(tokenInt, createEvent("so-event-a", 163, "edmure-tully", ts()));


        Thread.sleep(3000);
        ArgumentCaptor<Map<String, Object>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(eventDispatcher, Mockito.times(1))
                .dispatch(Mockito.any(Long.class), argumentCaptor.capture());
        Map<String, Object> eventData = argumentCaptor.getValue();
        assertWinnerData(eventData, oneWinnerAny.getPoints(), oneWinnerAny.getId(), 1, "ned-stark");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChallengeAnyMultiple() throws Exception {
        processor.submitChallenge(threeWinnerAny);

        processor.submitEvent(tokenInt, createEvent("so-event-b", 12, "ned-stark", ts()));
        Mockito.verify(jobService, Mockito.never()).updateJobState(Mockito.any(Long.class), Mockito.any());

        processor.submitEvent(tokenInt, createEvent("so-event-a", 24, "ned-stark", ts()));
        Mockito.verify(jobService, Mockito.never()).updateJobState(Mockito.any(Long.class), Mockito.any());

        processor.submitEvent(tokenInt, createEvent("so-event-a", 53, "ned-stark", ts()));
        processor.submitEvent(tokenInt, createEvent("so-event-a", 63, "ned-stark", ts()));
        processor.submitEvent(tokenInt, createEvent("so-event-a", 163, "edmure-tully", ts()));
        processor.submitEvent(tokenInt, createEvent("so-event-a", 200, "jaime-lannister", ts()));


        Thread.sleep(3000);
        ArgumentCaptor<Map<String, Object>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(eventDispatcher, Mockito.times(3))
                .dispatch(Mockito.any(Long.class), argumentCaptor.capture());
        List<Map<String, Object>> eventData = argumentCaptor.getAllValues();
        Assert.assertEquals(3, eventData.size());
        assertWinnerData(eventData.get(0), threeWinnerAny.getPoints(), threeWinnerAny.getId(), 1, "ned-stark");
        assertWinnerData(eventData.get(1), threeWinnerAny.getPoints(), threeWinnerAny.getId(), 2, "ned-stark");
        assertWinnerData(eventData.get(2), threeWinnerAny.getPoints(), threeWinnerAny.getId(), 3, "edmure-tully");
    }

    @After
    public void after() throws Exception {
        Thread.sleep(1000);
        processor.setStop();
        Thread.sleep(1000);
        Assert.assertTrue(processor.isStopped());
        processor.stopChallengesOfGame(1);
    }

    private long ts() {
        return System.currentTimeMillis();
    }

    private void assertWinnerData(Map<String, Object> eventData, double points, long defId, int winNo, String userName) {
        Assert.assertEquals(points, (Double) eventData.get(ChallengeEvent.KEY_POINTS), 0.1);
        Assert.assertEquals(defId, eventData.get(ChallengeEvent.KEY_DEF_ID));
        Assert.assertEquals(winNo, eventData.get(ChallengeEvent.KEY_WIN_NO));
        UserProfile profile = users.get(userName);
        Assert.assertEquals(profile.getId(), eventData.get(Constants.FIELD_USER));
    }

    JsonEvent createEvent(String type, int value, String userName, long ts) throws Exception {
        UserProfile profile = users.get(userName);
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(profile.getId());

        JsonEvent event = new JsonEvent();
        event.put(Constants.FIELD_GAME_ID, 1);
        event.put(Constants.FIELD_SOURCE, 1);
        event.put(Constants.FIELD_ID, UUID.randomUUID().toString());
        event.put(Constants.FIELD_USER, profile.getId());
        event.put(Constants.FIELD_SCOPE, currentTeamOfUser.getScopeId());
        event.put(Constants.FIELD_TEAM, currentTeamOfUser.getTeamId());
        event.put(Constants.FIELD_EVENT_TYPE, type);
        event.put(Constants.FIELD_TIMESTAMP, ts);
        event.put("val", value);
        return event;
    }

    private void populateChallenges() {
        currId = 100;

        oneWinnerAny = populateDefaults("oneWinnerAny", 1000.0);
        {
            oneWinnerUser = populateDefaults("oneWinnerUser", 750);
            UserProfile ned = users.get("ned-stark");
            Assert.assertNotNull(ned);
            oneWinnerUser.setForUserId(ned.getId());
        }
        {
            oneWinnerTeam = populateDefaults("oneWinnerTeam", 500);
            TeamProfile winterfell = teams.get("winterfell");
            Assert.assertNotNull(winterfell);
            oneWinnerTeam.setForTeamId(winterfell.getId().longValue());
        }
        {
            oneWinnerScope = populateDefaults("oneWinnerScope", 300);
            TeamScope north = scopes.get("the-north");
            Assert.assertNotNull(north);
            oneWinnerScope.setForTeamId(north.getId().longValue());
        }

        threeWinnerAny = populateDefaults("threeWinnerAny", 1000.0);
        threeWinnerAny.setWinnerCount(3);
        {
            threeWinnerUser = populateDefaults("threeWinnerUser", 750);
            threeWinnerUser.setWinnerCount(3);
            UserProfile ned = users.get("ned-stark");
            Assert.assertNotNull(ned);
            threeWinnerUser.setForUserId(ned.getId());
        }
        {
            threeWinnerTeam = populateDefaults("threeWinnerTeam", 500);
            threeWinnerTeam.setWinnerCount(3);
            TeamProfile winterfell = teams.get("winterfell");
            Assert.assertNotNull(winterfell);
            threeWinnerTeam.setForTeamId(winterfell.getId().longValue());
        }
        {
            threeWinnerScope = populateDefaults("threeWinnerScope", 300);
            threeWinnerScope.setWinnerCount(3);
            TeamScope north = scopes.get("the-north");
            Assert.assertNotNull(north);
            threeWinnerScope.setForTeamId(north.getId().longValue());
        }
    }

    private void mockChallengeStatusServices() throws Exception {
        Map<Long, SubmittedJob> jobMap = new HashMap<>();
        Mockito.when(jobService.submitJob(Mockito.any(SubmittedJob.class))).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                SubmittedJob job = invocation.getArgument(0);
                jobMap.put(job.getDefId(), job);
                return job.getDefId();
            }
        });
        Mockito.when(jobService.readJob(Mockito.any(Long.class))).thenAnswer(new Answer<SubmittedJob>() {
            @Override
            public SubmittedJob answer(InvocationOnMock invocation) throws Throwable {
                long id = invocation.getArgument(0);
                return jobMap.get(id);
            }
        });
        Mockito.when(jobService.updateJobState(Mockito.any(Long.class), Mockito.any())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                long id = invocation.getArgument(0);
                SubmittedJob job = jobMap.get(id);
                if (job != null) {
                    job.setStateData(invocation.getArgument(1));
                }
                return true;
            }
        });
        Mockito.when(jobService.stopJobByDef(Mockito.any(Long.class))).thenReturn(true);
    }

    private ChallengeDef populateDefaults(String name, double points) {
        ChallengeDef def = new ChallengeDef();
        def.setId(currId++);
        def.setStartAt(startTime);
        def.setExpireAfter(endTime);
        def.setWinnerCount(1);
        def.setName(name);
        def.setDisplayName(name);
        def.setPoints(points);
        def.setConditions(Collections.singletonList("val > 50"));
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setGameId(1L);
        return def;
    }
}
