package io.github.isuru.oasis.injector;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.output.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumerInterceptor implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerInterceptor.class);

    private static final String SCRIPT_ADD_FEED = "game/batch/addFeed";

    private final ContextInfo contextInfo;
    private final BufferedRecords buffer;
    private IOasisDao dao;

    ConsumerInterceptor(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;

        buffer = new BufferedRecords(this::flush);
    }

    public void init(IOasisDao dao) {
        this.dao = dao;
        buffer.init(contextInfo.getPool());
    }

    public void consume(Object value) {
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

        } else if (value instanceof OStateModel) {
            OStateModel stateModel = (OStateModel) value;

            if (!stateModel.getPreviousState().equals(stateModel.getCurrentState())) {
                Pair<String, String> eInfo = deriveCausedEvent(stateModel.getEvent());

                Map<String, Object> data = new HashMap<>();
                data.put("gameId", stateModel.getGameId());
                data.put("userId", stateModel.getUserId());
                data.put("teamId", stateModel.getTeamId());
                data.put("teamScopeId", stateModel.getTeamScopeId());
                data.put("defKindId", OasisDefinition.STATE.getTypeId());
                data.put("defId", stateModel.getStateId());
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
        }
    }

    private Pair<String, String> deriveCausedEvent(List<JsonEvent> events) {
        if (events == null || events.isEmpty()) {
            return null;
        } else {
            JsonEvent event = events.get(events.size() - 1);
            return Pair.of(event.getEventType(), event.getExternalId());
        }
    }

    private Pair<String, String> deriveCausedEvent(JsonEvent event) {
        if (event == null) {
            return null;
        } else {
            return Pair.of(event.getEventType(), event.getExternalId());
        }
    }

    private void flush(List<BufferedRecords.ElementRecord> records) {
        List<Map<String, Object>> maps = records.stream()
                .map(BufferedRecords.ElementRecord::getData)
                .collect(Collectors.toList());
        try {
            dao.executeBatchInsert(SCRIPT_ADD_FEED, maps);
        } catch (DbException e) {
            LOG.error("Error at inserting feed records!", e);
        }
    }

    @Override
    public void close() {
        buffer.close();
    }
}
