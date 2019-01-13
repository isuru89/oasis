package io.github.isuru.oasis.services.dto.stats;

import io.github.isuru.oasis.model.defs.ChallengeDef;

import java.util.List;

/**
 * @author iweerarathna
 */
public class ChallengeInfoDto {

    private ChallengeDef challengeDef;

    private List<ChallengeWinnerDto> winners;

    public List<ChallengeWinnerDto> getWinners() {
        return winners;
    }

    public void setWinners(List<ChallengeWinnerDto> winners) {
        this.winners = winners;
    }

    public ChallengeDef getChallengeDef() {
        return challengeDef;
    }

    public void setChallengeDef(ChallengeDef challengeDef) {
        this.challengeDef = challengeDef;
    }
}
