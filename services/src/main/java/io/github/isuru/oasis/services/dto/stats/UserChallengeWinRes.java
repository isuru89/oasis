package io.github.isuru.oasis.services.dto.stats;

import java.util.List;

public class UserChallengeWinRes {

    private List<ChallengeWinDto> wins;

    public List<ChallengeWinDto> getWins() {
        return wins;
    }

    public void setWins(List<ChallengeWinDto> wins) {
        this.wins = wins;
    }
}
