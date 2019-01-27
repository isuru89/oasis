package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.process.RaceProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class RaceOperator {

    public static DataStream<PointNotification> createRacePipeline(DataStream<Event> eventDataStream,
                                                                  PointRule pointRule) {
        return eventDataStream
                .filter(new FilterFunction<Event>() {
                    @Override
                    public boolean filter(Event event) {
                        return Utils.eventEquals(event, EventNames.OASIS_EVENT_RACE_AWARD);
                    }
                }).process(new RaceProcess(pointRule)).uid("oasis-race-process");
    }

}
