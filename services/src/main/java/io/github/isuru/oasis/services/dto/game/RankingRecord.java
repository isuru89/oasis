package io.github.isuru.oasis.services.dto.game;

public class RankingRecord {

    private Integer rank;
    private Double myValue;
    private Long myCount;
    private Double nextValue;
    private Double topValue;

    public Double getMyValue() {
        return myValue;
    }

    public void setMyValue(Double myValue) {
        this.myValue = myValue;
    }

    public Long getMyCount() {
        return myCount;
    }

    public void setMyCount(Long myCount) {
        this.myCount = myCount;
    }

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
