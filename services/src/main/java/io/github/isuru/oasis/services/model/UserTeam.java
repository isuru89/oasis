package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class UserTeam {

    private Integer userId;
    private Integer teamId;
    private long since;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }
}
