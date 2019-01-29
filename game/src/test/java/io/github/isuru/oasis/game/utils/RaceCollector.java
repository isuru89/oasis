package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.IRaceHandler;
import org.apache.flink.api.java.tuple.Tuple4;

public class RaceCollector implements IRaceHandler {

    private String sinkId;

    public RaceCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void addRaceWinner(RaceEvent raceEvent) {
        Memo.addRace(sinkId, Tuple4.of(raceEvent.getUser(),
                ((Number)raceEvent.getFieldValue(RaceEvent.KEY_DEF_ID)).longValue(),
                ((Number)raceEvent.getFieldValue(RaceEvent.KEY_POINTS)).doubleValue(),
                raceEvent.getExternalId()));
    }
}
