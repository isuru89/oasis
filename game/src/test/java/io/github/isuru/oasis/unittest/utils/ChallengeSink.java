package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class ChallengeSink implements SinkFunction<ChallengeEvent> {

    private String name;

    public ChallengeSink(String name) {
        this.name = name;
    }

    @Override
    public void invoke(ChallengeEvent value, Context context) throws Exception {
        Memo.addChallenge(name, value);
    }
}
