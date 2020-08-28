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

import io.github.oasis.core.ID;
import io.github.oasis.core.collect.Record;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.services.AbstractStatsApiService;
import io.github.oasis.core.services.annotations.OasisQueryService;
import io.github.oasis.core.services.annotations.OasisStatEndPoint;
import io.github.oasis.core.services.annotations.QueryPayload;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog.BadgeLogRecord;
import io.github.oasis.elements.badges.stats.to.UserBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@OasisQueryService
public class BadgeStats extends AbstractStatsApiService {

    private final Map<Integer, Map<Integer, AttributeInfo>> gameAttributes = new ConcurrentHashMap<>();

    public BadgeStats(Db dbPool, OasisMetadataSupport contextSupport) {
        super(dbPool, contextSupport);
    }

    @OasisStatEndPoint(path = "/elements/badges/summary")
    public Object getBadgeSummary(@QueryPayload UserBadgeRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String badgeKey = ID.getGameUserBadgesSummary(request.getGameId(), request.getUserId());

            Mapped badgeSummary = db.MAP(badgeKey);

            List<String> subKeys = new ArrayList<>();
            subKeys.add("all");
            Map<String, SimpleElementDefinition> elementDefinitions = new HashMap<>();
            if (Utils.isNotEmpty(request.getRuleFilters())) {
                subKeys.addAll(request.getRuleFilters().stream().map(rule -> "rule:" + rule)
                        .collect(Collectors.toList()));

                if (Utils.isNotEmpty(request.getAttributeFilters())) {
                    subKeys.addAll(request.getRuleFilters().stream()
                            .flatMap(rule -> request.getAttributeFilters().stream().map(attr -> "rule:" + rule + ":" + attr))
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
                String[] parts = key.split(Constants.COLON);

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
    public Object getBadgeLog(@QueryPayload UserBadgeLogRequest request) throws Exception {
        try (DbContext db = getDbPool().createContext()) {

            String badgeLogKey = ID.getGameUserBadgesLog(request.getGameId(), request.getUserId());

            Sorted badgeLog = db.SORTED(badgeLogKey);
            List<Record> rangeRecords = badgeLog.getRangeByScoreWithScores(request.getTimeFrom(), request.getTimeTo());
            List<BadgeLogRecord> logRecords = new ArrayList<>();

            Set<String> badgeIds = new HashSet<>();
            for (Record record : rangeRecords) {
                long awardedTime = record.getScoreAsLong();
                String[] parts = record.getMember().split(Constants.COLON);

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
