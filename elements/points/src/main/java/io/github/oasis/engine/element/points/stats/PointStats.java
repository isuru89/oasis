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

package io.github.oasis.engine.element.points.stats;

import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.collect.Record;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.model.TimeScope;
import io.github.oasis.core.services.AbstractStatsApiService;
import io.github.oasis.core.services.EngineDataReader;
import io.github.oasis.core.services.annotations.OasisQueryService;
import io.github.oasis.core.services.annotations.OasisStatEndPoint;
import io.github.oasis.core.services.annotations.QueryPayload;
import io.github.oasis.core.services.exceptions.ApiQueryException;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Timestamps;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.engine.element.points.PointIDs;
import io.github.oasis.engine.element.points.stats.to.LeaderboardRequest;
import io.github.oasis.engine.element.points.stats.to.LeaderboardSummary;
import io.github.oasis.engine.element.points.stats.to.UserPointSummary;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest.PointsFilterScope;
import io.github.oasis.engine.element.points.stats.to.UserRankingRequest;
import io.github.oasis.engine.element.points.stats.to.UserRankingSummary;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.oasis.core.utils.Constants.COLON;

/**
 * @author Isuru Weerarathna
 */
@OasisQueryService
@Tag(name = "Points", description = "Retrieve points statistics")
public class PointStats extends AbstractStatsApiService {

    private static final String LEADERBOARD_RANK = "O.PLEADRANKS";
    private static final String LEADERBOARD_RANK_REVERSE = "O.PLEADRANKSREV";
    private static final String WITH_CARDINALITY = "withcard";

    public PointStats(EngineDataReader dataReader, OasisMetadataSupport contextSupport) {
        super(dataReader, contextSupport);
    }

