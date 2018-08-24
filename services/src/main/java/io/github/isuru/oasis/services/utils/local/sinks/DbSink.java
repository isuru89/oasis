package io.github.isuru.oasis.services.utils.local.sinks;

import io.github.isuru.oasis.game.persist.OasisSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class DbSink extends OasisSink {

    private OutputSink badgeSink;
    private OutputSink pointsSink;
    private OutputSink challengeSink;
    private OutputSink milestoneSink;
    private OutputSink milestoneStateSink;
    private OutputSink stateSink;

    public DbSink(long gameId) {
        badgeSink = new OutputSink(gameId, LocalSinks.SQ_BADGES);
        pointsSink = new OutputSink(gameId, LocalSinks.SQ_POINTS);
        challengeSink = new OutputSink(gameId, LocalSinks.SQ_CHALLENGES);
        milestoneSink = new OutputSink(gameId, LocalSinks.SQ_MILESTONES);
        milestoneStateSink = new OutputSink(gameId, LocalSinks.SQ_MILESTONE_STATES);
        stateSink = new OutputSink(gameId, LocalSinks.SQ_STATES);
    }

    @Override
    public SinkFunction<String> createPointSink() {
        return pointsSink;
    }

    @Override
    public SinkFunction<String> createMilestoneSink() {
        return milestoneSink;
    }

    @Override
    public SinkFunction<String> createMilestoneStateSink() {
        return milestoneStateSink;
    }

    @Override
    public SinkFunction<String> createBadgeSink() {
        return badgeSink;
    }

    @Override
    public SinkFunction<String> createChallengeSink() {
        return challengeSink;
    }

    @Override
    public SinkFunction<String> createStatesSink() {
        return stateSink;
    }

    public static class OutputSink implements SinkFunction<String> {
        private final long gid;
        private final String name;

        OutputSink(long gid, String name) {
            this.gid = gid;
            this.name = name;
        }

        @Override
        public void invoke(String value, Context context) {
            try {
                SinkData.get().poll(gid, name).put(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
