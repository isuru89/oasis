package io.github.isuru.oasis.services.api.dto;

/**
 * @author iweerarathna
 */
public class OasisRecordDto {

    private Integer userId;
    private Long teamId;
    private String teamName;
    private Long teamScopeId;
    private String teamScopeName;
    private String eventType;
    private String extId;
    private Long ts;
    private String tag;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public String getTeamScopeName() {
        return teamScopeName;
    }

    public void setTeamScopeName(String teamScopeName) {
        this.teamScopeName = teamScopeName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
