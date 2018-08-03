package io.github.isuru.oasis.services.model;

import java.util.List;

/**
 * @author iweerarathna
 */
public class LeaderboardResponseDto {

    private LeaderboardRequestDto request;

    private List<LeaderboardRecordDto> rankings;

    public LeaderboardRequestDto getRequest() {
        return request;
    }

    public void setRequest(LeaderboardRequestDto request) {
        this.request = request;
    }

    public List<LeaderboardRecordDto> getRankings() {
        return rankings;
    }

    public void setRankings(List<LeaderboardRecordDto> rankings) {
        this.rankings = rankings;
    }
}
