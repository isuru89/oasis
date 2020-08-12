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
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog.BadgeLogRecord;
import io.github.oasis.elements.badges.stats.to.UserBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class BadgeStats {

    private final Db dbPool;

    public BadgeStats(Db dbPool) {
        this.dbPool = dbPool;
    }

    public Object getBadgeSummary(UserBadgeRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            String badgeKey = ID.getGameUserBadgesSummary(request.getGameId(), request.getUserId());

            Mapped badgeSummary = db.MAP(badgeKey);

            List<String> subKeys = new ArrayList<>();
            subKeys.add("all");
            if (Utils.isNotEmpty(request.getRuleFilters())) {
                subKeys.addAll(request.getRuleFilters().stream().map(rule -> "rule:" + rule)
                        .collect(Collectors.toList()));

                if (Utils.isNotEmpty(request.getAttributeFilters())) {
                    subKeys.addAll(request.getRuleFilters().stream()
                            .flatMap(rule -> request.getAttributeFilters().stream().map(attr -> "rule:" + rule + ":" + attr))
                            .collect(Collectors.toList()));
                }

            } else if (Utils.isNotEmpty(request.getAttributeFilters())) {
                subKeys.addAll(request.getAttributeFilters().stream().map(attr -> "attr:" + attr)
                        .collect(Collectors.toList()));
            }

            UserBadgeSummary summary = new UserBadgeSummary();
            summary.setGameId(request.getGameId());
            summary.setUserId(request.getUserId());

            List<String> countValues = badgeSummary.getValues(subKeys.toArray(new String[0]));
            summary.setTotalBadges(Numbers.asInt(countValues.get(0)));
            for (int i = 1; i < subKeys.size(); i++) {
                String key = subKeys.get(i);
                String[] parts = key.split(Constants.COLON);

                if ("attr".equals(parts[0])) {
                     summary.addSummaryStat(parts[1], new UserBadgeSummary.AttributeSummaryStat(
                            Numbers.asInt(parts[1]), Numbers.asInt(countValues.get(i)), null));
                } else {
                    if (parts.length == 3) {
                        int attr = Numbers.asInt(parts[2]);
                        summary.addRuleStat(parts[1], attr, new UserBadgeSummary.AttributeSummaryStat(
                                attr, Numbers.asInt(countValues.get(i)), null));
                    } else if (parts.length == 2) {
                        summary.addRuleSummary(parts[1], Numbers.asInt(countValues.get(i)), null);
                    }
                }
            }

            return summary;
        }
    }

    public Object getBadgeLog(UserBadgeLogRequest request) throws Exception {
        try (DbContext db = dbPool.createContext()) {

            String badgeLogKey = ID.getGameUserBadgesLog(request.getGameId(), request.getUserId());

            Sorted badgeLog = db.SORTED(badgeLogKey);
            List<Record> rangeRecords = badgeLog.getRangeByScoreWithScores(request.getTimeFrom(), request.getTimeTo());
            List<BadgeLogRecord> logRecords = new ArrayList<>();

            for (Record record : rangeRecords) {
                long awardedTime = record.getScoreAsLong();
                String[] parts = record.getMember().split(Constants.COLON);

                if (parts.length < 2) {
                    continue;
                }

                if (parts[2].contains("-")) {
                    logRecords.add(new BadgeLogRecord(parts[0], Numbers.asInt(parts[1]), parts[2], awardedTime));
                } else {
                    logRecords.add(new BadgeLogRecord(parts[0], Numbers.asInt(parts[1]), Numbers.asLong(parts[2]), awardedTime));
                }
            }

            UserBadgeLog userBadgeLog = new UserBadgeLog();
            userBadgeLog.setGameId(request.getGameId());
            userBadgeLog.setUserId(request.getUserId());
            userBadgeLog.setLog(logRecords);

            return userBadgeLog;
        }
    }
}
