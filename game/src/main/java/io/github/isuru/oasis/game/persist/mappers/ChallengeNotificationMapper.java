package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.NotificationUtils;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeNotificationMapper extends BaseNotificationMapper<ChallengeEvent> {
    @Override
    public String map(ChallengeEvent value) throws Exception {
        Map<String, Object> data = NotificationUtils.mapChallenge(value);

        return BaseNotificationMapper.OBJECT_MAPPER.writeValueAsString(data);
    }
}
