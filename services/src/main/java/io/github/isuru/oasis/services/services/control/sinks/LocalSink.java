package io.github.isuru.oasis.services.services.control.sinks;

import io.github.isuru.oasis.game.persist.OasisSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author iweerarathna
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalSink extends OasisSink {

    public static final String SQ_BADGES = "badges";
    public static final String SQ_POINTS = "points";
    public static final String SQ_MILESTONES = "milestones";
    public static final String SQ_MILESTONE_STATES = "milestone-states";
    public static final String SQ_RATINGS = "ratings";
    public static final String SQ_RACES = "races";
    public static final String SQ_CHALLENGES = "challenges";


    private OutputSink badgeSink;
    private OutputSink pointsSink;
    private OutputSink challengeSink;
    private OutputSink milestoneSink;
    private OutputSink milestoneStateSink;
    private OutputSink ratingSink;
    private OutputSink raceSink;

    public LocalSink() {
        badgeSink = new OutputSink(SQ_BADGES);
        pointsSink = new OutputSink(SQ_POINTS);
        challengeSink = new OutputSink(SQ_CHALLENGES);
        milestoneSink = new OutputSink(SQ_MILESTONES);
        milestoneStateSink = new OutputSink(SQ_MILESTONE_STATES);
        ratingSink = new OutputSink(SQ_RATINGS);
        raceSink = new OutputSink(SQ_RACES);
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
    public SinkFunction<String> createRaceSink() {
        return raceSink;
    }

    @Override
    public SinkFunction<String> createRatingSink() {
        return ratingSink;
    }

    public static class OutputSink implements SinkFunction<String> {
        private final String name;

        OutputSink(String name) {
            this.name = name;
        }

        @Override
        public void invoke(String value, Context context) {
            try {
                SinkData.get().poll(name).put(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
