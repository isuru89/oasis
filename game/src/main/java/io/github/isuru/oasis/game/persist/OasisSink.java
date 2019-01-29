package io.github.isuru.oasis.game.persist;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public abstract class OasisSink {

    public abstract SinkFunction<String> createPointSink();
    public abstract SinkFunction<String> createMilestoneSink();
    public abstract SinkFunction<String> createMilestoneStateSink();
    public abstract SinkFunction<String> createBadgeSink();

    public abstract SinkFunction<String> createChallengeSink();
    public abstract SinkFunction<String> createRaceSink();
    public abstract SinkFunction<String> createStatesSink();

}
