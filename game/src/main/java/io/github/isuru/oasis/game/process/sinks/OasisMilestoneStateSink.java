package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisMilestoneStateSink implements SinkFunction<MilestoneStateEvent> {

    private IMilestoneHandler milestoneHandler;

    public OasisMilestoneStateSink(IMilestoneHandler milestoneHandler) {
        this.milestoneHandler = milestoneHandler;
    }

    @Override
    public void invoke(MilestoneStateEvent value, Context context) throws Exception {
        if (value.isDouble()) {
            milestoneHandler.addMilestoneCurrState(value.getUserId(),
                    value.getMilestone(),
                    value.getValue(),
                    value.getNextValue());
        } else {
            milestoneHandler.addMilestoneCurrState(value.getUserId(),
                    value.getMilestone(),
                    value.getValueInt(),
                    value.getNextValueInt());
        }
    }
}
