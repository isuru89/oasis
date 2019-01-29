package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.IRaceHandler;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class OasisRaceSink implements SinkFunction<RaceEvent> {

    private IRaceHandler raceHandler;

    public OasisRaceSink(IRaceHandler raceHandler) {
        this.raceHandler = raceHandler;
    }

    @Override
    public void invoke(RaceEvent value, Context context) {
        raceHandler.addRaceWinner(value);
    }
}
