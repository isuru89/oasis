package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.handlers.IMilestoneHandler;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;

public class MilestoneCollector implements IMilestoneHandler {

    private String sinkId;

    public MilestoneCollector(String sinkId) {
        this.sinkId = sinkId;
    }

    @Override
    public void milestoneReached(Long user, int level, Event event, Milestone milestone) {
        Memo.addMilestone(sinkId, Tuple4.of(user, level, event, milestone));
    }

    @Override
    public void onMilestoneError(Throwable ex, Event e, Milestone rule) {
        Memo.addMilestoneError(sinkId, Tuple3.of(ex, e, rule));
    }

}
