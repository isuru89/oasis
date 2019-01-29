package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.process.RaceProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

public class RaceOperator {

    public static RacePipelineResult createRacePipeline(DataStream<Event> eventDataStream,
                                                           OutputTag<PointNotification> pointOutput,
                                                           PointRule pointRule) {
        SingleOutputStreamOperator<RaceEvent> raceStream = eventDataStream
                .filter(new FilterFunction<Event>() {
                    @Override
                    public boolean filter(Event event) {
                        return Utils.eventEquals(event, EventNames.OASIS_EVENT_RACE_AWARD);
                    }
                }).process(new RaceProcess(pointRule, pointOutput)).uid("oasis-race-process");

        return new RacePipelineResult(raceStream, raceStream.getSideOutput(pointOutput));
    }

    public static class RacePipelineResult {
        private DataStream<RaceEvent> raceStream;
        private DataStream<PointNotification> pointStream;

        RacePipelineResult(DataStream<RaceEvent> raceStream, DataStream<PointNotification> pointStream) {
            this.raceStream = raceStream;
            this.pointStream = pointStream;
        }

        public DataStream<RaceEvent> getRaceStream() {
            return raceStream;
        }

        public DataStream<PointNotification> getPointStream() {
            return pointStream;
        }
    }

}
