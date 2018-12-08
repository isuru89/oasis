package io.github.isuru.oasis.model.defs;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author iweerarathna
 */
public class ChallengeDef implements Serializable {

    private Long id;
    private String name;
    private String displayName;
    private String description;

    private long expireAfter;
    private Long startAt;
    private long winnerCount;

    private double points;

    private Collection<String> forEvents;
    private Long forTeamId;
    private String forTeam;
    private Long forUserId;
    private String forUser;
    private String forTeamScope;
    private Long forTeamScopeId;
    private List<String> conditions;

    private Long gameId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getForTeamScope() {
        return forTeamScope;
    }

    public void setForTeamScope(String forTeamScope) {
        this.forTeamScope = forTeamScope;
    }

    public Long getForTeamScopeId() {
        return forTeamScopeId;
    }

    public void setForTeamScopeId(Long forTeamScopeId) {
        this.forTeamScopeId = forTeamScopeId;
    }

    public Long getForTeamId() {
        return forTeamId;
    }

    public void setForTeamId(Long forTeamId) {
        this.forTeamId = forTeamId;
    }

    public String getForTeam() {
        return forTeam;
    }

    public void setForTeam(String forTeam) {
        this.forTeam = forTeam;
    }

    public Long getForUserId() {
        return forUserId;
    }

    public void setForUserId(Long forUserId) {
        this.forUserId = forUserId;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt;
    }

    public long getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(long winnerCount) {
        this.winnerCount = winnerCount;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Collection<String> getForEvents() {
        return forEvents;
    }

    public void setForEvents(Collection<String> forEvents) {
        this.forEvents = forEvents;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }
}
