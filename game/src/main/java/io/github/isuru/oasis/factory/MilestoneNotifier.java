package io.github.isuru.oasis.factory;

import io.github.isuru.oasis.model.MilestoneEvent;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author iweerarathna
 */
public class MilestoneNotifier implements MapFunction<MilestoneEvent, MilestoneEvent> {

    private final IMilestoneHandler handler;

    public MilestoneNotifier(IMilestoneHandler handler) {
        this.handler = handler;
    }

    @Override
    public MilestoneEvent map(MilestoneEvent value) throws Exception {
        handler.milestoneReached(value.getUser(), value.getLevel(), value, value.getMilestone());
        return value;
    }
}
