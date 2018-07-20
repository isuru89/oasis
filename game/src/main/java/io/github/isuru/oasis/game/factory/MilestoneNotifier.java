package io.github.isuru.oasis.game.factory;

import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author iweerarathna
 */
public class MilestoneNotifier implements MapFunction<MilestoneEvent, MilestoneNotification> {

    @Override
    public MilestoneNotification map(MilestoneEvent value) {
        return new MilestoneNotification(value.getUser(),
                value.getLevel(),
                value,
                value.getMilestone());
    }
}
