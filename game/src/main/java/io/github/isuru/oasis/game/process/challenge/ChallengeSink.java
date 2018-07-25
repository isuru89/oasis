package io.github.isuru.oasis.game.process.challenge;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class ChallengeSink extends RichSinkFunction<ChallengeEvent> {

    private SinkFunction<ChallengeEvent> delegated;

    public ChallengeSink(SinkFunction<ChallengeEvent> delegated) {
        this.delegated = delegated;
    }

    @Override
    public void invoke(ChallengeEvent value, Context context) throws Exception {
        if (delegated != null) {
            delegated.invoke(value, context);
        }
    }


}
