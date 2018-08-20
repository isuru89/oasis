package io.github.isuru.oasis.game.process.challenge;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iweerarathna
 */
public class ChallengeWindowProcessor extends KeyedProcessFunction<Byte, Event, ChallengeEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeWindowProcessor.class);

    private ChallengeDef challengeDef;

    private final ValueStateDescriptor<Boolean> registeredStateDesc;
    private final ValueStateDescriptor<Long> deadlineStateDesc;
    private final ValueStateDescriptor<Long> winnerCountDesc;

    private boolean closed = false;

    public ChallengeWindowProcessor(ChallengeContext challengeContext) {
        this.challengeDef = challengeContext.getChallengeDef();

        Long cid = challengeDef.getId();
        registeredStateDesc = new ValueStateDescriptor<>(
                String.format("challenge-trigger-%d", cid), Boolean.class);
        deadlineStateDesc = new ValueStateDescriptor<>(
                String.format("challenge-trigger-deadline-%d", cid), Long.class);
        winnerCountDesc = new ValueStateDescriptor<>(
                String.format("challenge-winning-count-%d", cid), Long.class);
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<ChallengeEvent> out) throws Exception {
        if (closed) {
            return;
        }

        ValueState<Boolean> regState = getRuntimeContext().getState(registeredStateDesc);
        ValueState<Long> deadlineState = getRuntimeContext().getState(deadlineStateDesc);
        if (regState.value() == registeredStateDesc.getDefaultValue()) {
            long l = ctx.timerService().currentProcessingTime() + challengeDef.getExpireAfter();

            LOG.debug("Submitting challenge deadline at: {}", l);
            ctx.timerService().registerProcessingTimeTimer(l);
            deadlineState.update(l);
            regState.update(Boolean.TRUE);
        }

        ValueState<Long> winnerCount = getRuntimeContext().getState(winnerCountDesc);
        if (winnerCount.value() == winnerCountDesc.getDefaultValue()) {
            winnerCount.update(0L);
        }

        if (ChallengeFilter.filter(value, challengeDef)) {
            winnerCount.update(winnerCount.value() + 1);
            out.collect(new ChallengeEvent(value, challengeDef));
        }

        if (winnerCount.value() >= challengeDef.getWinnerCount()) {
            closed = true;
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<ChallengeEvent> out) throws Exception {
        ValueState<Long> deadlineState = getRuntimeContext().getState(deadlineStateDesc);
        LOG.info("Fired.");
        if (timestamp == deadlineState.value()) {
            closed = true;
        }
    }
}
