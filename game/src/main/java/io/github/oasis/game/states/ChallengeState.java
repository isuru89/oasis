package io.github.oasis.game.states;

import io.github.oasis.model.defs.ChallengeDef;

import java.io.Serializable;

/**
 * State representing all challenge data.
 *
 * @author Isuru Weerarathna
 */
public class ChallengeState implements Serializable {

    private int winners;

    public boolean allowNoMoreWinners(ChallengeDef def) {
        return winners >= def.getWinnerCount();
    }

    public int getWinning() {
        return winners++;
    }

}
