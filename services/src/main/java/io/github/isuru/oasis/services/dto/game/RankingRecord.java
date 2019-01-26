package io.github.isuru.oasis.services.dto.game;

public class RankingRecord {

    private Integer rank;
    private Double nextValue;
    private Double topValue;

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public void setNextValue(Double nextValue) {
        this.nextValue = nextValue;
    }

    public Double getTopValue() {
        return topValue;
    }

    public void setTopValue(Double topValue) {
        this.topValue = topValue;
    }

}
