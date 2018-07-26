package io.github.isuru.oasis.game.process.sinks;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IChallengeHandler;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisChallengeSink implements SinkFunction<ChallengeEvent> {

    private IChallengeHandler challengeHandler;

    public OasisChallengeSink(IChallengeHandler challengeHandler) {
        this.challengeHandler = challengeHandler;
    }

    @Override
    public void invoke(ChallengeEvent value, Context context) {
        challengeHandler.addChallengeWinner(value);
    }
}
