package io.github.oasis.game.states;

import io.github.oasis.model.defs.ChallengeDef;

import java.io.Serializable;

/**
 * State representing all challenge data.
 *
 * @author Isuru Weerarathna
 */
public class ChallengeState implements Serializable {

    private long challengeId;

    private int winners;

    public ChallengeState() {
    }

    public ChallengeState(long challengeId) {
        this.challengeId = challengeId;
    }

    public long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(long challengeId) {
        this.challengeId = challengeId;
    }

    public boolean allowNoMoreWinners(ChallengeDef def) {
        return winners >= def.getWinnerCount();
    }

    public int incrementAndGetWinningNumber() {
        return winners++;
    }

    @Override
    public String toString() {
        return "ChallengeState{" +
                "challengeId=" + challengeId +
                ", winners=" + winners +
                '}';
    }
}
