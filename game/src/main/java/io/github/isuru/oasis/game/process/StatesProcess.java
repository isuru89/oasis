package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.events.OStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatesProcess extends KeyedProcessFunction<Long, Event, OStateEvent> {

    private final ValueStateDescriptor<Integer> currStateDesc;

    private OState oState;
    private List<OState.OAState> orderedStates;
    private FilterFunction<Event> eventCondition;

    private ValueState<Integer> currState;

    public StatesProcess(OState oState, FilterFunction<Event> eventCondition) {
        this.oState = oState;
        this.eventCondition = eventCondition;
        this.orderedStates = oState.getStates();

        currStateDesc = new ValueStateDescriptor<>(String.format("states-%d-curr-state", oState.getId()),
                        Integer.class);
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<OStateEvent> out) throws Exception {
        initDefaultState();

        Map<String, Object> allFieldValues = value.getAllFieldValues();
        int previousState = currState.value();
        for (OState.OAState oaState : orderedStates) {
            if (Utils.evaluateCondition(oaState.getCondition(), allFieldValues)) {
                // this is the state
                Serializable stateValueExpression = oState.getStateValueExpression();
                String cv = String.valueOf(Utils.executeExpression(stateValueExpression, allFieldValues));

                currState.update(oaState.getId());

                out.collect(new OStateEvent(value.getUser(),
                        oState,
                        value,
                        previousState,
                        oaState,
                        cv));
                break;
            }
        }
    }

    private void initDefaultState() throws IOException {
        if (Objects.equals(currState.value(), currStateDesc.getDefaultValue())) {
            currState.update(oState.getDefaultState());
        }
    }

    @Override
    public void open(Configuration parameters) {
        currState = getRuntimeContext().getState(currStateDesc);
    }
}
