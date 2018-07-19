package io.github.isuru.oasis.process.sinks;

import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisMilestoneSink implements SinkFunction<MilestoneNotification> {

    private IMilestoneHandler milestoneHandler;

    public OasisMilestoneSink(IMilestoneHandler milestoneHandler) {
        this.milestoneHandler = milestoneHandler;
    }

    @Override
    public void invoke(MilestoneNotification value, Context context) {
        milestoneHandler.milestoneReached(value);
    }

    public static class DiscardingMilestoneHandler implements IMilestoneHandler {
        @Override
        public void milestoneReached(MilestoneNotification milestoneNotification) {

        }
    }
}
