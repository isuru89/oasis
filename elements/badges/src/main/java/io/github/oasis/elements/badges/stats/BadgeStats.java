/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.badges.stats;

import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.collect.Record;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.services.AbstractStatsApiService;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.annotations.OasisQueryService;
import io.github.oasis.core.services.annotations.OasisStatEndPoint;
import io.github.oasis.core.services.annotations.QueryPayload;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.BadgeParser;
import io.github.oasis.elements.badges.spec.Streak;
import io.github.oasis.elements.badges.stats.to.GameRuleWiseBadgeLog;
import io.github.oasis.elements.badges.stats.to.GameRuleWiseBadgeLog.RuleBadgeLogRecord;
import io.github.oasis.elements.badges.stats.to.GameRuleWiseBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog.BadgeLogRecord;
import io.github.oasis.elements.badges.stats.to.UserBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeSummary;
import io.github.oasis.elements.badges.stats.to.UserBadgesProgressRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgesProgressResponse;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.oasis.core.utils.Constants.COLON;

/**
 * @author Isuru Weerarathna
 */
@OasisQueryService
public class BadgeStats extends AbstractStatsApiService {

    private final Map<Integer, Map<Integer, AttributeInfo>> gameAttributes = new ConcurrentHashMap<>();
    private final Map<Integer, Map<String, BadgeDef>> gameWiseRuleCache = new ConcurrentHashMap<>();

    private final BadgeParser parser = new BadgeParser();
    private final Set<String> STREAK_KINDS = Set.of(BadgeDef.STREAK_N_KIND, BadgeDef.TIME_BOUNDED_STREAK_KIND);
    private final Set<String> THRESHOLD_KINDS = Set.of(BadgeDef.PERIODIC_ACCUMULATIONS_KIND, BadgeDef.PERIODIC_OCCURRENCES_KIND);
    private final Set<String> PERIODIC_THRESHOLD_KINDS = Set.of(BadgeDef.PERIODIC_ACCUMULATIONS_STREAK_KIND, BadgeDef.PERIODIC_OCCURRENCES_STREAK_KIND);

    public BadgeStats(Db dbPool, OasisMetadataSupport contextSupport, @AdminDbRepository OasisRepository adminRepo) {
        super(dbPool, contextSupport, adminRepo);
    }

