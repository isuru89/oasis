package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.process.ChallengeProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

public class ChallengeOperator {

    public static ChallengePipelineResponse createChallengePipeline(DataStream<Event> eventDataStream,
                                                                    OutputTag<PointNotification> pointOutputTag,
                                                                    PointRule pointRule) {
        SingleOutputStreamOperator<ChallengeEvent> challengeStream = eventDataStream
                .filter(new FilterFunction<Event>() {
                    @Override
                    public boolean filter(Event event) {
                        return Utils.eventEquals(event, EventNames.OASIS_EVENT_CHALLENGE_WINNER);
                    }
                }).process(new ChallengeProcess(pointRule, pointOutputTag)).uid("oasis-challenge-process");

        DataStream<PointNotification> pointOutput = challengeStream.getSideOutput(pointOutputTag);
        return new ChallengePipelineResponse(pointOutput, challengeStream);
    }

    public static class ChallengePipelineResponse {
        private DataStream<PointNotification> pointNotificationStream;
        private DataStream<ChallengeEvent> challengeEventDataStream;

        ChallengePipelineResponse(DataStream<PointNotification> pointNotificationStream,
                                  DataStream<ChallengeEvent> challengeEventDataStream) {
            this.pointNotificationStream = pointNotificationStream;
            this.challengeEventDataStream = challengeEventDataStream;
        }

        public DataStream<PointNotification> getPointNotificationStream() {
            return pointNotificationStream;
        }

        public DataStream<ChallengeEvent> getChallengeEventDataStream() {
            return challengeEventDataStream;
        }
    }

}