    @ApiResponse(
            responseCode = "200", description = "Points summary for a user",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserPointSummary.class))
            }
    )
    @OasisStatEndPoint(path = "/elements/points/summary")
    public UserPointSummary getUserPoints(@QueryPayload UserPointsRequest request) throws OasisApiException {
        Validators.checkPointRequest(request);

        try (DbContext db = getDbPool().createContext()) {

            String key = PointIDs.getGameUserPointsSummary(request.getGameId(), request.getUserId());

            Mapped points = db.MAP(key);
            BigDecimal allPoints = Numbers.asDecimal(points.getValue("all"));

            UserPointSummary summary = new UserPointSummary();
            summary.setGameId(request.getGameId());
            summary.setUserId(request.getUserId());
            summary.setTotalPoints(allPoints);

            if (Objects.nonNull(request.getFilters())) {
                for (PointsFilterScope filter : request.getFilters()) {
                    List<String> subKeys = extrapolateKeys(filter);

                    if (subKeys != null) {
                        List<String> values = points.getValues(subKeys.toArray(new String[0]));
                        appendToSummary(summary, filter, subKeys, values);
                    }
                }
            }

            return summary;
        } catch (IOException e) {
            throw new ApiQueryException("Error while querying for points summary!", e);
        }
    }

    @ApiResponse(
            responseCode = "200", description = "Get leaderboard",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LeaderboardSummary.class))
            }
    )
    @OasisStatEndPoint(path = "/elements/points/leaderboard/summary")
    public Object getLeaderboard(@QueryPayload LeaderboardRequest request) throws OasisApiException {
        Validators.checkLeaderboardRequest(request);

        try (DbContext db = getDbPool().createContext()) {

            String leadKey;
            String trait = request.getTimeRange() == TimeScope.ALL
                ? "all"
                : String.valueOf(request.getTimeRange().name().charAt(0)).toLowerCase();
            String duration = request.getTimeRange() == TimeScope.ALL ? null : trait.toUpperCase() + request.getTime();
            if (request.isTeamScoped()) {
                leadKey = PointIDs.getGameTeamLeaderboard(request.getGameId(), request.getTeamId(), trait, duration);
            } else {
                leadKey = PointIDs.getGameLeaderboard(request.getGameId(), trait, duration);
            }

            LeaderboardSummary summary = new LeaderboardSummary();
            summary.setGameId(request.getGameId());
            summary.setTeamId(request.getTeamId());

            Sorted leaderboard = db.SORTED(leadKey);

            int minRank = request.getOffset() - 1;
            int maxRank = request.getOffset() + request.getLimit() - 1;
            List<Record> records;
            if (request.isDescendingOrder()) {
                records = leaderboard.getRevRangeByRankWithScores(minRank, maxRank);
            } else {
                records = leaderboard.getRangeByRankWithScores(minRank, maxRank);
            }

            List<String> userIds = records.stream().map(Record::getMember).collect(Collectors.toList());
            Map<String, UserMetadata> userNameMap = getContextHelper().readUsersByIdStrings(userIds);

            for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);
                summary.addRecord(new LeaderboardSummary.LeaderboardRecord(
                        request.getOffset() + i,
                        Long.parseLong(record.getMember()),
                        userNameMap.get(record.getMember()),
                        BigDecimal.valueOf(record.getScore())));
            }

            return summary;
        } catch (IOException | OasisException e) {
            throw new ApiQueryException("Error while querying for leaderboard records!", e);
        }
    }

    @ApiResponse(
            responseCode = "200", description = "Get user rankings",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRankingSummary.class))
            }
    )
    @OasisStatEndPoint(path = "/elements/points/rankings/summary", summary = "Rankings summary")
    @SuppressWarnings("unchecked")
    public Object getUserRankings(@QueryPayload UserRankingRequest request) throws OasisApiException {
        Validators.checkRankingRequest(request);

        try (DbContext db = getDbPool().createContext()) {

            List<Object> keysToRead = Stream.of(TimeScope.values())
                    .map(timeScope -> {
                        String trait = timeScope.getTrait();
                        String duration = timeScope == TimeScope.ALL ? null : Timestamps.formatKey(LocalDate.parse(request.getDate()), timeScope);
                        if (request.isTeamScoped()) {
                            return PointIDs.getGameTeamLeaderboard(request.getGameId(), request.getTeamId(), trait, duration);
                        }
                        return PointIDs.getGameLeaderboard(request.getGameId(), trait, duration);
                    })
                    .collect(Collectors.toList());

            UserRankingSummary summary = new UserRankingSummary();
            summary.setGameId(request.getGameId());
            summary.setUserId(request.getUserId());

            String cardinality = request.isIncludeTotalCount() ? WITH_CARDINALITY : "";
            String scriptToRun = request.isDescendingOrder() ? LEADERBOARD_RANK_REVERSE : LEADERBOARD_RANK;
            List<Object> values = (List<Object>) db.runScript(scriptToRun, keysToRead, request.getUserId(), cardinality);
            for (int i = 0; i < values.size(); i += 3) {
                Object rankVal = values.get(i);
                if (rankVal == null) {
                    continue;
                }

                int keyIdx = i / 3;
                String[] parts = ((String) keysToRead.get(keyIdx)).split(COLON);
                int rank = rankVal instanceof Number ? ((Number) rankVal).intValue() : Numbers.asInt(String.valueOf(rankVal));
                BigDecimal score = new BigDecimal(String.valueOf(values.get(i + 1)));
                Object totalVal = values.get(i + 2);
                Long total = totalVal instanceof Number ? ((Number) totalVal).longValue() : null;

                summary.addRankingDetail(parts[parts.length - 1], new UserRankingSummary.RankInfo(rank + 1, score, total));
            }

            return summary;
        } catch (IOException e) {
            throw new ApiQueryException("Error while querying for user ranking summary!", e);
        }
    }

    void appendToSummary(UserPointSummary summary, PointsFilterScope filterScope, List<String> keys, List<String> values) {
        UserPointSummary.StatResults results = new UserPointSummary.StatResults();
        for (int i = 0; i < keys.size(); i++) {
            String val = values.get(i);
            if (Objects.nonNull(val)) {
                results.addPointRecord(keys.get(i), new BigDecimal(val));
            }
        }

        summary.addSummary(Utils.firstNonNull(filterScope.getRefId(), filterScope.getType().name().toLowerCase()).toString(), results);
    }

    List<String> extrapolateKeys(PointsFilterScope filterScope) {
        if (filterScope.getType() == null) {
            return null;
        }

        String prefix = filterScope.getType().name().toLowerCase();
        UserPointsRequest.PointRange range = filterScope.getRange();
        if (range != null) {
            List<String> timeRanges = Timestamps.timeUnitsWithinRange(
                    LocalDate.parse(range.getFrom()),
                    LocalDate.parse(range.getTo()),
                    range.getType());
            if (Utils.isNotEmpty(filterScope.getValues())) {
                return timeRanges.stream().flatMap(tr -> filterScope.getValues().stream().map(val -> val + COLON + tr))
                        .map(val -> prefix + COLON + val)
                        .collect(Collectors.toList());
            } else {
                return timeRanges.stream().map(val -> prefix + COLON + val).collect(Collectors.toList());
            }

        } else if (Utils.isNotEmpty(filterScope.getValues())) {
            return filterScope.getValues().stream()
                    .map(val -> prefix + COLON + val)
                    .collect(Collectors.toList());
        }

        return null;
    }

}
