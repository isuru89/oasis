package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.process.EventUserSelector;
import io.github.isuru.oasis.game.process.RatingProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Rating;
import io.github.isuru.oasis.model.events.RatingEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class RatingsOperator {

    public static DataStream<RatingEvent> createRatingStream(Rating rating,
                                                             DataStream<Event> eventDataStream,
                                                             Oasis oasis) {
        FilterFunction<Event> filter;
        if (rating.getCondition() != null) {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return Utils.eventEquals(value, rating.getEvent())
                            && Utils.evaluateCondition(rating.getCondition(), value.getAllFieldValues());
                }
            };
        } else {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) {
                    return Utils.eventEquals(value, rating.getEvent());
                }
            };
        }

        return eventDataStream.filter(filter)
                .keyBy(new EventUserSelector<>())
                .process(new RatingProcess(rating))
                .uid(String.format("oasis-%s-states-processor-%d", oasis.getId(), rating.getId()));
    }

}
