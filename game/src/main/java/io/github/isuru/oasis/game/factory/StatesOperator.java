package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.game.Oasis;
import io.github.isuru.oasis.game.process.EventUserSelector;
import io.github.isuru.oasis.game.process.StatesProcess;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.events.OStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class StatesOperator {

    public static DataStream<OStateEvent> createStateStream(OState oState,
                                                            DataStream<Event> eventDataStream,
                                                            Oasis oasis) {
        FilterFunction<Event> filter;
        if (oState.getCondition() != null) {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return Utils.eventEquals(value, oState.getEvent())
                            && Utils.evaluateCondition(oState.getCondition(), value.getAllFieldValues());
                }
            };
        } else {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) {
                    return Utils.eventEquals(value, oState.getEvent());
                }
            };
        }

        return eventDataStream.filter(filter)
                .keyBy(new EventUserSelector<>())
                .process(new StatesProcess(oState, filter))
                .uid(String.format("oasis-%s-states-processor-%d", oasis.getId(), oState.getId()));
    }

}
