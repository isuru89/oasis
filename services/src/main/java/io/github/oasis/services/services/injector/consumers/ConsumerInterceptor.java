/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.injector.consumers;

import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.db.DbException;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.defs.OasisDefinition;
import io.github.oasis.model.events.JsonEvent;
import io.github.oasis.model.handlers.output.BadgeModel;
import io.github.oasis.model.handlers.output.ChallengeModel;
import io.github.oasis.model.handlers.output.MilestoneModel;
import io.github.oasis.model.handlers.output.PointModel;
import io.github.oasis.model.handlers.output.RaceModel;
import io.github.oasis.model.handlers.output.RatingModel;
import io.github.oasis.services.services.injector.ConsumerContext;
import io.github.oasis.services.utils.BufferedRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConsumerInterceptor implements Consumer<Object>, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerInterceptor.class);

    private static final String SCRIPT_ADD_FEED = "game/batch/addFeed";

    private final ConsumerContext contextInfo;
    private final BufferedRecords buffer;
    private IOasisDao dao;

    public ConsumerInterceptor(ConsumerContext contextInfo, BufferedRecords bufferRef) {
        this.contextInfo = contextInfo;

        buffer = bufferRef;
    }

    public ConsumerInterceptor(ConsumerContext contextInfo) {
        this.contextInfo = contextInfo;

        buffer = new BufferedRecords(this::flush);
    }

    public void init(IOasisDao dao) {
        this.dao = dao;
        buffer.init(contextInfo.getPool());
    }

    @Override
    public void accept(Object value) {
        long ts = System.currentTimeMillis();
        if (value instanceof BadgeModel) {
            // a badge award
            BadgeModel badgeModel = (BadgeModel) value;
            Pair<String, String> eInfo = deriveCausedEvent(badgeModel.getEvents());
            Map<String, Object> data = new HashMap<>();
            data.put("gameId", badgeModel.getGameId());
            data.put("userId", badgeModel.getUserId());
            data.put("teamId", badgeModel.getTeamId());
            data.put("teamScopeId", badgeModel.getTeamScopeId());
            data.put("defKindId", OasisDefinition.BADGE.getTypeId());
            data.put("defId", badgeModel.getBadgeId());
            data.put("actionId", 1);
            data.put("message", null);
            data.put("subMessage", null);
            data.put("causedEvent", eInfo.getValue1());
            data.put("eventType", eInfo.getValue0());
            data.put("tag", badgeModel.getTag());
            data.put("ts", badgeModel.getTs());

            buffer.push(new BufferedRecords.ElementRecord(data, ts));

        } else if (value instanceof PointModel) {
            // a point award
            PointModel pointModel = (PointModel) value;
            Pair<String, String> eInfo = deriveCausedEvent(pointModel.getEvents());
            Map<String, Object> data = new HashMap<>();
            data.put("gameId", pointModel.getGameId());
            data.put("userId", pointModel.getUserId());
            data.put("teamId", pointModel.getTeamId());
            data.put("teamScopeId", pointModel.getTeamScopeId());
            data.put("defKindId", OasisDefinition.POINT.getTypeId());
            data.put("defId", pointModel.getRuleId());
            data.put("actionId", 1);
            data.put("message", null);
            data.put("subMessage", null);
            data.put("causedEvent", eInfo.getValue1());
            data.put("eventType", eInfo.getValue0());
            data.put("tag", pointModel.getTag());
            data.put("ts", pointModel.getTs());

            buffer.push(new BufferedRecords.ElementRecord(data, ts));

        } else if (value instanceof MilestoneModel) {
            // a milestone reached
            MilestoneModel milestoneModel = (MilestoneModel) value;
            Pair<String, String> eInfo = deriveCausedEvent(milestoneModel.getEvent());
            Map<String, Object> data = new HashMap<>();
            data.put("gameId", milestoneModel.getGameId());
            data.put("userId", milestoneModel.getUserId());
            data.put("teamId", milestoneModel.getTeamId());
            data.put("teamScopeId", milestoneModel.getTeamScopeId());
            data.put("defKindId", OasisDefinition.MILESTONE.getTypeId());
            data.put("defId", milestoneModel.getMilestoneId());
            data.put("actionId", 1);
            data.put("message", String.format("Reached level %d", milestoneModel.getLevel()));
            data.put("subMessage", null);
            data.put("causedEvent", eInfo.getValue1());
            data.put("eventType", eInfo.getValue0());
            data.put("tag", null);
            data.put("ts", milestoneModel.getTs());

            buffer.push(new BufferedRecords.ElementRecord(data, ts));

        } else if (value instanceof RatingModel) {
            RatingModel stateModel = (RatingModel) value;

            if (!stateModel.getPreviousState().equals(stateModel.getCurrentState())) {
                Pair<String, String> eInfo = deriveCausedEvent(stateModel.getEvent());

                Map<String, Object> data = new HashMap<>();
                data.put("gameId", stateModel.getGameId());
                data.put("userId", stateModel.getUserId());
                data.put("teamId", stateModel.getTeamId());
                data.put("teamScopeId", stateModel.getTeamScopeId());
                data.put("defKindId", OasisDefinition.RATING.getTypeId());
                data.put("defId", stateModel.getRatingId());
                data.put("actionId", 1);
                data.put("message", String.format("State changed from %s to %s",
                                stateModel.getPreviousStateName(),
                                stateModel.getCurrentStateName()));
                data.put("subMessage", null);
                data.put("causedEvent", eInfo.getValue1());
                data.put("eventType", eInfo.getValue0());
                data.put("tag", null);
                data.put("ts", stateModel.getTs());

                buffer.push(new BufferedRecords.ElementRecord(data, ts));
            }

        } else if (value instanceof ChallengeModel) {
            // a challenge won
            ChallengeModel challengeModel = (ChallengeModel) value;
            Map<String, Object> data = new HashMap<>();
            data.put("gameId", challengeModel.getGameId());
            data.put("userId", challengeModel.getUserId());
            data.put("teamId", challengeModel.getTeamId());
            data.put("teamScopeId", challengeModel.getTeamScopeId());
            data.put("defKindId", OasisDefinition.CHALLENGE.getTypeId());
            data.put("defId", challengeModel.getChallengeId());
            data.put("actionId", 1);
            data.put("message", null);
            data.put("subMessage", null);
            data.put("causedEvent", challengeModel.getEventExtId());
            data.put("eventType", null);
            data.put("tag", null);
            data.put("ts", challengeModel.getWonAt());

            buffer.push(new BufferedRecords.ElementRecord(data, ts));
        } else if (value instanceof RaceModel) {
            // a challenge won
            RaceModel raceModel = (RaceModel) value;
            Map<String, Object> data = new HashMap<>();
            data.put("gameId", raceModel.getGameId());
            data.put("userId", raceModel.getUserId());
            data.put("teamId", raceModel.getTeamId());
            data.put("teamScopeId", raceModel.getTeamScopeId());
            data.put("defKindId", OasisDefinition.RACE.getTypeId());
            data.put("defId", raceModel.getRaceId());
            data.put("actionId", 1);
            data.put("message", raceModel.getRank());
            data.put("subMessage", raceModel.getPoints());
            data.put("causedEvent", null);
            data.put("eventType", null);
            data.put("tag", raceModel.getScoredPoints());
            data.put("ts", raceModel.getRaceEndedAt());

            buffer.push(new BufferedRecords.ElementRecord(data, ts));
        }
    }

    Pair<String, String> deriveCausedEvent(List<JsonEvent> events) {
        if (events == null || events.isEmpty()) {
            return null;
        } else {
            JsonEvent event = events.get(events.size() - 1);
            return Pair.of(event.getEventType(), event.getExternalId());
        }
    }

    Pair<String, String> deriveCausedEvent(JsonEvent event) {
        if (event == null) {
            return null;
        } else {
            return Pair.of(event.getEventType(), event.getExternalId());
        }
    }

    void flush(List<BufferedRecords.ElementRecord> records) {
        if (records != null) {
            List<Map<String, Object>> maps = records.stream()
                    .map(BufferedRecords.ElementRecord::getData)
                    .collect(Collectors.toList());
            try {
                if (maps.size() > 0) {
                    dao.executeBatchInsert(SCRIPT_ADD_FEED, maps);
                }
            } catch (DbException e) {
                LOG.error("Error at inserting feed records!", e);
            }
        }
    }

    @Override
    public void close() {
        buffer.close();
    }
}
