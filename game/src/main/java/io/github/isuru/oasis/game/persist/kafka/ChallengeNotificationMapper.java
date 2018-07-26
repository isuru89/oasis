package io.github.isuru.oasis.game.persist.kafka;

import io.github.isuru.oasis.model.events.ChallengeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeNotificationMapper extends BaseNotificationMapper<ChallengeEvent> {
    @Override
    public String map(ChallengeEvent value) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", value.getUser());
        data.put("wonAt", value.getTimestamp());
        data.put("challengeId", value.getChallengeDef().getId());
        data.put("points", value.getChallengeDef().getPoints());
        data.put("eventExtId", value.getExternalId());

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
