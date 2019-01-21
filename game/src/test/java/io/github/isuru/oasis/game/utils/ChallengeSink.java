package io.github.isuru.oasis.game.utils;

import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.IChallengeHandler;

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
        Memo.addChallenge(name, challengeEvent);
    }
}
