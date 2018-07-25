package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.process.challenge.ChallengeContext;
import io.github.isuru.oasis.game.process.challenge.ChallengeFilter;
import io.github.isuru.oasis.game.process.challenge.ChallengeSink;
import io.github.isuru.oasis.game.process.challenge.ChallengeWindowProcessor;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import org.apache.flink.api.java.functions.NullByteKeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @author iweerarathna
 */
public class OasisChallengeExecution {

    private SourceFunction<Event> eventSource;
    private SinkFunction<ChallengeEvent> outputHandler;

    private StreamExecutionEnvironment env;

    public OasisChallengeExecution build(Oasis oasis, ChallengeDef challenge) {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.enableCheckpointing(15000, CheckpointingMode.EXACTLY_ONCE);
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String oasisId = oasis.getId();

        ChallengeContext context = new ChallengeContext(challenge, eventSource);

        env.addSource(eventSource)
                .uid(String.format("challenge-source-%s", oasisId))
                .filter(new ChallengeFilter(challenge))
                .uid(String.format("challenge-filter-events-%s", oasisId))
                .keyBy(new NullByteKeySelector<>())
                .process(new ChallengeWindowProcessor(context))
                .uid(String.format("challenge-processor-%s", oasisId))
                .forceNonParallel()
                .addSink(new ChallengeSink(outputHandler))
                .setParallelism(1);

        return this;
    }

    public void execute() throws Exception {
        env.execute();
    }

    public OasisChallengeExecution withSource(SourceFunction<Event> source) {
        this.eventSource = source;
        return this;
    }

    public OasisChallengeExecution outputHandler(SinkFunction<ChallengeEvent> sink) {
        this.outputHandler = sink;
        return this;
    }

}
