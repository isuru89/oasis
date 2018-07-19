package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import io.github.isuru.oasis.model.handlers.MilestoneNotification;
import org.apache.flink.api.java.tuple.Tuple4;

public class MilestoneCollector implements IMilestoneHandler {

    private String sinkId;

    public MilestoneCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void milestoneReached(MilestoneNotification milestoneNotification) {
        Memo.addMilestone(sinkId, Tuple4.of(milestoneNotification.getUserId(),
                milestoneNotification.getLevel(),
                milestoneNotification.getEvent(),
                milestoneNotification.getMilestone()));
    }

}
