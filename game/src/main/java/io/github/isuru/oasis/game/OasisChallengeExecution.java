package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.game.persist.mappers.ChallengeNotificationMapper;
import io.github.isuru.oasis.game.process.challenge.ChallengeContext;
import io.github.isuru.oasis.game.process.challenge.ChallengeFilter;
import io.github.isuru.oasis.game.process.challenge.ChallengeWindowProcessor;
import io.github.isuru.oasis.game.process.sinks.OasisChallengeSink;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import org.apache.flink.api.java.functions.NullByteKeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisChallengeExecution {

    private SourceFunction<Event> eventSource;
    private OasisSink outputSink;
    private IOutputHandler outputHandler;

    private StreamExecutionEnvironment env;

    private Properties gameProperties;

    public OasisChallengeExecution build(Oasis oasis, ChallengeDef challenge) {
        if (gameProperties == null) gameProperties = new Properties();

        env = StreamExecutionEnvironment.getExecutionEnvironment();
        OasisExecution.appendCheckpointStatus(env, gameProperties);
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String oasisId = oasis.getId();

        ChallengeContext context = new ChallengeContext(challenge, eventSource);

        SingleOutputStreamOperator<ChallengeEvent> execution = env.addSource(eventSource)
                .uid(String.format("challenge-source-%s", oasisId))
                .filter(new ChallengeFilter(challenge))
                .uid(String.format("challenge-filter-events-%s", oasisId))
                .keyBy(new NullByteKeySelector<>())
                .process(new ChallengeWindowProcessor(context))
                .uid(String.format("challenge-processor-%s", oasisId))
                .forceNonParallel();

        if (outputSink != null) {
            execution.map(new ChallengeNotificationMapper())
                    .addSink(outputSink.createChallengeSink())
                    .setParallelism(1);

        } else if (outputHandler != null) {
            execution.addSink(new OasisChallengeSink(outputHandler.getChallengeHandler()))
                    .setParallelism(1);
        }

        return this;
    }

    public void start() throws Exception {
        env.execute();
    }

    public OasisChallengeExecution outputHandler(IOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
        this.outputSink = null;
        return this;
    }

    public OasisChallengeExecution withSource(SourceFunction<Event> source) {
        this.eventSource = source;
        return this;
    }

    OasisChallengeExecution outputHandler(OasisSink sink) {
        this.outputSink = sink;
        this.outputHandler = null;
        return this;
    }

    OasisChallengeExecution havingGameProperties(Properties properties) {
        this.gameProperties = properties;
        return this;
    }

    OasisSink getOutputSink() {
        return outputSink;
    }

    IOutputHandler getOutputHandler() {
        return outputHandler;
    }
}
