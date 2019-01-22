package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.output.ChallengeModel;

/**
 * @author iweerarathna
 */
public class ChallengeNotificationMapper extends BaseNotificationMapper<ChallengeEvent, ChallengeModel> {

    @Override
    ChallengeModel create(ChallengeEvent notification) {
        ChallengeModel model = new ChallengeModel();
        model.setTeamId(notification.getTeam());
        model.setTeamScopeId(notification.getTeamScope());
        model.setUserId(notification.getUser());
        model.setWonAt(notification.getTimestamp());
        model.setChallengeId(notification.getChallengeDef().getId());
        model.setPoints(notification.getChallengeDef().getPoints());
        model.setEventExtId(notification.getExternalId());
        model.setTs(notification.getTimestamp());
        model.setSourceId(notification.getSource());
        return model;
    }
}
