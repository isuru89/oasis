package io.github.isuru.oasis.model.handlers.output;

import io.github.isuru.oasis.model.events.JsonEvent;

import java.util.List;

/**
 * @author iweerarathna
 */
public class BadgeModel {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private List<JsonEvent> events;
    private String eventType;
    private String tag;
    private Long badgeId;
    private String subBadgeId;
    private Long ruleId;
    private Long ts;
    private Integer sourceId;

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
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

    public List<JsonEvent> getEvents() {
        return events;
    }

    public void setEvents(List<JsonEvent> events) {
        this.events = events;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public String getSubBadgeId() {
        return subBadgeId;
    }

    public void setSubBadgeId(String subBadgeId) {
        this.subBadgeId = subBadgeId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}