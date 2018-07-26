package io.github.isuru.oasis.game;

import io.github.isuru.oasis.game.persist.OasisKafkaSink;
import io.github.isuru.oasis.game.persist.kafka.ChallengeNotificationMapper;
import io.github.isuru.oasis.game.process.challenge.ChallengeContext;
import io.github.isuru.oasis.game.process.challenge.ChallengeFilter;
import io.github.isuru.oasis.game.process.challenge.ChallengeWindowProcessor;
import io.github.isuru.oasis.game.process.sinks.OasisChallengeSink;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IOutputHandler;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.NullByteKeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

/**
 * @author iweerarathna
 */
public class OasisChallengeExecution {

    private SourceFunction<Event> eventSource;
    private OasisKafkaSink outputSink;
    private IOutputHandler outputHandler;

    private StreamExecutionEnvironment env;

    public OasisChallengeExecution build(Oasis oasis, ChallengeDef challenge) {
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.enableCheckpointing(15000, CheckpointingMode.EXACTLY_ONCE);
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
                    .addSink(new FlinkKafkaProducer011<>(outputSink.getKafkaHost(),
                            outputSink.getTopicChallengeWinners(),
                            new SimpleStringSchema()))
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

    OasisChallengeExecution outputHandler(OasisKafkaSink sink) {
        this.outputSink = sink;
        this.outputHandler = null;
        return this;
    }

}
