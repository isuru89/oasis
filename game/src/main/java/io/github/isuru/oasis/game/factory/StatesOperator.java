package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.process.StatesProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.events.OStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

public class StatesOperator {

    public static DataStream<OStateEvent> createStateStream(OState oState,
                                                            KeyedStream<Event, Long> keyedUserStream,
                                                            Oasis oasis) {
        FilterFunction<Event> filter;
        if (oState.getCondition() != null) {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return value.getEventType().equals(oState.getEvent())
                            && Utils.evaluateCondition(oState.getCondition(), value.getAllFieldValues());
                }
            };
        } else {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) {
                    return value.getEventType().equals(oState.getEvent());
                }
            };
        }

        return keyedUserStream
                .process(new StatesProcess(oState, filter))
                .uid(String.format("%s-states-processor-%d", oasis.getId(), oState.getId()));
    }

}
