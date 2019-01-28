package io.github.isuru.oasis.model.defs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RaceDef extends BaseDef {

    public static final Set<String> FROM_SCOPES = new HashSet<>(Arrays.asList(ScopingType.GLOBAL.name(),
            ScopingType.TEAM.name(), ScopingType.TEAM_SCOPE.name()));

    public static final Set<String> TIME_WINDOWS = new HashSet<>(Arrays.asList("WEEKLY", "MONTHLY", "DAILY"));

    private int leaderboardId;

    private String fromScope = ScopingType.TEAM_SCOPE.name();
    private String timeWindow = "weekly";

    private Integer top;

    private Map<Integer, Double> rankPoints;
    private String rankPointsExpression;

    public String getRankPointsExpression() {
        return rankPointsExpression;
    }

    public void setRankPointsExpression(String rankPointsExpression) {
        this.rankPointsExpression = rankPointsExpression;
    }

    public Map<Integer, Double> getRankPoints() {
        return rankPoints;
    }

    public void setRankPoints(Map<Integer, Double> rankPoints) {
        this.rankPoints = rankPoints;
    }

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
