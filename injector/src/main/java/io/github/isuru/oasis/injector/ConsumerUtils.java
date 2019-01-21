package io.github.isuru.oasis.injector;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.handlers.output.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class ConsumerUtils {

    public static Map<String, Object> toMilestoneStateDaoData(long gameId, MilestoneStateModel stateModel) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", stateModel.getUserId());
        map.put("milestoneId", stateModel.getMilestoneId());
        map.put("valueDouble", stateModel.getValue());
        map.put("valueLong", stateModel.getValueInt() == Long.MIN_VALUE ? null : stateModel.getValueInt());
        map.put("nextVal", stateModel.getNextValue());
        map.put("nextValInt", stateModel.getNextValueInt());
        map.put("gameId", gameId);
        return map;
    }

    public static Map<String, Object> toMilestoneLossStateDaoData(long gameId, MilestoneStateModel stateModel) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", stateModel.getUserId());
        map.put("milestoneId", stateModel.getMilestoneId());
        map.put("lossVal", stateModel.getLossValue());
        map.put("lossValInt", stateModel.getLossValueInt());
        map.put("gameId", gameId);
        return map;
    }

    public static Map<String, Object> toStateDaoData(long gameId, OStateModel stateModel) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", stateModel.getUserId());
        map.put("teamId", stateModel.getTeamId());
        map.put("teamScopeId", stateModel.getTeamScopeId());
        map.put("stateId", stateModel.getStateId());
        map.put("currState", stateModel.getCurrentState());
        map.put("currValue", stateModel.getCurrentValue());
        map.put("currPoints", stateModel.getCurrentPoints());
        map.put("isCurrency", stateModel.isCurrency());
        map.put("extId", stateModel.getExtId());
        map.put("ts", stateModel.getTs());
        map.put("gameId", gameId);
        map.put("sourceId", stateModel.getSourceId());
        return map;
    }

    public static Map<String, Object> toMilestoneDaoData(long gameId, MilestoneModel milestoneModel) {
        Map<String, Object> map = new HashMap<>();
        Event event = milestoneModel.getEvent();

        map.put("userId", milestoneModel.getUserId());
        map.put("teamId", event.getTeam());
        map.put("eventType", event.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("milestoneId", milestoneModel.getMilestoneId());
        map.put("level", milestoneModel.getLevel());
        map.put("gameId", gameId);
        return map;
    }

    public static Map<String, Object> toChallengeDaoData(long gameId, ChallengeModel challengeModel) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", challengeModel.getUserId());
        map.put("teamId", challengeModel.getTeamId());
        map.put("teamScopeId", challengeModel.getTeamScopeId());
        map.put("challengeId", challengeModel.getChallengeId());
        map.put("points", challengeModel.getPoints());
        map.put("wonAt", challengeModel.getWonAt());
        map.put("gameId", gameId);
        map.put("sourceId", challengeModel.getSourceId());
        return map;
    }

    public static Map<String, Object> toPointDaoData(long gameId, PointModel pointModel) {
        Event event = pointModel.getEvents().get(0);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", pointModel.getUserId());
        map.put("teamId", event.getTeam());
        map.put("teamScopeId", event.getTeamScope());
        map.put("eventType", pointModel.getEventType());
        map.put("extId", event.getExternalId());
        map.put("ts", event.getTimestamp());
        map.put("pointId", pointModel.getRuleId());
        map.put("pointName", pointModel.getRuleName());
        map.put("points", pointModel.getAmount());
        map.put("isCurrency", pointModel.getCurrency());
        map.put("gameId", gameId);
        map.put("sourceId", pointModel.getSourceId());
        map.put("tag", pointModel.getTag());
        return map;
    }

    public static Map<String, Object> toBadgeDaoData(long gameId, BadgeModel badgeModel) {
        Event first = badgeModel.getEvents().get(0);
        Event last = badgeModel.getEvents().get(badgeModel.getEvents().size() - 1);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", badgeModel.getUserId());
        map.put("teamId", last.getTeam());
        map.put("teamScopeId", last.getTeamScope());
        map.put("eventType", badgeModel.getEventType());
        map.put("extId", last.getExternalId());
        map.put("ts", last.getTimestamp());
        map.put("badgeId", badgeModel.getBadgeId());
        map.put("subBadgeId", badgeModel.getSubBadgeId() != null ? badgeModel.getSubBadgeId() : "");
        map.put("startExtId", first.getExternalId());
        map.put("endExtId", last.getExternalId());
        map.put("startTime", first.getTimestamp());
        map.put("endTime", last.getTimestamp());
        map.put("gameId", gameId);
        map.put("sourceId", last.getSource());
        map.put("tag", badgeModel.getTag());
        return map;
    }

}
