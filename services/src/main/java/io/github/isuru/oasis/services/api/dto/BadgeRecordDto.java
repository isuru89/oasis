package io.github.isuru.oasis.services.api.dto;

/**
 * @author iweerarathna
 */
public class BadgeRecordDto extends OasisRecordDto {

    private Long timeStart;
    private Long timeEnd;
    private String extIdStart;
    private String extIdEnd;

    private Integer badgeId;
    private String subBadgeId;

    public Long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Long timeStart) {
        this.timeStart = timeStart;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getExtIdStart() {
        return extIdStart;
    }

    public void setExtIdStart(String extIdStart) {
        this.extIdStart = extIdStart;
    }

    public String getExtIdEnd() {
        return extIdEnd;
    }

    public void setExtIdEnd(String extIdEnd) {
        this.extIdEnd = extIdEnd;
    }

    public Integer getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Integer badgeId) {
        this.badgeId = badgeId;
    }

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }
}
