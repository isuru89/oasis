package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IChallengeHandler;
import org.apache.flink.api.java.tuple.Tuple4;

/**
 * @author iweerarathna
 */
public class ChallengeSink implements IChallengeHandler {

    private String name;

    public ChallengeSink(String name) {
        this.name = name;
    }

    @Override
    public void addChallengeWinner(ChallengeEvent challengeEvent) {
        Memo.addChallenge(name, Tuple4.of(challengeEvent.getUser(),
                challengeEvent.getExternalId(),
                challengeEvent.getChallengeId(),
                challengeEvent.getPoints()));
    }
}