    @OasisStatEndPoint(path = "/elements/badges/summary")
    public UserBadgeSummary getBadgeSummary(@QueryPayload UserBadgeRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String badgeKey = BadgeIDs.getGameUserBadgesSummary(request.getGameId(), request.getUserId());

            Mapped badgeSummary = db.MAP(badgeKey);

            List<String> subKeys = new ArrayList<>();
            subKeys.add("all");
            Map<String, SimpleElementDefinition> elementDefinitions = new HashMap<>();
            if (Utils.isNotEmpty(request.getRuleFilters())) {
                subKeys.addAll(request.getRuleFilters().stream().map(rule -> "rule:" + rule)
                        .collect(Collectors.toList()));

                if (Utils.isNotEmpty(request.getAttributeFilters())) {
                    subKeys.addAll(request.getRuleFilters().stream()
                            .flatMap(rule -> request.getAttributeFilters().stream().map(attr -> "rule:" + rule + COLON + attr))
                            .collect(Collectors.toList()));
                }

                elementDefinitions = getContextHelper().readElementDefinitions(request.getGameId(), request.getRuleFilters());

            } else if (Utils.isNotEmpty(request.getAttributeFilters())) {
                subKeys.addAll(request.getAttributeFilters().stream().map(attr -> "attr:" + attr)
                        .collect(Collectors.toList()));
            }

            UserBadgeSummary summary = new UserBadgeSummary();
            summary.setGameId(request.getGameId());
            summary.setUserId(request.getUserId());

            List<String> countValues = badgeSummary.getValues(subKeys.toArray(new String[0]));
            summary.setTotalBadges(Numbers.asInt(countValues.get(0)));
            Map<Integer, AttributeInfo> gameAttributes = loadGameAttributes(request.getGameId());

            for (int i = 1; i < subKeys.size(); i++) {
                String key = subKeys.get(i);
                String[] parts = key.split(COLON);

                if ("attr".equals(parts[0])) {
                    int attrId = Numbers.asInt(parts[1]);
                    summary.addSummaryStat(parts[1], new UserBadgeSummary.AttributeSummaryStat(
                            attrId, gameAttributes.get(attrId), Numbers.asInt(countValues.get(i)), null));
                } else {
                    UserBadgeSummary.RuleSummaryStat ruleSummaryStat = null;
                    if (parts.length == 3) {
                        int attr = Numbers.asInt(parts[2]);
                        ruleSummaryStat = summary.addRuleStat(parts[1], attr, new UserBadgeSummary.AttributeSummaryStat(
                                attr, gameAttributes.get(attr), Numbers.asInt(countValues.get(i)), null));

                    } else if (parts.length == 2) {
                        ruleSummaryStat = summary.addRuleSummary(parts[1], Numbers.asInt(countValues.get(i)), null);
                    }

                    if (ruleSummaryStat != null) {
                        ruleSummaryStat.setBadgeMetadata(elementDefinitions.get(parts[1]));
                    }
                }
            }

            return summary;
        }
    }

    @OasisStatEndPoint(path = "/elements/badges/log")
    public UserBadgeLog getBadgeLog(@QueryPayload UserBadgeLogRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String badgeLogKey = BadgeIDs.getGameUserBadgesLog(request.getGameId(), request.getUserId());

            Sorted badgeLog = db.SORTED(badgeLogKey);
            List<Record> rangeRecords = badgeLog.getRangeByScoreWithScores(request.getTimeFrom(), request.getTimeTo());
            List<BadgeLogRecord> logRecords = new ArrayList<>();

            Set<String> badgeIds = new HashSet<>();
            for (Record record : rangeRecords) {
                long awardedTime = record.getScoreAsLong();
                String[] parts = record.getMember().split(COLON);

                if (parts.length < 2) {
                    continue;
                }

                badgeIds.add(parts[0]);
                if (parts[2].contains("-")) {
                    logRecords.add(new BadgeLogRecord(parts[0], Numbers.asInt(parts[1]), parts[2], awardedTime));
                } else {
                    logRecords.add(new BadgeLogRecord(parts[0], Numbers.asInt(parts[1]), Numbers.asLong(parts[2]), awardedTime));
                }
            }

            if (Utils.isNotEmpty(badgeIds)) {
                Map<String, SimpleElementDefinition> defMap = getContextHelper().readElementDefinitions(request.getGameId(), badgeIds);
                Map<Integer, AttributeInfo> attributeInfoMap = loadGameAttributes(request.getGameId());
                for (BadgeLogRecord record : logRecords) {
                    record.setAttributeMetadata(attributeInfoMap.get(record.getAttribute()));
                    record.setBadgeMetadata(defMap.get(record.getBadgeId()));
                }
            }

            UserBadgeLog userBadgeLog = new UserBadgeLog();
            userBadgeLog.setGameId(request.getGameId());
            userBadgeLog.setUserId(request.getUserId());
            userBadgeLog.setLog(logRecords);

            return userBadgeLog;
        }
    }

    @OasisStatEndPoint(path = "/elements/badges/rules/log")
    public GameRuleWiseBadgeLog getRuleWiseBadgeLog(@QueryPayload GameRuleWiseBadgeLogRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String badgeLogKey = BadgeIDs.getGameRuleWiseBadgeLogKey(request.getGameId(), request.getBadgeId());

            Sorted badgeLog = db.SORTED(badgeLogKey);
            List<Record> rangeRecords;
            if (request.isTimeBased()) {
                rangeRecords = badgeLog.getRangeByScoreWithScores(request.getTimeFrom(), request.getTimeTo());
            } else {
                rangeRecords = badgeLog.getRangeByRankWithScores(request.getOffset(), request.getOffset() + request.getSize() - 1);
            }
            List<RuleBadgeLogRecord> logRecords = new ArrayList<>();

            Set<String> userIds = new HashSet<>();
            for (Record record : rangeRecords) {
                long awardedTime = record.getScoreAsLong();
                String[] parts = record.getMember().split(COLON);

                if (parts.length < 3) {
                    continue;
                }

                userIds.add(parts[0]);
                logRecords.add(new RuleBadgeLogRecord(Numbers.asLong(parts[0]),
                        Numbers.asInt(parts[1]),
                        Numbers.asLong(parts[2]),
                        awardedTime));
            }

            if (Utils.isNotEmpty(userIds)) {
                Map<String, UserMetadata> defMap = getContextHelper().readUsersByIdStrings(userIds);
                Map<Integer, AttributeInfo> attributeInfoMap = loadGameAttributes(request.getGameId());
                for (RuleBadgeLogRecord record : logRecords) {
                    record.setAttributeMetadata(attributeInfoMap.get(record.getAttribute()));
                    record.setUserMetadata(defMap.get(String.valueOf(record.getUserId())));
                }
            }

            GameRuleWiseBadgeLog userBadgeLog = new GameRuleWiseBadgeLog();
            userBadgeLog.setGameId(request.getGameId());
            userBadgeLog.setBadgeMetadata(getContextHelper().readElementDefinition(request.getGameId(), request.getBadgeId()));
            userBadgeLog.setLog(logRecords);

            return userBadgeLog;
        }
    }

    @OasisStatEndPoint(path = "/elements/badges/progress")
    public UserBadgesProgressResponse getUserProgress(@QueryPayload UserBadgesProgressRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            UserBadgesProgressResponse response = new UserBadgesProgressResponse();
            response.setGameId(request.getGameId());
            response.setUserId(request.getUserId());

            UserMetadata userMetadata = getContextHelper().readUserMetadata(request.getUserId());
            long timeOffset = StringUtils.isEmpty(userMetadata.getTz())
                    ? 0
                    : ZoneId.of(userMetadata.getTz()).getRules().getOffset(Instant.now()).getTotalSeconds() * 1000L;

            Map<String, BadgeDef> rulesMap = gameWiseRuleCache.computeIfAbsent(request.getGameId(), (id) -> new ConcurrentHashMap<>());

            List<String> ruleIds = request.getRuleIds();

            for (String ruleId : ruleIds) {
                BadgeDef def = rulesMap.computeIfAbsent(ruleId, (rId) -> {
                    try {
                        ElementDef elementDef = getContextHelper().readFullElementDef(request.getGameId(), rId);
                        EngineMessage message = EngineMessage.fromElementDef(request.getGameId(), elementDef);
                        return parser.parse(message);

                    } catch (OasisException e) {
                        throw new OasisRuntimeException("Unable to load rule details " + rId + "!", e);
                    }
                });

                if (STREAK_KINDS.contains(def.getSpec().getKind())) {
                    handleStreakStatus(db, ruleId, response);
                } else if (THRESHOLD_KINDS.contains(def.getSpec().getKind())) {
                    handleThresholdStatus(db, ruleId, timeOffset, def, response);
                } else if (PERIODIC_THRESHOLD_KINDS.contains(def.getSpec().getKind())) {
                    handlePeriodicStreaks(db, ruleId, timeOffset, def, response);
                }
            }

            return response;
        }
    }

    private void handleThresholdStatus(DbContext db, String ruleId, long userTimeOffset, BadgeDef badgeDef, UserBadgesProgressResponse response) {
        String thresholdKey = BadgeIDs.getUserTemporalBadgeKey(response.getGameId(), response.getUserId(), ruleId);
        Mapped mapped = db.MAP(thresholdKey);

        long timeUnit = BadgeParser.toLongTimeUnit(badgeDef.getSpec().getPeriod());
        long ts = System.currentTimeMillis() + userTimeOffset;
        long tsUnit = ts - (ts % timeUnit);
        String subKey = String.valueOf(tsUnit);

        String currValueStr = mapped.getValue(subKey);
        if (Texts.isEmpty(currValueStr)) {
            response.addThresholdProgress(ruleId, BigDecimal.ZERO);
        } else {
            response.addThresholdProgress(ruleId, new BigDecimal(currValueStr));
        }
    }

    private void handleStreakStatus(DbContext db, String ruleId, UserBadgesProgressResponse response) {
        String streakKey = BadgeIDs.getUserBadgeStreakKey(response.getGameId(), response.getUserId(), ruleId);
        Sorted streakMap = db.SORTED(streakKey);

        List<Record> memberList = streakMap.getRangeByRankWithScores(0, -1);
        int streakCount = 0;
        for (int i = memberList.size() - 1; i >= 0; i--) {
            String memberStatus = memberList.get(i).getMember().split(COLON)[1];
            if ("0".equals(memberStatus)) break;
            streakCount++;
        }
        response.addStreakProgress(ruleId, streakCount);
    }

    private void handlePeriodicStreaks(DbContext db, String ruleId, long userTimeOffset, BadgeDef badgeDef, UserBadgesProgressResponse response) {
        Optional<Integer> maxStreakRef = findMaxStreak(badgeDef);
        if (maxStreakRef.isEmpty()) {
            return;
        }

        String streakKey = BadgeIDs.getBadgeHistogramKey(response.getGameId(), response.getUserId(), ruleId);
        Sorted sorted = db.SORTED(streakKey);

        long timeUnit = BadgeParser.toLongTimeUnit(badgeDef.getSpec().getPeriod());
        long ts = System.currentTimeMillis() + userTimeOffset;
        long score = ts - (ts % timeUnit);

        Optional<String> memberByScore = sorted.getMemberByScore(score);
        if (memberByScore.isPresent()) {
            String member = memberByScore.get();

            long rank = sorted.getRank(member);
            List<Record> rangeByRankWithScores = sorted.getRangeByRankWithScores(rank - maxStreakRef.get(), rank + 1);
            int currStreak = 0;
            long currTs = score;
            for (int i = rangeByRankWithScores.size() - 1; i >= 0; i--) {
                Record record = rangeByRankWithScores.get(i);
                if (record.getScore() < currTs - timeUnit) {
                    break;
                }
                currTs -= timeUnit;
                BigDecimal recordValue = new BigDecimal(record.getMember().split(COLON)[1]);
                if (recordValue.compareTo(badgeDef.getSpec().getThreshold()) < 0) {
                    break;
                }
                currStreak++;
            }

            response.addThresholdStreakProgress(ruleId, new BigDecimal(member.split(COLON)[1]), currStreak);
        }
    }

    private Optional<Integer> findMaxStreak(BadgeDef def) {
        return def.getSpec().getStreaks().stream()
                .max(Comparator.comparing(Streak::getStreak))
                .map(Streak::getStreak);
    }

    private Map<Integer, AttributeInfo> loadGameAttributes(int gameId) {
        return gameAttributes.computeIfAbsent(gameId, this::loadGameAttributesFromDb);
    }

    private Map<Integer, AttributeInfo> loadGameAttributesFromDb(int gameId) {
        try {
            return getContextHelper().readAttributesInfo(gameId);
        } catch (OasisException e) {
            throw new RuntimeException("Cannot load attributes!", e);
        }
    }
}
