package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.process.race.AggregatedBucket;
import io.github.isuru.oasis.game.process.race.SumAggregator;
import io.github.isuru.oasis.game.process.windows.OasisTimeWindow;
import io.github.isuru.oasis.model.AggregatorType;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class RaceOperator {

    public static void createPipeline(DataStream<Event> inputEventStream,
                                      KeyedStream<Event, Long> userStream,
                                      Race race) {
        WindowedStream<Event, Long, TimeWindow> windowedStream = assignTimeWindow(userStream, race);

        DataStream<AggregatedBucket> aggregate;
        if (race.getAggregatorType() == AggregatorType.SUM) {
            aggregate = windowedStream.aggregate(new SumAggregator(race));
        } else if (race.getAggregatorType() == AggregatorType.MAX) {

        }
    }

    private static WindowedStream<Event, Long, TimeWindow> assignTimeWindow(
            KeyedStream<Event, Long> userStream,
            Race race) {
        if ("daily".equalsIgnoreCase(race.getTimewindow())) {
            return userStream.window(OasisTimeWindow.DAILY());
        } else if ("weekly".equalsIgnoreCase(race.getTimewindow())) {
            return userStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(race.getTimewindow())) {
            return userStream.window(OasisTimeWindow.WEEKLY());
        } else {
            throw new RuntimeException("Unknown timewindow period for the Race '" + race.getName() + "'!");
        }
    }
}
