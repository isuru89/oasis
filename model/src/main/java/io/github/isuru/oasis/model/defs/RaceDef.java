package io.github.isuru.oasis.model.defs;

public class RaceDef extends BaseDef {

    private int leaderboardId;

    private String fromScope = ScopingType.TEAM_SCOPE.name();
    private String timeWindow = "weekly";

    private Integer top;

    public String getFromScope() {
        return fromScope;
    }

    public void setFromScope(String fromScope) {
        this.fromScope = fromScope;
    }

    public int getLeaderboardId() {
        return leaderboardId;
    }

    public void setLeaderboardId(int leaderboardId) {
        this.leaderboardId = leaderboardId;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

}
