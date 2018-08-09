package io.github.isuru.oasis.services.api.dto;

/**
 * @author iweerarathna
 */
public class PointRecordDto extends OasisRecordDto {

    private Integer pointId;
    private String pointName;
    private Float points;

    public Integer getPointId() {
        return pointId;
    }

    public void setPointId(Integer pointId) {
        this.pointId = pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public Float getPoints() {
        return points;
    }

    public void setPoints(Float points) {
        this.points = points;
    }

}
