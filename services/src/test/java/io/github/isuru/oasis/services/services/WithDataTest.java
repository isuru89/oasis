package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.injector.BufferedRecords;
import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import io.github.isuru.oasis.model.handlers.output.PointModel;
import io.github.isuru.oasis.services.dto.crud.TeamProfileAddDto;
import io.github.isuru.oasis.services.dto.crud.TeamScopeAddDto;
import io.github.isuru.oasis.services.dto.crud.UserProfileAddDto;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserRole;
import io.github.isuru.oasis.services.model.UserTeam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class WithDataTest extends AbstractServiceTest {

    private static final Slugify SLUGIFY = new Slugify();

    private static List<String> ruleOrder = new ArrayList<>();
    private static Map<String, List<String>> badgeRules = new LinkedHashMap<>();

    @Autowired
    private IProfileService ps;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IProfileService profileService;

    protected Map<String, TeamScope> scopes = new HashMap<>();
    protected Map<String, TeamProfile> teams = new HashMap<>();
    protected Map<String, UserProfile> users = new HashMap<>();

    ExecutorService pool;

    private final List<BufferedRecords> buffers = new ArrayList<>();

    List<Long> pointRuleIds;
    List<Long> badgeIds;

    List<Long> addPointRules(long gameId, String... rules) throws Exception {
        ruleOrder.clear();
        List<Long> ids = new ArrayList<>();
        for (String r : rules) {
            PointDef pointDef = new PointDef();
            pointDef.setName(r);
            pointDef.setEvent("so.event." + r);
            pointDef.setDisplayName(r);
            pointDef.setCondition("true");
            pointDef.setAmount(200);
            ids.add(gameDefService.addPointDef(gameId, pointDef));
        }
        ruleOrder.addAll(Arrays.asList(rules));
        pointRuleIds = new ArrayList<>(ids);
        return ids;
    }

    List<Long> addBadgeNames(long gameId, List<String>... badges) throws Exception {
        badgeIds = new ArrayList<>();
        for (List<String> b : badges) {
            Assert.assertTrue(b.size() >= 1);
            String bk = b.get(0);
            List<String> subBadges = new ArrayList<>();
            subBadges.add("");
            if (b.size() == 1) {
                badgeRules.put(bk, subBadges);
            } else {
                for (int i = 1; i < b.size(); i++) {
                    subBadges.add(b.get(i));
                }
                badgeRules.put(bk, subBadges);
            }

            BadgeDef def = new BadgeDef();
            def.setName(bk);
            def.setDisplayName(bk);
            badgeIds.add(gameDefService.addBadgeDef(gameId, def));
        }
        return badgeIds;
    }

    void initPool(int size) {
        pool = Executors.newFixedThreadPool(size);
    }

    void closePool() {
        for (BufferedRecords b : buffers) {
            b.close();
        }

        if (pool != null) {
            pool.shutdown();
        }
        buffers.clear();
    }

    void loadUserData() throws Exception {
        scopes.clear();
        teams.clear();
        users.clear();

        List<TeamScope> teamScopes = new ArrayList<>();
        List<TeamProfile> teamProfiles = new ArrayList<>();
        List<UserProfile> userProfiles = new ArrayList<>();

        Map<Long, Long> expectedScopeCounts = new HashMap<>();
        Map<Long, Long> expectedTeamCounts = new HashMap<>();

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
                        TeamProfile teamProfile = userTeam.get();

                        UserProfileAddDto dto = addUser(parts[1].trim(),
                                Boolean.parseBoolean(parts[2].trim()),
                                parts[0].trim());
                        try {
                            long u = ps.addUserProfile(dto, teamProfile.getId(), UserRole.PLAYER);
                            userProfiles.add(ps.readUserProfile(u));

                            {
                                Long count = expectedTeamCounts.computeIfAbsent(teamProfile.getId().longValue(),
                                        aLong -> 1L);   // 1 with default user
                                expectedTeamCounts.put(teamProfile.getId().longValue(), count + 1);
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("No team is found by name!" + parts[0].trim());
                    }
                });

        {
            teamProfiles.forEach(t -> {
                Long count = expectedScopeCounts.computeIfAbsent(t.getTeamScope().longValue(), aLong -> 0L);
                expectedScopeCounts.put(t.getTeamScope().longValue(),
                        count + expectedTeamCounts.get(t.getId().longValue()));

            });
        }

        teamScopes.forEach(ts -> scopes.put(ts.getName(), ts));
        teamProfiles.forEach(t -> teams.put(t.getName(), t));
        userProfiles.forEach(u -> users.put(u.getName(), u));

        Assert.assertTrue(scopes.size() > 0);
        Assert.assertTrue(teams.size() > 0);
        Assert.assertTrue(users.size() > 0);

        {
            Map<Long, Long> countMap = new HashMap<>();
            profileService.listUserCountInTeams(System.currentTimeMillis())
                    .forEach(r -> countMap.put(r.getId(), r.getTotalUsers()));
            Assertions.assertThat(countMap).containsAllEntriesOf(expectedTeamCounts);
        }

        {
            Map<Long, Long> countMap = new HashMap<>();
            profileService.listUserCountInTeamScopes(System.currentTimeMillis())
                    .forEach(r -> countMap.put(r.getId(), r.getTotalUsers()));
            Assertions.assertThat(countMap).containsAllEntriesOf(expectedScopeCounts);
        }

    }

    int loadPoints(Instant startTime, long timeRange, long gameId) throws Exception {
        Collection<UserProfile> profiles = users.values();

        BufferedRecords buffer = new BufferedRecords(this::flushPoints);
        buffers.add(buffer);
        buffer.init(pool);

        int count = 0;
        for (UserProfile profile : profiles) {
            Random random = new Random(System.currentTimeMillis());
            int eventCount = 20 + random.nextInt(20);
            List<Long> tss = orderedSeq(timeRange, eventCount, startTime.toEpochMilli());

            for (Long ts : tss) {
                UserTeam curTeam = ps.findCurrentTeamOfUser(profile.getId(), true, ts);

                int i = random.nextInt(ruleOrder.size());
                String ruleName = ruleOrder.get(i);
                long rid = pointRuleIds.get(i);


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
                model.setRuleId(rid);

                model.setEvents(Collections.singletonList(toJsonEvent(model, gameId)));

                count++;
                Map<String, Object> data = ConsumerUtils.toPointDaoData(gameId, model);
                buffer.push(new BufferedRecords.ElementRecord(data, System.currentTimeMillis()));
            }
        }

        buffer.flushNow();
        return count;
    }

    int loadBadges(Instant startTime, long timeRange, long gameId) throws Exception {
        Collection<UserProfile> profiles = users.values();

        Assert.assertTrue(badgeRules.size() > 0);

        BufferedRecords buffer = new BufferedRecords(this::flushBadge);
        buffers.add(buffer);
        buffer.init(pool);

        ArrayList<String> badgeNameList = new ArrayList<>(badgeRules.keySet());

        int count = 0;
        for (UserProfile profile : profiles) {
            Random random = new Random(System.currentTimeMillis());
            int eventCount = 10 + random.nextInt(10);
            List<Long> tss = orderedSeq(timeRange, eventCount, startTime.toEpochMilli());

            for (Long ts : tss) {
                UserTeam curTeam = ps.findCurrentTeamOfUser(profile.getId(), true, ts);

                int i = random.nextInt(badgeNameList.size());
                long bid = badgeIds.get(i);
                String bName = badgeNameList.get(i);
                List<String> subBadges = badgeRules.get(bName);

                String sbName = subBadges.get(random.nextInt(subBadges.size()));
                if (sbName.trim().length() == 0) sbName = null;

                BadgeModel model = new BadgeModel();
                model.setGameId((int) gameId);
                model.setSourceId(1);
                model.setTs(ts);
                model.setEventType("so.event." + StringUtils.substringAfterLast(bName, "."));
                model.setUserId(profile.getId());
                model.setTeamScopeId(curTeam.getScopeId().longValue());
                model.setTeamId(curTeam.getTeamId().longValue());
                model.setBadgeId(bid);
                model.setSubBadgeId(sbName);

                model.setEvents(Collections.singletonList(toJsonEvent(model, gameId)));

                count++;
                Map<String, Object> data = ConsumerUtils.toBadgeDaoData(gameId, model);
                buffer.push(new BufferedRecords.ElementRecord(data, System.currentTimeMillis()));
            }
        }

        buffer.flushNow();
        return count;
    }

    private void flushPoints(List<BufferedRecords.ElementRecord> elementRecords) {
        List<Map<String, Object>> data = elementRecords.stream().map(BufferedRecords.ElementRecord::getData)
                .collect(Collectors.toList());
        try {
            dao.executeBatchInsert("game/batch/addPoint", data);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void flushBadge(List<BufferedRecords.ElementRecord> elementRecords) {
        List<Map<String, Object>> data = elementRecords.stream().map(BufferedRecords.ElementRecord::getData)
                .collect(Collectors.toList());
        try {
            dao.executeBatchInsert("game/batch/addBadge", data);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private JsonEvent toJsonEvent(PointModel model, long gameId) {
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

    private JsonEvent toJsonEvent(BadgeModel model, long gameId) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.setFieldValue(Constants.FIELD_GAME_ID, gameId);
        jsonEvent.setFieldValue(Constants.FIELD_TEAM, model.getTeamId());
        jsonEvent.setFieldValue(Constants.FIELD_SCOPE, model.getTeamScopeId());
        jsonEvent.setFieldValue(Constants.FIELD_USER, model.getUserId());
        jsonEvent.setFieldValue(Constants.FIELD_TIMESTAMP, model.getTs());
        jsonEvent.setFieldValue(Constants.FIELD_ID, randomId());
        jsonEvent.setFieldValue(Constants.FIELD_EVENT_TYPE,
                "so.event." + StringUtils.substringAfterLast(String.valueOf(model.getBadgeId()), "."));
        return jsonEvent;
    }

    private List<Long> orderedSeq(long range, int n, long offset) {
        Random random = new Random(System.currentTimeMillis());
        return random.longs(n, 0, range)
                .map(operand -> offset + operand)
                .boxed()
                .collect(Collectors.toList());
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
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
