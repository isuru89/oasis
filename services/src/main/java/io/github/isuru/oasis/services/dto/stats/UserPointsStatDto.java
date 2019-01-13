package io.github.isuru.oasis.services.dto.stats;

/**
 * @author iweerarathna
 */
public class UserPointsStatDto {

    private Integer userId;
    private Integer pointId;
    private Double totalPoints;
    private Double deltaPoints;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPointId() {
        return pointId;
    }

    public void setPointId(Integer pointId) {
        this.pointId = pointId;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Double getDeltaPoints() {
        return deltaPoints;
    }

    public void setDeltaPoints(Double deltaPoints) {
        this.deltaPoints = deltaPoints;
    }
}
