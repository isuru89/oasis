package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.injector.BufferedRecords;
import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.output.PointModel;
import io.github.isuru.oasis.services.dto.crud.TeamProfileAddDto;
import io.github.isuru.oasis.services.dto.crud.TeamScopeAddDto;
import io.github.isuru.oasis.services.dto.crud.UserProfileAddDto;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.isuru.oasis.services.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LeaderboardTest extends AbstractServiceTest {

    private static final Slugify SLUGIFY = new Slugify();

    private static final long TIME_RANGE = 3600L * 24 * 7 * 1000;

    private static Map<String, Double> eventProbabilities = new LinkedHashMap<>();
    private static List<String> eventOrder = new ArrayList<>();
    private static List<String> ruleOrder = new ArrayList<>();

    private Map<String, TeamScope> scopes = new HashMap<>();
    private Map<String, TeamProfile> teams = new HashMap<>();
    private Map<String, UserProfile> users = new HashMap<>();

    private ExecutorService pool;

    @Autowired
    private IProfileService ps;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IGameDefService gameDefService;

    private long gameId;
    private LeaderboardDef lb1;

    @Before
    public void beforeEach() throws Exception {
        resetSchema();

        ruleOrder.add("so.rule.a");
        ruleOrder.add("so.rule.b");
        ruleOrder.add("so.rule.c");
        ruleOrder.add("so.rule.d");
        ruleOrder.add("so.rule.e");

        scopes.clear();
        teams.clear();
        users.clear();

        // populate dummy data
        loadData();

        Assert.assertTrue(scopes.size() > 0);
        Assert.assertTrue(teams.size() > 0);
        Assert.assertTrue(users.size() > 0);

        if (pool != null) {
            pool.shutdown();
        }
        pool = Executors.newFixedThreadPool(5);

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow Game");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        lb1 = new LeaderboardDef();
        lb1.setOrderBy("desc");
        lb1.setRuleIds(Arrays.asList("so.rule.a", "so.rule.b"));
        lb1.setName("leaderboard-ab");
        lb1.setDisplayName("A & B Leaderboard");
        gameDefService.addLeaderboardDef(gameId, lb1);
    }

    @Test
    public void run() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 24, 12, 30).atZone(ZoneOffset.UTC)
                .toInstant();
        Collection<UserProfile> profiles = users.values();

        BufferedRecords buffer = new BufferedRecords(this::flush);

        int count = 0;
        for (UserProfile profile : profiles) {
            Random random = new Random(System.currentTimeMillis());
            int eventCount = 20 + random.nextInt(20);
            List<Long> tss = orderedSeq(TIME_RANGE, eventCount, startTime.toEpochMilli());

            for (Long ts : tss) {
                UserTeam curTeam = ps.findCurrentTeamOfUser(profile.getId(), true, ts);

                int i = random.nextInt(ruleOrder.size());
                String ruleName = ruleOrder.get(i);

                PointModel model = new PointModel();
                model.setGameId((int) gameId);
                model.setSourceId(1);
                model.setTs(ts);
                model.setCurrency(true);
                model.setRuleName(ruleName);
                model.setEventType("so.event." + StringUtils.substringAfterLast(ruleName, "."));
                model.setUserId(profile.getId());
                model.setTeamScopeId(curTeam.getScopeId().longValue());
                model.setTeamId(curTeam.getTeamId().longValue());
                model.setAmount(Math.round(random.nextDouble() * 500 * 100) / 100.0);
                model.setRuleId((long) i);

                model.setEvents(Collections.singletonList(toJsonEvent(model)));

                count++;
                Map<String, Object> data = ConsumerUtils.toPointDaoData(gameId, model);
                buffer.push(new BufferedRecords.ElementRecord(data, System.currentTimeMillis()));
            }
        }

        buffer.flushNow();

        List<Map<String, Object>> points = ServiceUtils.toList(
                dao.executeRawQuery("SELECT * FROM OA_POINT", null));
        int size = points.size();
        Assert.assertEquals(count, size);

        int globalLbSize;
        {
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            List<GlobalLeaderboardRecordDto> rankings = gameService.readGlobalLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            globalLbSize = rankings.size();
            for (GlobalLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating Winterfell team leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            TeamProfile teamProfile = teams.get("winterfell");
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(teamProfile.getId(), dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating The-Riverlands team leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            TeamScope scope = scopes.get("the-riverlands");
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamScopeLeaderboard(scope.getId(), dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating For Race team leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            Assert.assertEquals(rankings.size(), globalLbSize);
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating For Race team leaderboard (Only top-2)...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            dto.setTopThreshold(2);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                Assert.assertTrue(recordDto.getRankInTeamScope() <= 2);
                System.out.println(recordDto);
            }
        }

        {
            double thold = 1500.0;
            System.out.println("Calculating For Race team leaderboard (Only having "+thold+" points)...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            dto.setMinPointThreshold(thold);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                Assert.assertTrue(recordDto.getTotalPoints() >= thold);
                System.out.println(recordDto);
            }
        }
    }

    private void flush(List<BufferedRecords.ElementRecord> elementRecords) {
        List<Map<String, Object>> data = elementRecords.stream().map(BufferedRecords.ElementRecord::getData)
                .collect(Collectors.toList());
        try {
            dao.executeBatchInsert("game/batch/addPoint", data);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private JsonEvent toJsonEvent(PointModel model) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.setFieldValue(Constants.FIELD_GAME_ID, gameId);
        jsonEvent.setFieldValue(Constants.FIELD_TEAM, model.getTeamId());
        jsonEvent.setFieldValue(Constants.FIELD_SCOPE, model.getTeamScopeId());
        jsonEvent.setFieldValue(Constants.FIELD_USER, model.getUserId());
        jsonEvent.setFieldValue(Constants.FIELD_TIMESTAMP, model.getTs());
        jsonEvent.setFieldValue(Constants.FIELD_ID, randomId());
        jsonEvent.setFieldValue(Constants.FIELD_EVENT_TYPE,
                "so.event." + StringUtils.substringAfterLast(model.getRuleName(), "."));
        return jsonEvent;
    }

    private void loadData() {
        List<TeamScope> teamScopes = new ArrayList<>();
        List<TeamProfile> teamProfiles = new ArrayList<>();
        List<UserProfile> userProfiles = new ArrayList<>();

        readLines("/dataex/scopes.csv")
                .forEach(line -> {
                    String[] parts = line.split("[,]");
                    TeamScopeAddDto dto = addTeamScope(parts[0].trim(), Long.parseLong(parts[1].trim()));
                    try {
                        teamScopes.add(ps.readTeamScope(ps.addTeamScope(dto)));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load team scopes!", e);
                    }
                });

        readLines("/dataex/teams.csv")
                .forEach(line -> {
                    String[] parts = line.split("[,]");
                    TeamProfileAddDto dto = addTeam(parts[0].trim());
                    Optional<TeamScope> scopeOptional = teamScopes.stream()
                            .filter(ts -> ts.getName().equals(parts[1].trim()))
                            .findFirst();
                    if (scopeOptional.isPresent()) {
                        try {
                            dto.setTeamScope(scopeOptional.get().getId());
                            TeamProfile teamProfile = ps.readTeam(ps.addTeam(dto));
                            teamProfiles.add(teamProfile);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("No scope is found by name " + parts[1].trim());
                    }
                });

        readLines("/dataex/users.csv")
                .forEach(line -> {
                    String[] parts = line.split("[,]");
                    Optional<TeamProfile> userTeam = teamProfiles.stream()
                            .filter(t -> t.getName().equals(parts[0].trim()))
                            .findFirst();
                    if (userTeam.isPresent()) {
                        UserProfileAddDto dto = addUser(parts[1].trim(),
                                Boolean.parseBoolean(parts[2].trim()),
                                parts[0].trim());
                        try {
                            long u = ps.addUserProfile(dto, userTeam.get().getId(), UserRole.PLAYER);
                            userProfiles.add(ps.readUserProfile(u));
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("No team is found by name!" + parts[0].trim());
                    }
                });

        teamScopes.forEach(ts -> scopes.put(ts.getName(), ts));
        teamProfiles.forEach(t -> teams.put(t.getName(), t));
        userProfiles.forEach(u -> users.put(u.getName(), u));
    }

    private static List<String> readLines(String resPath) {
        try (InputStream inputStream = LeaderboardTest.class.getResourceAsStream(resPath)) {
            return IOUtils.readLines(inputStream, StandardCharsets.UTF_8).stream()
                    .filter(l -> !l.trim().isEmpty() && !l.trim().startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    private List<Long> orderedSeq(long range, int n, long offset) {
        Random random = new Random(System.currentTimeMillis());
        return random.longs(n, 0, range)
                .map(operand -> offset + operand)
                .boxed()
                .collect(Collectors.toList());
    }

    private List<Integer> orderedSeqInt(int n, List<String> keys, Map<String, Double> probs) {
        Random random = new Random(System.currentTimeMillis());
        int size = keys.size();
        return random.ints(n, 0, size)
                .map(eventType -> {
                    double p = probs.get(keys.get(eventType));
                    return (int) Math.round(p);
                })
                .boxed()
                .collect(Collectors.toList());
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private TeamScopeAddDto addTeamScope(String name, long extId) {
        TeamScopeAddDto s = new TeamScopeAddDto();
        s.setName(SLUGIFY.slugify(name));
        s.setDisplayName(name);
        s.setAutoScope(false);
        s.setExtId(extId);
        return s;
    }

    private TeamProfileAddDto addTeam(String name) {
        TeamProfileAddDto t = new TeamProfileAddDto();
        t.setName(SLUGIFY.slugify(name));
        t.setAutoTeam(false);
        t.setAvatarId(String.format("images/t/%s.jpg", SLUGIFY.slugify(name)));
        return t;
    }

    private UserProfileAddDto addUser(String name, boolean male, String team) {
        UserProfileAddDto u = new UserProfileAddDto();
        String dn = name.split("[ ]+")[0];
        u.setName(SLUGIFY.slugify(name));
        u.setNickName(name);
        u.setMale(male);
        u.setEmail(SLUGIFY.slugify(dn) + "@" + team + ".com");
        u.setAvatarId(String.format("images/u/%s.jpg", SLUGIFY.slugify(dn)));
        return u;
    }

}
