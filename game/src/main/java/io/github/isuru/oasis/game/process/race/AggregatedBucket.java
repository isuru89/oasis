package io.github.isuru.oasis.game.process.race;

import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;

public class AggregatedBucket implements Serializable {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private Double aggValue;
    private Tuple2<Double, Long> supportValues;

    public boolean isFilled() {
        return userId != null || teamId != null || teamScopeId != null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public Tuple2<Double, Long> getSupportValues() {
        return supportValues;
    }

    public void setSupportValues(Tuple2<Double, Long> supportValues) {
        this.supportValues = supportValues;
    }

    public Double getAggValue() {
        return aggValue;
    }

    public void setAggValue(Double aggValue) {
        this.aggValue = aggValue;
    }
}
