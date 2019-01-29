package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.output.RaceModel;

public class RaceNotificationMapper extends BaseNotificationMapper<RaceEvent, RaceModel> {

    @Override
    RaceModel create(RaceEvent raceEvent) {
        RaceModel model = new RaceModel();
        model.setGameId(raceEvent.getGameId());
        model.setUserId(raceEvent.getUser());
        model.setTeamId(raceEvent.getTeam());
        model.setTeamScopeId(raceEvent.getTeamScope());

        model.setRaceStartedAt(raceEvent.getRaceStartedAt());
        model.setRaceEndedAt(raceEvent.getRaceEndedAt());
        model.setScoredCount(raceEvent.getScoredCount());

        model.setRaceId(raceEvent.getRaceId());
        model.setRank(raceEvent.getRank());

        model.setPoints(raceEvent.getAwardedPoints());
        model.setScoredPoints(raceEvent.getScoredPoints());

        model.setTs(raceEvent.getTimestamp());
        model.setSourceId(raceEvent.getSource());

        return model;
    }
}
