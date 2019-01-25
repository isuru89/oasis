package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.events.ChallengeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class NotificationUtils {

    public static Map<String, Object> mapChallenge(ChallengeEvent value) {
        Map<String, Object> data = new HashMap<>();
        data.put("teamId", value.getTeam());
        data.put("teamScopeId", value.getTeamScope());
        data.put("userId", value.getUser());
        data.put("wonAt", value.getTimestamp());
        data.put("challengeId", value.getChallengeId());
        data.put("points", value.getPoints());
        data.put("eventExtId", value.getExternalId());
        data.put("ts", value.getTimestamp());
        data.put("sourceId", value.getSource());
        return data;
    }

}
