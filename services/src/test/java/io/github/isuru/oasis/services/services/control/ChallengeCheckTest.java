package io.github.isuru.oasis.services.services.control;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.services.AbstractServiceTest;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.services.WithDataTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class ChallengeCheckTest extends WithDataTest {

    private long gameId;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IProfileService profileService;

    @Before
    public void before() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stack Overflow");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        loadUserData();
    }

    @Test
    public void testChallengeCheckAnySingleWinner() throws Exception {
        UserProfile ned = users.get("ned-stark");
        Assert.assertNotNull(ned);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(31);

        ChallengeDef def = new ChallengeDef();
        def.setGameId(gameId);
        def.setPoints(1000.0);
        def.setWinnerCount(1);
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setConditions(Collections.singletonList("val > 50"));
        def.setStartAt(now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setExpireAfter(end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setName("first-come");
        def.setDisplayName("First to arrive with > 50");

        ChallengeCheck check = new ChallengeCheck(def);

        // different type of event
        assertNotMatches(check.check(createEvent("so-event-b",
                100,
                "walder-frey",
                System.currentTimeMillis())));

        // not exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                49,
                "lysa-arryn",
                System.currentTimeMillis())));
        assertNotMatches(check.check(createEvent("so-event-a",
                50,
                "ramsay-bolton",
                System.currentTimeMillis())));

        // satisfying event before start of challenge
        assertNotMatches(check.check(createEvent("so-event-a",
                80,
                "edmure-tully",
                now.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())));

        // after expiration should halt
        assertNotMatches(check.check(createEvent("so-event-a",
                70,
                "yara-greyjoy",
                end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())), false);

        // a winner
        assertMatches(check.check(createEvent("so-event-a",
                51,
                "benjen-stark",
                System.currentTimeMillis())), 1, false);

        // another winner should ignore
        assertNotMatches(check.check(createEvent("so-event-a",
                70,
                "euron-greyjoy",
                System.currentTimeMillis())), false);

        // after expiration should halt
        assertNotMatches(check.check(createEvent("so-event-a",
                70,
                "euron-greyjoy",
                end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())), false);
    }

    @Test
    public void testChallengeCheckTargettedWinner() throws Exception {
        UserProfile ned = users.get("ned-stark");
        Assert.assertNotNull(ned);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(31);

        ChallengeDef def = new ChallengeDef();
        def.setGameId(gameId);
        def.setPoints(500.0);
        def.setWinnerCount(1);
        def.setForUserId(ned.getId());
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setConditions(Collections.singletonList("val > 50"));
        def.setStartAt(now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setExpireAfter(end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setName("first-come");
        def.setDisplayName("First to arrive with > 50");

        ChallengeCheck check = new ChallengeCheck(def);

        // different type of event
        assertNotMatches(check.check(createEvent("so-event-b",
                100,
                "walder-frey",
                System.currentTimeMillis())));

        // not exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                49,
                "lysa-arryn",
                System.currentTimeMillis())));
        assertNotMatches(check.check(createEvent("so-event-a",
                50,
                "ramsay-bolton",
                System.currentTimeMillis())));

        // same user but does not exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                20,
                "ned-stark",
                System.currentTimeMillis())));

        // different user exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                60,
                "brandon-stark",
                System.currentTimeMillis())));

        // after expiration should halt
        assertNotMatches(check.check(createEvent("so-event-a",
                70,
                "ned-stark",
                end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())), false);

        // a winner
        assertMatches(check.check(createEvent("so-event-a",
                56,
                "ned-stark",
                System.currentTimeMillis())), 1, false);

        // another win should ignore
        assertNotMatches(check.check(createEvent("so-event-a",
                70,
                "ned-stark",
                System.currentTimeMillis())), false);
    }

    @Test
    public void testChallengeCheckAnyTeamWinner() throws Exception {
        //UserProfile ned = users.get("ned-stark");
        TeamProfile winterfell = teams.get("winterfell");
        Assert.assertNotNull(winterfell);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(31);

        ChallengeDef def = new ChallengeDef();
        def.setGameId(gameId);
        def.setPoints(500.0);
        def.setWinnerCount(1);
        def.setForTeamId(winterfell.getId().longValue());
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setConditions(Collections.singletonList("val > 50"));
        def.setStartAt(now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setExpireAfter(end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setName("first-come");
        def.setDisplayName("First to arrive with > 50");

        ChallengeCheck check = new ChallengeCheck(def);

        // different type of event
        assertNotMatches(check.check(createEvent("so-event-b",
                100,
                "walder-frey",
                System.currentTimeMillis())));

        // not exceeding threshold from same team
        assertNotMatches(check.check(createEvent("so-event-a",
                49,
                "sansa-stark",
                System.currentTimeMillis())));
        assertNotMatches(check.check(createEvent("so-event-a",
                50,
                "benjen-stark",
                System.currentTimeMillis())));

        // different user exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                60,
                "edmure-tully",
                System.currentTimeMillis())));

        // a winner
        assertMatches(check.check(createEvent("so-event-a",
                56,
                "arya-stark",
                System.currentTimeMillis())), 1, false);

        // no further winners
        assertNotMatches(check.check(createEvent("so-event-a",
                76,
                "ned-stark",
                System.currentTimeMillis())), false);
    }

    @Test
    public void testChallengeCheckAnyTeamMultipleWinner() throws Exception {
        //UserProfile ned = users.get("ned-stark");
        TeamProfile winterfell = teams.get("winterfell");
        Assert.assertNotNull(winterfell);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(31);

        ChallengeDef def = new ChallengeDef();
        def.setGameId(gameId);
        def.setPoints(500.0);
        def.setWinnerCount(3);
        def.setForTeamId(winterfell.getId().longValue());
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setConditions(Collections.singletonList("val > 50"));
        def.setStartAt(now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setExpireAfter(end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setName("first-come");
        def.setDisplayName("First to arrive with > 50");

        ChallengeCheck check = new ChallengeCheck(def);

        // different type of event
        assertNotMatches(check.check(createEvent("so-event-b",
                100,
                "walder-frey",
                System.currentTimeMillis())));

        // not exceeding threshold from same team
        assertNotMatches(check.check(createEvent("so-event-a",
                49,
                "sansa-stark",
                System.currentTimeMillis())));
        assertNotMatches(check.check(createEvent("so-event-a",
                50,
                "benjen-stark",
                System.currentTimeMillis())));

        // different user exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                60,
                "edmure-tully",
                System.currentTimeMillis())));

        // a winner
        assertMatches(check.check(createEvent("so-event-a",
                56,
                "arya-stark",
                System.currentTimeMillis())), 1, true);

        // another winner
        assertMatches(check.check(createEvent("so-event-a",
                96,
                "ned-stark",
                System.currentTimeMillis())), 2, true);

        // another winner
        assertMatches(check.check(createEvent("so-event-a",
                78,
                "arya-stark",
                System.currentTimeMillis())), 3, false);

        // no further winners
        assertNotMatches(check.check(createEvent("so-event-a",
                68,
                "robb-stark",
                System.currentTimeMillis())), false);
    }

    @Test
    public void testChallengeCheckAnyTeamScopeWinner() throws Exception {
        //UserProfile ned = users.get("ned-stark");
        TeamScope north = scopes.get("the-north");
        Assert.assertNotNull(north);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(31);

        ChallengeDef def = new ChallengeDef();
        def.setGameId(gameId);
        def.setPoints(500.0);
        def.setWinnerCount(1);
        def.setForTeamScopeId(north.getId().longValue());
        def.setForEvents(Collections.singletonList("so-event-a"));
        def.setConditions(Collections.singletonList("val > 50"));
        def.setStartAt(now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setExpireAfter(end.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        def.setName("first-come");
        def.setDisplayName("First to arrive with > 50");

        ChallengeCheck check = new ChallengeCheck(def);

        // different type of event
        assertNotMatches(check.check(createEvent("so-event-b",
                100,
                "walder-frey",
                System.currentTimeMillis())));

        // not exceeding threshold from same team
        assertNotMatches(check.check(createEvent("so-event-a",
                49,
                "sansa-stark",
                System.currentTimeMillis())));
        assertNotMatches(check.check(createEvent("so-event-a",
                50,
                "benjen-stark",
                System.currentTimeMillis())));

        // different user exceeding threshold
        assertNotMatches(check.check(createEvent("so-event-a",
                60,
                "edmure-tully",
                System.currentTimeMillis())));

        // a winner
        assertMatches(check.check(createEvent("so-event-a",
                56,
                "arya-stark",
                System.currentTimeMillis())), 1, false);

        // no further winners
        assertNotMatches(check.check(createEvent("so-event-a",
                76,
                "ned-stark",
                System.currentTimeMillis())), false);
    }


    private void assertNotMatches(ChallengeCheck.ChallengeFilterResult result) {
        assertNotMatches(result, true);
    }

    private void assertNotMatches(ChallengeCheck.ChallengeFilterResult result, boolean cont) {
        Assert.assertFalse(result.isSatisfied());
        Assert.assertEquals(cont, result.isContinue());
        Assert.assertEquals(0, result.getWinNumber());
    }

    private void assertMatches(ChallengeCheck.ChallengeFilterResult result, int pos, boolean cont) {
        Assert.assertTrue(result.isSatisfied());
        Assert.assertEquals(cont, result.isContinue());
        Assert.assertEquals(pos, result.getWinNumber());
    }

    JsonEvent createEvent(String type, int value, String userName, long ts) throws Exception {
        UserProfile profile = users.get(userName);
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(profile.getId());

        JsonEvent event = new JsonEvent();
        event.put(Constants.FIELD_GAME_ID, gameId);
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

}
